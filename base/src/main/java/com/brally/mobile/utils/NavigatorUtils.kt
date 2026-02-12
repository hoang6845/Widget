package com.brally.mobile.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.brally.mobile.base.activity.BaseActivity
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.application.appInfo
import java.io.File
import java.io.Serializable

fun FragmentActivity.getBaseActivity(): BaseActivity<*, *>? {
    return if (this is BaseActivity<*, *>) {
        this
    } else null
}

fun FragmentActivity.getBaseFragment(): BaseFragment<*, *>? {
    return if (this is BaseFragment<*, *>) {
        this
    } else null
}

fun Fragment.getBaseFragment(): BaseFragment<*, *>? {
    return if (this is BaseFragment<*, *>) {
        this
    } else null
}

fun Activity.callResultActivity() {
    callResultActivity(null)
}

fun Activity.callResultActivity(bundle: Bundle?) {
    val returnIntent = Intent()
    if (bundle != null) returnIntent.putExtras(bundle)
    setResult(Activity.RESULT_OK, returnIntent)
    finish()
}

fun Context.openActivity(pClass: Class<out Activity>) {
    openActivity(pClass, null)
}

fun Context.openActivity(pClass: Class<out Activity>, bundle: Bundle?) {
    openActivity(pClass, false, bundle)
}

fun Context.openActivity(
    pClass: Class<out Activity>, isFinish: Boolean, bundle: Bundle? = null
) {
    val intent = Intent(this, pClass)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
    if (isFinish) {
        (this as Activity).finish()
    }
}

fun Context.openActivityNoAd(
    pClass: Class<out Activity>, isFinish: Boolean, bundle: Bundle? = null
) {
    val intent = Intent(this, pClass)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
    if (isFinish) {
        (this as Activity).finish()
    }
}


fun Context.openActivityNoAd(pClass: Class<out Activity>, bundle: Bundle? = null) {
    openActivityNoAd(pClass, false, bundle)
}


fun Context.openActivityReorderToFront(pClass: Class<out Activity>, isFinish: Boolean) {
    val intent = Intent(this, pClass)
    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    startActivity(intent)
    if (isFinish) {
        (this as Activity).finish()
    }
}

fun Context.openActivityForResult(
    pClass: Class<out Activity?>?,
    bundle: Bundle?,
    requestCode: Int,
) {
    val intent = Intent(this, pClass)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    (this as Activity).startActivityForResult(intent, requestCode)
}

fun Activity.openActivityForResult(pClass: Class<out Activity?>?, requestCode: Int) {
    openActivityForResult(pClass, null, requestCode)
}

fun <T> Context.openActivityWithClearTask(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}

fun <K : Serializable> pushParcelableBundle(k: K): Bundle {
    val bundle = Bundle()
    bundle.putSerializable(k.javaClass.name, k)
    return bundle
}

fun <K : Parcelable> pushParcelableBundle(k: K): Bundle {
    val bundle = Bundle()
    bundle.putParcelable(k.javaClass.name, k)
    return bundle
}

fun <K : Parcelable> AppCompatActivity.getParcelableBundle(key: String): K? {
    @Suppress("DEPRECATION")
    return when (intent != null && intent.extras?.containsKey(key) == true) {
        true -> intent.extras?.getParcelable(key)
        else -> null
    }
}

inline fun <reified T> pushBundle(data: T): Bundle {
    val bundle = Bundle()
    when (data) {
        is Long -> bundle.putLong(T::class.java.name, data)
        is Int -> bundle.putInt(T::class.java.name, data)
        is Float -> bundle.putFloat(T::class.java.name, data)
        is Boolean -> bundle.putBoolean(T::class.java.name, data)
        is String -> bundle.putString(T::class.java.name, data.toString())
        is Parcelable -> bundle.putParcelable(T::class.java.name, data)
        is Serializable -> bundle.putSerializable(T::class.java.name, data)
        else -> bundle.putString(T::class.java.name, data.toString())
    }
    return bundle
}

fun <T> pushBundle(key: String, data: T): Bundle {
    val bundle = Bundle()
    when (data) {
        is Long -> bundle.putLong(key, data)
        is Int -> bundle.putInt(key, data)
        is Float -> bundle.putFloat(key, data)
        is Boolean -> bundle.putBoolean(key, data)
        is String -> bundle.putString(key, data.toString())
        is Parcelable -> bundle.putParcelable(key, data)
        is Serializable -> bundle.putSerializable(key, data)
        else -> bundle.putString(key, data.toString())
    }
    return bundle
}

