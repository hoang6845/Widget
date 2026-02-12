package com.brally.mobile.utils

//import com.bralydn.analytics.event.BralyTracking
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.brally.mobile.base.R
import com.brally.mobile.base.application.getBaseApplication
import com.brally.mobile.service.firebase.SeverRemoteConfig
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.io.File

fun ImageView.loadImage(path: String) {
    val requestBuilder: RequestBuilder<Drawable> =
        Glide.with(this).asDrawable().sizeMultiplier(0.1f)
    Glide.with(this).load(File(path)).placeholder(R.drawable.bg_place_holder)
        .thumbnail(requestBuilder)
        .into(this)
}

fun ImageView.loadImageWithUri(uri: Uri) {
    val requestBuilder: RequestBuilder<Drawable> =
        Glide.with(this).asDrawable().sizeMultiplier(0.1f)
    Glide.with(this).load(uri)
        .thumbnail(requestBuilder)
        .into(this)
}

fun ImageView.loadImage(drawable: Int?) {
    val requestBuilder: RequestBuilder<Drawable> =
        Glide.with(this).asDrawable().sizeMultiplier(0.1f)
    Glide.with(this).load(drawable).thumbnail(requestBuilder)
        .into(this)
}

fun ImageView.loadImageBitmap(bitmap: Bitmap?, onResourceReady: (Bitmap) -> Unit) {
    val mBitmap = bitmap ?: return
    Glide.with(this).asBitmap().load(mBitmap)
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Bitmap,
                model: Any,
                target: Target<Bitmap>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                onResourceReady.invoke(resource)
                return false
            }

        }
        ).into(this)
}

fun ImageView.loadImageSketch(url: String) {
    val requestBuilder: RequestBuilder<Drawable> = Glide.with(this)
        .asDrawable()
       .sizeMultiplier(0.1f)
    val fullUrl = generateGlideUrl(url)
    Glide.with(this)
        .load(fullUrl)
        .thumbnail(requestBuilder)
        .diskCacheStrategy(DiskCacheStrategy.DATA)
       .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.loadImageFullSketch(url: String) {
    val requestBuilder: RequestBuilder<Drawable> = Glide.with(this)
        .asDrawable()
//        .sizeMultiplier(0.1f)
    val fullUrl = generateGlideUrl(url)
    Glide.with(this)
        .load(fullUrl)
        .thumbnail(requestBuilder)
        .diskCacheStrategy(DiskCacheStrategy.DATA)
//        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun preloadImage(
    url: String,
    onResourceReady: (Drawable) -> Unit,
    isFromRemote: Boolean
) {
    Glide.with(getBaseApplication())
        .load(if (isFromRemote) generateGlideUrl(url) else url)
        .centerCrop()
        .encodeFormat(Bitmap.CompressFormat.PNG)
        .encodeQuality(100)
        .skipMemoryCache(false)
        .into(object : CustomTarget<Drawable>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                onResourceReady.invoke(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) = Unit
        })
}

fun fetchImageFromGithub(context: Context, url: String, onResult: (Bitmap?) -> Unit) {
    val fullUrl = generateGlideUrl(url)
    Glide.with(context)
        .asBitmap()
        .load(fullUrl)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                onResult.invoke(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) = Unit

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                onResult.invoke(null)
            }
        })
}

fun ImageView.loadImageUrl(
    endPointUrl: String
) {
    val startTimeLoading = System.currentTimeMillis()
    Glide.with(this)
        .load(generateGlideUrl(endPointUrl))
        .placeholder(R.drawable.img_placeholder)
        .timeout(20000)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable?>,
                isFirstResource: Boolean
            ): Boolean {
                //BralyTracking.logEvent(this@loadImageUrl.context, IMAGE_LOADING_FAIL)
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                if (dataSource == DataSource.REMOTE) {
                    //BralyTracking.logEvent(this@loadImageUrl.context, IMAGE_LOADING_SUCCESS)
                }
                return false
            }
        })
        .into(this)
}

fun generateGlideUrl(endpoint: String): GlideUrl {
    val baseUrl = SeverRemoteConfig.getUrlBase()
    if (SeverRemoteConfig.isGithubUrl()) {
        val headers = SeverRemoteConfig.getHeaderToken()
        return GlideUrl(baseUrl + endpoint, headers)
    } else {
        return GlideUrl(baseUrl + endpoint)
    }
}

fun generateGlideUrlPublic(endpoint: String): GlideUrl {
    return GlideUrl(endpoint)
}
