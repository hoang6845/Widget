package com.brally.mobile.service.network

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.brally.mobile.data.model.EventModel
import com.brally.mobile.service.event.sendEvent
import com.brally.mobile.utils.isNetworkAvailable
import com.brally.mobile.utils.logApp

class NetworkStatusReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        val isNet = isNetworkAvailable()
        logApp("NetworkStatusReceiver $isNet")
        sendEvent(EventModel(EventModel.EVENT_NETWORK, isNet))
    }
}
