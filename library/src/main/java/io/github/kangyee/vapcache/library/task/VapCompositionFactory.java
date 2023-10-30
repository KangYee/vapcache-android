package io.github.kangyee.vapcache.library.task;

import static okio.Okio.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.RestrictTo;
import androidx.annotation.WorkerThread;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.kangyee.vapcache.library.logger.Logger;
import io.github.kangyee.vapcache.library.model.VapCompositionCache;
import io.github.kangyee.vapcache.library.network.NetworkCache;
import io.github.kangyee.vapcache.library.network.VapNetworkManager;
import okio.BufferedSource;
import okio.Okio;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class VapCompositionFactory {

    private static final Map<String, VapTask<File>> taskCache = new HashMap<>();
    private static final Set<VapTaskIdleListener> taskIdleListeners = new HashSet<>();

    private VapCompositionFactory() {

    }

    /**
     * Set the maximum number of compositions to keep cached in memory.
     * This must be {@literal >} 0.
     */
    public static void setMaxCacheSize(int size) {
        VapCompositionCache.getInstance().resize(size);
    }

    @SuppressLint("RestrictedApi")
    public static void clearCache(Context context) {
        taskCache.clear();
        VapCompositionCache.getInstance().clear();
        final NetworkCache networkCache = VapNetworkManager.networkCache(context);
        if (networkCache != null) {
            networkCache.clear();
        }
    }

    /**
     * Use this to register a callback for when the composition factory is idle or not.
     * This can be used to provide data to an espresso idling resource.
     * Refer to FragmentVisibilityTests and its VapIdlingResource in the VapCache repo for
     * an example.
     */
    public static void registerVapTaskIdleListener(VapTaskIdleListener listener) {
        taskIdleListeners.add(listener);
        listener.onIdleChanged(taskCache.size() == 0);
    }

    public static void unregisterVapTaskIdleListener(VapTaskIdleListener listener) {
        taskIdleListeners.remove(listener);
    }

    /**
     * Fetch an animation from an http url. Once it is downloaded once, VapCache will cache the file to disk for
     * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
     * might need an animation in the future.
     * <p>
     * To skip the cache, add null as a third parameter.
     */
    public static VapTask<File> fromUrl(final Context context, final String url) {
        return fromUrl(context, url, "url_" + url);
    }

    /**
     * Fetch an animation from an http url. Once it is downloaded once, VapCache will cache the file to disk for
     * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
     * might need an animation in the future.
     */
    @SuppressLint("RestrictedApi")
    public static VapTask<File> fromUrl(final Context context, final String url, @Nullable final String cacheKey) {
        return cache(cacheKey, () -> {
            VapResult<File> result = VapNetworkManager.networkFetcher(context).fetchSync(context, url, cacheKey);
            if (cacheKey != null && result.getValue() != null) {
                VapCompositionCache.getInstance().put(cacheKey, result.getValue());
            }
            return result;
        }, null);
    }

    /**
     * Fetch an animation from an http url. Once it is downloaded once, VapCache will cache the file to disk for
     * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
     * might need an animation in the future.
     */
    @WorkerThread
    public static VapResult<File> fromUrlSync(Context context, String url) {
        return fromUrlSync(context, url, url);
    }

    /**
     * Fetch an animation from an http url. Once it is downloaded once, VapCache will cache the file to disk for
     * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
     * might need an animation in the future.
     */
    @WorkerThread
    public static VapResult<File> fromUrlSync(Context context, String url, @Nullable String cacheKey) {
        VapResult<File> result = VapNetworkManager.networkFetcher(context).fetchSync(context, url, cacheKey);
        if (cacheKey != null && result.getValue() != null) {
            VapCompositionCache.getInstance().put(cacheKey, result.getValue());
        }
        return result;
    }


    /**
     * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
     * The asset file name will be used as a cache key so future usages won't have to parse the json again.
     * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
     * <p>
     * To skip the cache, add null as a third parameter.
     */
    public static VapTask<File> fromAsset(Context context, final String fileName) {
        String cacheKey = "asset_" + fileName;
        return fromAsset(context, fileName, cacheKey);
    }

    /**
     * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
     * The asset file name will be used as a cache key so future usages won't have to parse the json again.
     * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
     * <p>
     * Pass null as the cache key to skip the cache.
     */
    public static VapTask<File> fromAsset(Context context, final String fileName, @Nullable final String cacheKey) {
        // Prevent accidentally leaking an Activity.
        final Context appContext = context.getApplicationContext();
        return cache(cacheKey, () -> fromAssetSync(appContext, fileName, cacheKey), null);
    }

    /**
     * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
     * The asset file name will be used as a cache key so future usages won't have to parse the json again.
     * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
     * <p>
     * To skip the cache, add null as a third parameter.
     */
    @WorkerThread
    public static VapResult<File> fromAssetSync(Context context, String fileName) {
        String cacheKey = "asset_" + fileName;
        return fromAssetSync(context, fileName, cacheKey);
    }

    /**
     * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
     * The asset file name will be used as a cache key so future usages won't have to parse the json again.
     * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
     * <p>
     * Pass null as the cache key to skip the cache.
     */
    @WorkerThread
    public static VapResult<File> fromAssetSync(Context context, String fileName, @Nullable String cacheKey) {
        try {
            return fromInputStreamSync(context, context.getAssets().open(fileName), cacheKey);
        } catch (IOException e) {
            return new VapResult<>(e);
        }
    }

    /**
     * Parse an animation from raw/res. This is recommended over putting your animation in assets because
     * it uses a hard reference to R.
     * The resource id will be used as a cache key so future usages won't parse the json again.
     * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
     * The Activity won't be leaked.
     * <p>
     * To skip the cache, add null as a third parameter.
     */
    public static VapTask<File> fromRawRes(Context context, @RawRes final int rawRes) {
        return fromRawRes(context, rawRes, rawResCacheKey(context, rawRes));
    }

    /**
     * Parse an animation from raw/res. This is recommended over putting your animation in assets because
     * it uses a hard reference to R.
     * The resource id will be used as a cache key so future usages won't parse the json again.
     * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
     * The Activity won't be leaked.
     * <p>
     * Pass null as the cache key to skip caching.
     */
    public static VapTask<File> fromRawRes(Context context, @RawRes final int rawRes, @Nullable final String cacheKey) {
        // Prevent accidentally leaking an Activity.
        final WeakReference<Context> contextRef = new WeakReference<>(context);
        final Context appContext = context.getApplicationContext();
        return cache(cacheKey, () -> {
            @Nullable Context originalContext = contextRef.get();
            Context context1 = originalContext != null ? originalContext : appContext;
            return fromRawResSync(context1, rawRes, cacheKey);
        }, null);
    }

    /**
     * Parse an animation from raw/res. This is recommended over putting your animation in assets because
     * it uses a hard reference to R.
     * The resource id will be used as a cache key so future usages won't parse the json again.
     * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
     * The Activity won't be leaked.
     * <p>
     * To skip the cache, add null as a third parameter.
     */
    @WorkerThread
    public static VapResult<File> fromRawResSync(Context context, @RawRes int rawRes) {
        return fromRawResSync(context, rawRes, rawResCacheKey(context, rawRes));
    }

    /**
     * Parse an animation from raw/res. This is recommended over putting your animation in assets because
     * it uses a hard reference to R.
     * The resource id will be used as a cache key so future usages won't parse the json again.
     * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
     * The Activity won't be leaked.
     * <p>
     * Pass null as the cache key to skip caching.
     */
    @WorkerThread
    public static VapResult<File> fromRawResSync(Context context, @RawRes int rawRes, @Nullable String cacheKey) {
        try {
            BufferedSource source = Okio.buffer(source(context.getResources().openRawResource(rawRes)));
            return fromInputStreamSync(context, source.inputStream(), cacheKey);
        } catch (Resources.NotFoundException e) {
            return new VapResult<>(e);
        }
    }

    private static String rawResCacheKey(Context context, @RawRes int resId) {
        return "rawRes" + (isNightMode(context) ? "_night_" : "_day_") + resId;
    }

    /**
     * It is important to include day/night in the cache key so that if it changes, the cache won't return an animation from the wrong bucket.
     */
    private static boolean isNightMode(Context context) {
        int nightModeMasked = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeMasked == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Auto-closes the stream.
     *
     * @see #fromInputStreamSync(Context, InputStream, String)
     */
    public static VapTask<File> fromInputStream(@Nullable Context context, final InputStream stream, @Nullable final String cacheKey) {
        return cache(cacheKey, () -> fromInputStreamSync(context, stream, cacheKey), () -> closeQuietly(stream));
    }

    /**
     * @see #fromInputStreamSync(Context, InputStream, String)
     */
    public static VapTask<File> fromInputStream(@Nullable Context context, final InputStream stream, @Nullable final String cacheKey, boolean close) {
        return cache(cacheKey, () -> fromInputStreamSync(context, stream, cacheKey, close), () -> {
            if (close) {
                closeQuietly(stream);
            }
        });
    }

    /**
     * Return a File for the given InputStream.
     */
    @WorkerThread
    public static VapResult<File> fromInputStreamSync(@Nullable Context context, InputStream stream, @Nullable String cacheKey) {
        return fromInputStreamSync(context, stream, cacheKey, true);
    }

    /**
     * Return a File for the given InputStream.
     */
    @WorkerThread
    public static VapResult<File> fromInputStreamSync(@Nullable Context context, InputStream stream, @Nullable String cacheKey, boolean close) {
        try {
            String fileName = System.currentTimeMillis() + ".mp4";
            File tempFile = new File(context.getCacheDir(), fileName);
            try {
                try (OutputStream output = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4 * 1024];
                    int read;
                    while ((read = stream.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                }
            } catch (Throwable e) {
                Logger.INSTANCE.warning("Unable to save mp4 to the temporary file: " + fileName + ". ", e);
            }
            if (cacheKey != null) {
                VapCompositionCache.getInstance().put(cacheKey, tempFile);
            }
            return new VapResult<>(tempFile);
        } catch (Exception e) {
            return new VapResult<>(e);
        } finally {
            if (close) {
                closeQuietly(stream);
            }
        }
    }

    /**
     * First, check to see if there are any in-progress tasks associated with the cache key and return it if there is.
     * If not, create a new task for the callable.
     * Then, add the new task to the task cache and set up listeners so it gets cleared when done.
     */
    private static VapTask<File> cache(@Nullable final String cacheKey, Callable<VapResult<File>> callable,
                                       @Nullable Runnable onCached) {
        VapTask<File> task = null;
        final File cachedFile = cacheKey == null ? null : VapCompositionCache.getInstance().get(cacheKey);
        if (cachedFile != null) {
            task = new VapTask<>(() -> new VapResult<>(cachedFile));
        }
        if (cacheKey != null && taskCache.containsKey(cacheKey)) {
            task = taskCache.get(cacheKey);
        }
        if (task != null) {
            if (onCached != null) {
                onCached.run();
            }
            return task;
        }

        task = new VapTask<>(callable);
        if (cacheKey != null) {
            AtomicBoolean resultAlreadyCalled = new AtomicBoolean(false);
            task.addListener(result -> {
                taskCache.remove(cacheKey);
                resultAlreadyCalled.set(true);
                if (taskCache.size() == 0) {
                    notifyTaskCacheIdleListeners(true);
                }
            });
            task.addFailureListener(result -> {
                taskCache.remove(cacheKey);
                resultAlreadyCalled.set(true);
                if (taskCache.size() == 0) {
                    notifyTaskCacheIdleListeners(true);
                }
            });
            // It is technically possible for the task to finish and for the listeners to get called
            // before this code runs. If this happens, the task will be put in taskCache but never removed.
            // This would require this thread to be sleeping at exactly this point in the code
            // for long enough for the task to finish and call the listeners. Unlikely but not impossible.
            if (!resultAlreadyCalled.get()) {
                taskCache.put(cacheKey, task);
                if (taskCache.size() == 1) {
                    notifyTaskCacheIdleListeners(false);
                }
            }
        }
        return task;
    }

    private static void notifyTaskCacheIdleListeners(boolean idle) {
        List<VapTaskIdleListener> listeners = new ArrayList<>(taskIdleListeners);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onIdleChanged(idle);
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
                // Ignore.
            }
        }
    }

}
