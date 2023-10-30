package io.github.kangyee.vapcache.library.logger

import android.util.Log

object Logger {

    private const val TAG = "VapCache"

    private val loggedMessages = HashSet<String>()

    fun debug(message: String) {
        debug(message, null)
    }

    fun debug(message: String, exception: Throwable?) {
        Log.d(TAG, message, exception)
    }

    fun warning(message: String) {
        warning(message, null)
    }

    fun warning(message: String, exception: Throwable?) {
        if (loggedMessages.contains(message)) {
            return
        }

        Log.w(TAG, message, exception)
        loggedMessages.add(message)
    }

    fun error(message: String, exception: Throwable?) {
        Log.d(TAG, message, exception)
    }

}