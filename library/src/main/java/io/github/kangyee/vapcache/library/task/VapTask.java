package io.github.kangyee.vapcache.library.task;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import io.github.kangyee.vapcache.library.logger.Logger;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class VapTask<T> {

    public static Executor EXECUTOR = Executors.newCachedThreadPool();

    private final Set<VapListener<T>> successListeners = new LinkedHashSet<>(1);
    private final Set<VapListener<Throwable>> failureListeners = new LinkedHashSet<>(1);
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    private volatile VapResult<T> result = null;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public VapTask(Callable<VapResult<T>> runnable) {
        this(runnable, false);
    }

    /**
     * runNow is only used for testing.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    VapTask(Callable<VapResult<T>> runnable, boolean runNow) {
        if (runNow) {
            try {
                setResult(runnable.call());
            } catch (Throwable e) {
                setResult(new VapResult<>(e));
            }
        } else {
            EXECUTOR.execute(new VapFutureTask(runnable));
        }
    }

    private void setResult(@Nullable VapResult<T> result) {
        if (this.result != null) {
            throw new IllegalStateException("A task may only be set once.");
        }
        this.result = result;
        notifyListeners();
    }

    /**
     * Add a task listener. If the task has completed, the listener will be called synchronously.
     *
     * @return the task for call chaining.
     */
    public synchronized VapTask<T> addListener(VapListener<T> listener) {
        VapResult<T> result = this.result;
        if (result != null && result.getValue() != null) {
            listener.onResult(result.getValue());
        }

        successListeners.add(listener);
        return this;
    }

    /**
     * Remove a given task listener. The task will continue to execute so you can re-add
     * a listener if necessary.
     *
     * @return the task for call chaining.
     */
    public synchronized VapTask<T> removeListener(VapListener<T> listener) {
        successListeners.remove(listener);
        return this;
    }

    /**
     * Add a task failure listener. This will only be called in the even that an exception
     * occurs. If an exception has already occurred, the listener will be called immediately.
     *
     * @return the task for call chaining.
     */
    public synchronized VapTask<T> addFailureListener(VapListener<Throwable> listener) {
        VapResult<T> result = this.result;
        if (result != null && result.getException() != null) {
            listener.onResult(result.getException());
        }

        failureListeners.add(listener);
        return this;
    }

    /**
     * Remove a given task failure listener. The task will continue to execute so you can re-add
     * a listener if necessary.
     *
     * @return the task for call chaining.
     */
    public synchronized VapTask<T> removeFailureListener(VapListener<Throwable> listener) {
        failureListeners.remove(listener);
        return this;
    }

    private void notifyListeners() {
        // Listeners should be called on the main thread.
        handler.post(() -> {
            // Local reference in case it gets set on a background thread.
            VapResult<T> result = VapTask.this.result;
            if (result == null) {
                return;
            }
            if (result.getValue() != null) {
                notifySuccessListeners(result.getValue());
            } else {
                notifyFailureListeners(result.getException());
            }
        });
    }

    private synchronized void notifySuccessListeners(T value) {
        // Allows listeners to remove themselves in onResult.
        // Otherwise we risk ConcurrentModificationException.
        List<VapListener<T>> listenersCopy = new ArrayList<>(successListeners);
        for (VapListener<T> l : listenersCopy) {
            l.onResult(value);
        }
    }

    @SuppressLint("RestrictedApi")
    private synchronized void notifyFailureListeners(Throwable e) {
        // Allows listeners to remove themselves in onResult.
        // Otherwise we risk ConcurrentModificationException.
        List<VapListener<Throwable>> listenersCopy = new ArrayList<>(failureListeners);
        if (listenersCopy.isEmpty()) {
            Logger.INSTANCE.warning("VapCache encountered an error but no failure listener was added:", e);
            return;
        }

        for (VapListener<Throwable> l : listenersCopy) {
            l.onResult(e);
        }
    }

    private class VapFutureTask extends FutureTask<VapResult<T>> {
        VapFutureTask(Callable<VapResult<T>> callable) {
            super(callable);
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                // We don't need to notify and listeners if the task is cancelled.
                return;
            }

            try {
                setResult(get());
            } catch (InterruptedException | ExecutionException e) {
                setResult(new VapResult<>(e));
            }
        }
    }

}
