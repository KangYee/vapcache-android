package io.github.kangyee.vapcache.library

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred
import java.io.File

/**
 * 调用 [rememberVapComposition] 后的返回结果。
 * 实现了 State 接口，所以你可以直接这样使用：
 * ```
 * val compositionResult = rememberVapComposition(...)
 * // 或
 * val composition by rememberVapComposition(...)
 * ```
 *
 * @see rememberVapComposition
 */
@Stable
interface VapCompositionResult : State<File?> {

    /**
     * 存储获取到的文件数据。
     * 如果正在加载或加载失败则会为 null。
     */
    override val value: File?

    /**
     * 加载过程中的异常
     */
    val error: Throwable?

    /**
     * 是否正在加载
     */
    val isLoading: Boolean

    /**
     * 是否加载完成
     */
    val isComplete: Boolean

    /**
     * 是否失败
     */
    val isFailure: Boolean

    /**
     * 是否成功
     */
    val isSuccess: Boolean

    /**
     * 挂起，直到加载完成/失败
     */
    suspend fun await(): File

}

/**
 * 挂起，直到加载完成/失败，可能会返回 null
 */
suspend fun VapCompositionResult.awaitOrNull(): File? {
    return try {
        await()
    } catch (e: Throwable) {
        null
    }
}

@Stable
internal class VapCompositionResultImpl : VapCompositionResult {

    private val compositionDeferred = CompletableDeferred<File>()

    override var value: File? by mutableStateOf(null)
    private set

    override var error by mutableStateOf<Throwable?>(null)
    private set

    override val isLoading by derivedStateOf { value == null && error == null }

    override val isComplete by derivedStateOf { value != null || error != null }

    override val isFailure by derivedStateOf { error != null }

    override val isSuccess by derivedStateOf { value != null }

    override suspend fun await(): File {
        return compositionDeferred.await()
    }

    @Synchronized
    internal fun complete(file: File) {
        if (isComplete) return

        this.value = file
        compositionDeferred.complete(file)
    }

    @Synchronized
    internal fun completeExceptionally(error: Throwable) {
        if (isComplete) return

        this.error = error
        compositionDeferred.completeExceptionally(error)
    }

}