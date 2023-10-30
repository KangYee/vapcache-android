package io.github.kangyee.vapcache.library.network;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum FileExtension {
    MP4(".mp4");

    public final String extension;

    FileExtension(String extension) {
        this.extension = extension;
    }

    public String tempExtension() {
        return ".temp" + extension;
    }

    @Override public String toString() {
        return extension;
    }

}