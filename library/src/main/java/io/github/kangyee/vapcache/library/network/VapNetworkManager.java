package io.github.kangyee.vapcache.library.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.io.File;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class VapNetworkManager {

    private static boolean networkCacheEnabled = true;
    private static boolean disablePathInterpolatorCache = true;

    private static VapNetworkFetcher fetcher;
    private static VapNetworkCacheProvider cacheProvider;

    private static volatile NetworkFetcher networkFetcher;
    private static volatile NetworkCache networkCache;

    @NonNull
    public static NetworkFetcher networkFetcher(@NonNull Context context) {
        NetworkFetcher local = networkFetcher;
        if (local == null) {
            synchronized (NetworkFetcher.class) {
                local = networkFetcher;
                if (local == null) {
                    networkFetcher = local = new NetworkFetcher(
                            networkCache(context),
                            fetcher != null ? fetcher : new DefaultVapNetworkFetcher()
                    );
                }
            }
        }
        return local;
    }

    @Nullable
    public static NetworkCache networkCache(@NonNull final Context context) {
        if (!networkCacheEnabled) {
            return null;
        }
        final Context appContext = context.getApplicationContext();
        NetworkCache local = networkCache;
        if (local == null) {
            synchronized (NetworkCache.class) {
                local = networkCache;
                if (local == null) {
                    networkCache = local = new NetworkCache(cacheProvider != null ? cacheProvider :
                            () -> new File(appContext.getCacheDir(), "vap_network_cache"));
                }
            }
        }
        return local;
    }

}
