package io.github.kangyee.vapcache.library.model;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.collection.LruCache;

import java.io.File;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class VapCompositionCache {

    private static final VapCompositionCache INSTANCE = new VapCompositionCache();

    public static VapCompositionCache getInstance() {
        return INSTANCE;
    }

    private final LruCache<String, File> cache = new LruCache<>(20);

    @VisibleForTesting
    private VapCompositionCache() {

    }

    @Nullable
    public File get(@Nullable String cacheKey) {
        if (cacheKey == null) {
            return null;
        }
        return cache.get(cacheKey);
    }

    public void put(@Nullable String cacheKey, File file) {
        if (cacheKey == null) {
            return;
        }
        cache.put(cacheKey, file);
    }

    public void clear() {
        cache.evictAll();
    }

    /**
     * 设置在内存中缓存的动画数量
     * 这个值需要 > 0
     */
    public void resize(int size) {
        cache.resize(size);
    }

}
