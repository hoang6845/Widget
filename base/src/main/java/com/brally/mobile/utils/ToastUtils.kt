package com.brally.mobile.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.brally.mobile.base.application.appInfo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private var toast: Toast? = null

fun showError(context: Context, errorMessage: String) {
    CoroutineScope(Dispatchers.Main).launch {
        showMessageToast(context, errorMessage)
    }
}

fun showMessageToast(context: Context, message: String) {
    CoroutineScope(Dispatchers.Main).launch(CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }
}

fun Context.showDevelopMessage() {
    Toast.makeText(this, "develop", Toast.LENGTH_SHORT).show()
}


fun logApp(log: Any?) {
    if (log == null) {
        logApp(null, "null")
        return
    }
    when (log) {
        is String, is Int, is Float, is Char, is Double, is Long, is Boolean -> logApp(null, "$log")
        else -> logApp(null, log.toJson())
    }
}

fun logApp(key: String? = null, log: String) {
    if (appInfo().isDebug) {
        if (log.length > 4000) {
            Log.d(key ?: "logApp ->>>>>", " $log")
        } else {
            Log.i(key ?: "logApp ->>>>>", " $log")
        }
    }
}

