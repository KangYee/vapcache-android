package io.github.kangyee.vapcache.library.network;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DefaultVapNetworkFetcher implements VapNetworkFetcher {

    @Override
    @NonNull
    public VapFetchResult fetchSync(@NonNull String url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        return new DefaultVapFetchResult(connection);
    }
}
