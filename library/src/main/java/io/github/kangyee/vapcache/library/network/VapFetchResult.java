package io.github.kangyee.vapcache.library.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface VapFetchResult extends Closeable {

    /**
     * @return Is the operation successful
     */
    boolean isSuccessful();

    /**
     * @return Received content stream
     */
    @NonNull
    InputStream bodyByteStream() throws IOException;

    /**
     * @return Type of content received
     */
    @Nullable
    String contentType();

    /**
     * @return Operation error
     */
    @Nullable
    String error();

}
