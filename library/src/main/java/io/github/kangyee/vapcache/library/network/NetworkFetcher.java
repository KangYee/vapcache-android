package io.github.kangyee.vapcache.library.network;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.github.kangyee.vapcache.library.logger.Logger;
import io.github.kangyee.vapcache.library.task.VapCompositionFactory;
import io.github.kangyee.vapcache.library.task.VapResult;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class NetworkFetcher {

    @Nullable
    private final NetworkCache networkCache;
    @NonNull
    private final VapNetworkFetcher fetcher;

    public NetworkFetcher(@Nullable NetworkCache networkCache, @NonNull VapNetworkFetcher fetcher) {
        this.networkCache = networkCache;
        this.fetcher = fetcher;
    }

    @NonNull
    @WorkerThread
    public VapResult<File> fetchSync(Context context, @NonNull String url, @Nullable String cacheKey) {
        File result = fetchFromCache(context, url, cacheKey);
        if (result != null) {
            return new VapResult<>(result);
        }

        Logger.INSTANCE.debug("Animation for " + url + " not found in cache. Fetching from network.");

        return fetchFromNetwork(context, url, cacheKey);
    }

    @Nullable
    @WorkerThread
    private File fetchFromCache(Context context, @NonNull String url, @Nullable String cacheKey) {
        if (cacheKey == null || networkCache == null) {
            return null;
        }
        Pair<FileExtension, InputStream> cacheResult = networkCache.fetch(url);
        if (cacheResult == null) {
            return null;
        }

        FileExtension extension = cacheResult.first;
        InputStream inputStream = cacheResult.second;
        VapResult<File> result = VapCompositionFactory.fromInputStreamSync(
                context, inputStream, cacheKey
        );
        if (result.getValue() != null) {
            return result.getValue();
        }
        return null;
    }

    @NonNull
    @WorkerThread
    private VapResult<File> fetchFromNetwork(Context context, @NonNull String url, @Nullable String cacheKey) {
        Logger.INSTANCE.debug("Fetching " + url);

        VapFetchResult fetchResult = null;
        try {
            fetchResult = fetcher.fetchSync(url);
            if (fetchResult.isSuccessful()) {
                InputStream inputStream = fetchResult.bodyByteStream();
                String contentType = fetchResult.contentType();
                VapResult<File> result = fromInputStream(context, url, inputStream, contentType, cacheKey);
                Logger.INSTANCE.debug("Completed fetch from network. Success: " + (result.getValue() != null));
                return result;
            } else {
                return new VapResult<>(new IllegalArgumentException(fetchResult.error()));
            }
        } catch (Exception e) {
            Logger.INSTANCE.warning("fetch failed", e);
            return new VapResult<>(e);
        } finally {
            if (fetchResult != null) {
                try {
                    fetchResult.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.INSTANCE.warning("VapFetchResult close failed");
                }
            }
        }
    }

    @NonNull
    private VapResult<File> fromInputStream(Context context, @NonNull String url, @NonNull InputStream inputStream, @Nullable String contentType,
                                            @Nullable String cacheKey) throws IOException {
        FileExtension extension;
        VapResult<File> result;
        Logger.INSTANCE.debug("Received data response.");
        extension = FileExtension.MP4;
        result = fromFileStream(context, url, inputStream, cacheKey);

        if (cacheKey != null && result.getValue() != null && networkCache != null) {
            networkCache.renameTempFile(url, extension);
        }

        return result;
    }

    @NonNull
    private VapResult<File> fromFileStream(Context context, @NonNull String url, @NonNull InputStream inputStream, @Nullable String cacheKey)
            throws IOException {
        if (cacheKey == null || networkCache == null) {
            return VapCompositionFactory.fromInputStreamSync(context, inputStream, null);
        }
        File file = networkCache.writeTempCacheFile(url, inputStream, FileExtension.MP4);
        return VapCompositionFactory.fromInputStreamSync(context, new FileInputStream(file.getAbsolutePath()), url);
    }

}
