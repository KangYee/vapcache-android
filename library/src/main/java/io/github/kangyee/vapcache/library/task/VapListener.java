package io.github.kangyee.vapcache.library.task;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface VapListener<T> {

    void onResult(T result);

}
