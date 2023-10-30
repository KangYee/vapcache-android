package io.github.kangyee.vapcache.library.task;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.Arrays;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class VapResult<V> {

    @Nullable
    private final V value;
    @Nullable private final Throwable exception;

    public VapResult(V value) {
        this.value = value;
        exception = null;
    }

    public VapResult(Throwable exception) {
        this.exception = exception;
        value = null;
    }

    @Nullable public V getValue() {
        return value;
    }

    @Nullable public Throwable getException() {
        return exception;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VapResult)) {
            return false;
        }
        VapResult<?> that = (VapResult<?>) o;
        if (getValue() != null && getValue().equals(that.getValue())) {
            return true;
        }
        if (getException() != null && that.getException() != null) {
            return getException().toString().equals(getException().toString());
        }
        return false;
    }

    @Override public int hashCode() {
        return Arrays.hashCode(new Object[]{getValue(), getException()});
    }
}
