package com.app.base.component.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.brally.mobile.utils.singleClick
import com.app.base.R
import com.app.base.databinding.DialogExitAppBinding
import java.lang.ref.WeakReference

class ExitsAppDialog(context: Context) {

    private val contextRef = WeakReference(context)

    private val binding by lazy {
        DialogExitAppBinding.inflate(LayoutInflater.from(contextRef.get()))
    }

    var onClickSubmit: (() -> Unit)? = null

    private var dialog: AlertDialog? = contextRef.get()?.let {
        AlertDialog.Builder(it, R.style.dialog_transparent_width).setView(binding.root).create()
    }

    init {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        setCancelOutside(false)
        binding.flStay.singleClick {
            hide()
        }

        binding.flExit.singleClick {
            onClickSubmit?.invoke()
            hide()
        }
        (contextRef.get() as? AppCompatActivity)?.lifecycle?.addObserver(object :
            DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                hide()
            }
        })
    }

    fun setCancelOutside(isCancel: Boolean? = true) {
        isCancel?.let { dialog?.setCancelable(it) }
        isCancel?.let { dialog?.setCanceledOnTouchOutside(it) }
    }

    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }

    fun hide() {
        try {
            dialog?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun show(fragment: Fragment, onClickSubmit: (() -> Unit)? = null) {
        try {
            this.onClickSubmit = onClickSubmit
            if (isShowing().not()) dialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