fun <T> pushBundle(hashMap: HashMap<String, T>): Bundle {
    return try {
        val bundle = Bundle()
        hashMap.forEach {
            when (it.value) {
                is Long -> bundle.putLong(it.key, it.value as Long)
                is Int -> bundle.putInt(it.key, it.value as Int)
                is Float -> bundle.putFloat(it.key, it.value as Float)
                is Boolean -> bundle.putBoolean(it.key, it.value as Boolean)
                is String -> bundle.putString(it.key, it.value.toString())
                is Parcelable -> bundle.putParcelable(it.key, it.value as Parcelable)
                is Serializable -> bundle.putSerializable(it.key, it.value as Serializable)
                else -> bundle.putString(it.key, it.value.toString())
            }
        }
        bundle
    } catch (ex: Exception) {
        ex.printStackTrace()
        Bundle()
    }
}

fun <I, O> ComponentActivity.launchWithResult(
    input: I, contract: ActivityResultContract<I, O>, onResult: (O) -> Unit
) {
    val contractKey = "contract_${System.currentTimeMillis()}"
    var launcher: ActivityResultLauncher<I>? = null
    launcher = activityResultRegistry.register(contractKey, contract) { result ->
        launcher?.unregister()
        onResult(result)
    }
    launcher.launch(input)
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            launcher.unregister()
        }
    })
}

fun ComponentActivity.startActivityWithResult(
    input: Intent, result: (ActivityResult) -> Unit
): Unit = launchWithResult(input, StartActivityForResult(), result)

fun openBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun openPlayStore(context: Context?, link: String? = null) {
    if (context == null) return
    if (link.isNullOrEmpty()) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)
            )
        )
        return
    }
    if (!link.startsWith("http://play")) {
        openBrowser(context, link)
        return
    }
    try {
        val uri = Uri.parse(link)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        context.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)
            )
        )
    }
}

fun Activity.rateApp(packageId: String) {
    val uri: Uri = Uri.parse("market://details?id=${packageId}")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    goToMarket.addFlags(
        Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    )
    try {
        startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=${packageId}")
            )
        )
    }
}


@SuppressLint("IntentReset")
fun Activity.feedback() {
    try {
        val packageGmail = "com.google.android.gm"
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.type = "text/plain"
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(appInfo().emailFeedback))
        if (isPackageInstalled(packageGmail, packageManager)) {
            intent.setPackage(packageGmail)
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, appInfo().appName)
        val link = "https://play.google.com/store/apps/details?id=${packageName}"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "App Name:\n${appInfo().appName} \n" + "${getDeviceInfo()}\n" + "Link Store: \n$link \n Write for me:\n"
        )
        if (isPackageInstalled(packageGmail, packageManager)) {
            startActivity(intent)
        } else {
            startActivity(
                Intent.createChooser(intent, "Send email")
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.shareApp(appId: String = appInfo().appId) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/html"
        shareIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            "Dear My friend, \n Download via ${appInfo().appName} this link \nThank you very much!"
        )
        var shareMessage = appInfo().appName
        shareMessage =
            "${shareMessage}\n https://play.google.com/store/apps/details?id=" + appId.trimIndent()
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "Choose one"))
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun Activity.shareText(textToShare: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare)
    startActivity(Intent.createChooser(shareIntent, "Choose one"))
}

fun Activity.shareFile(file: File) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "*/*"
        val uriForFile = FileProvider.getUriForFile(
            this, appInfo().appId + ".provider", file
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_STREAM, uriForFile)
        var shareMessage = appInfo().appName
        shareMessage =
            "${shareMessage}\n https://play.google.com/store/apps/details?id=" + appInfo().appId
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(intent, "Share Chat AI!"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


private fun getDeviceInfo(): String {
    return (Build.MODEL + " " + Build.BRAND + " (" + Build.VERSION.RELEASE + ")" + " API-" + Build.VERSION.SDK_INT)
}

private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Parcelable> Activity.getParcelable(): T? = when {
    Build.VERSION.SDK_INT >= 33 -> intent?.extras?.getParcelable(T::class.java.name, T::class.java)
    else -> @Suppress("DEPRECATION") intent?.extras?.getParcelable(T::class.java.name) as? T
}

inline fun <reified T : Parcelable> Bundle.getParcelable(): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(T::class.java.name, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(T::class.java.name) as? T
}


inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}
