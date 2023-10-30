package io.github.kangyee.vapcache.library.network;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.io.File;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface VapNetworkCacheProvider {

    /**
     * Called during cache operations
     *
     * @return cache directory
     */
    @NonNull
    File getCacheDir();

}
