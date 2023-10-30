package io.github.kangyee.vapcache.library.network;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.WorkerThread;

import java.io.IOException;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface VapNetworkFetcher {

    @WorkerThread
    @NonNull
    VapFetchResult fetchSync(@NonNull String url) throws IOException;

}
