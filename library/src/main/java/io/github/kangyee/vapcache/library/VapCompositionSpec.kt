package io.github.kangyee.vapcache.library

interface VapCompositionSpec {

    /**
     * 从 res/raw 加载动画。
     */
    @JvmInline
    value class RawRes(@androidx.annotation.RawRes val resId: Int) : VapCompositionSpec

    /**
     * 从网络加载动画，通过 HttpURLConnection 实现。
     */
    @JvmInline
    value class Url(val url: String) : VapCompositionSpec

    /**
     * 从本地文件加载动画。
     * 须确保有对应文件读取权限，否则可能会导致读取失败。
     */
    @JvmInline
    value class File(val filePath: String) : VapCompositionSpec

    /**
     * 从 assets 加载动画。
     */
    @JvmInline
    value class Asset(val assetName: String) : VapCompositionSpec


}