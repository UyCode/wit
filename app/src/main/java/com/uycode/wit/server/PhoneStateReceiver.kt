package com.uycode.wit.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.widget.Toast
import com.uycode.wit.PhoneInfo
import com.uycode.wit.geo.PhoneNumberLookup
import com.uycode.wit.geo.algo.LookupAlgorithm
import com.uycode.wit.views.OverlayView

class PhoneStateReceiver: BroadcastReceiver() {

    private var overlayView: OverlayView? = null

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: return
                        val info = getPhoneNumberInfo(phoneNumber)
                        val content = info.isp + " " + info.province + " " + info.city;
                        showOverlay(context, phoneNumber, content)
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        overlayView?.hide()
                    }
                }
            }
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                val serviceIntent = Intent(context, PhoneStateReceiver::class.java)
                context.startForegroundService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


    private fun showToast(context: Context, info: String) {
        Toast.makeText(context, info, Toast.LENGTH_LONG).show()
    }

    private fun showOverlay(context: Context, phoneNumber: String, info: String) {
        if (overlayView == null) {
            overlayView = OverlayView(context.applicationContext)
        }
        overlayView?.show(phoneNumber, info)
    }


    companion object {
        private lateinit var lookup: PhoneNumberLookup

        fun initializeLookup(context: Context) {
            lookup = PhoneNumberLookup.instance()
            lookup.initialize(context)
            lookup.with(LookupAlgorithm.IMPL.SEQUENCE)
        }

        fun getPhoneNumberInfo(phoneNumber: String): PhoneInfo {


            if (!::lookup.isInitialized) {
                throw IllegalStateException("Lookup not initialized")
            }

            return lookup.lookup(phoneNumber)?.let { data ->
                PhoneInfo(
                    id = 1,
                    number = phoneNumber,
                    name = "",
                    isp = data.isp.carrier,
                    province = data.geoInfo.province,
                    city = data.geoInfo.city,
                    zip = data.geoInfo.zipCode,
                    areaCode = data.geoInfo.areaCode
                )
            }?:PhoneInfo.PhoneInfo()

        }


    }



}