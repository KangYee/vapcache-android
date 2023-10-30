package io.github.kangyee.vapcache.library

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.kangyee.vapcache.library.task.VapCompositionFactory
import io.github.kangyee.vapcache.library.task.VapTask
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val DefaultCacheKey = "__VapInternalDefaultCacheKey__"

@Composable
fun rememberVapComposition(
    spec: VapCompositionSpec,
    cacheKey: String? = DefaultCacheKey,
    onRetry: suspend (failCount: Int, previousException: Throwable) -> Boolean = { _, _ -> false },
): VapCompositionResult {
    val context = LocalContext.current
    val result by remember(spec) { mutableStateOf(VapCompositionResultImpl()) }
    remember(spec, cacheKey) { vapTask(context, spec, cacheKey, isWarmingCache = true) }
    LaunchedEffect(spec, cacheKey) {
        var exception: Throwable? = null
        var failedCount = 0
        while (!result.isSuccess && (failedCount == 0 || onRetry(failedCount, exception!!))) {
            try {
                val composition = vapComposition(
                    context,
                    spec,
                    cacheKey,
                )
                result.complete(composition)
            } catch (e: Throwable) {
                exception = e
                failedCount++
            }
        }
        if (!result.isComplete && exception != null) {
            result.completeExceptionally(exception)
        }
    }
    return result
}

private suspend fun vapComposition(
    context: Context,
    spec: VapCompositionSpec,
    cacheKey: String?,
): File {
    val task = requireNotNull(vapTask(context, spec, cacheKey, isWarmingCache = false)) {
        "Unable to create load task for $spec."
    }

    return task.await()
}

private fun vapTask(
    context: Context,
    spec: VapCompositionSpec,
    cacheKey: String?,
    isWarmingCache: Boolean,
): VapTask<File>? {
    return when (spec) {
        is VapCompositionSpec.RawRes -> {
            if (cacheKey == DefaultCacheKey) {
                VapCompositionFactory.fromRawRes(context, spec.resId)
            } else {
                VapCompositionFactory.fromRawRes(context, spec.resId, cacheKey)
            }
        }
        is VapCompositionSpec.Url -> {
            if (cacheKey == DefaultCacheKey) {
                VapCompositionFactory.fromUrl(context, spec.url)
            } else {
                VapCompositionFactory.fromUrl(context, spec.url, cacheKey)
            }
        }
        is VapCompositionSpec.File -> {
            if (isWarmingCache) {
                // Warming the cache is done from the main thread so we can't
                // create the FileInputStream needed in this path.
                null
            } else {
                val fis = FileInputStream(spec.filePath)
                VapCompositionFactory.fromInputStream(
                    context,
                    fis,
                    if (cacheKey == DefaultCacheKey) spec.filePath else cacheKey,
                )
            }
        }
        is VapCompositionSpec.Asset -> {
            if (cacheKey == DefaultCacheKey) {
                VapCompositionFactory.fromAsset(context, spec.assetName)
            } else {
                VapCompositionFactory.fromAsset(context, spec.assetName, cacheKey)
            }
        }
        else -> null
    }
}

private suspend fun <T> VapTask<T>.await(): T = suspendCancellableCoroutine { cont ->
    addListener { c ->
        if (!cont.isCompleted) cont.resume(c)
    }.addFailureListener { e ->
        if (!cont.isCompleted) cont.resumeWithException(e)
    }
}