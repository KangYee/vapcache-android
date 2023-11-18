package io.github.kangyee.vapcache.library.ext

import com.tencent.qgame.animplayer.AnimView
import io.github.kangyee.vapcache.library.task.VapCompositionFactory
import io.github.kangyee.vapcache.library.task.VapTask
import java.io.File

fun AnimView.startPlay(url: String) {
    val task = VapCompositionFactory.fromUrl(context, url)
    setCompositionTask(task)
}

private fun setCompositionTask(compositionTask: VapTask<File>) {
    userActionsTaken.add(UserActionTaken.SET_ANIMATION)
    clearComposition()
    cancelLoaderTask()
    this.compositionTask = compositionTask
        .addListener(loadedListener)
        .addFailureListener(wrappedFailureListener)
}