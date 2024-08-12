package com.uycode.wit.views

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.uycode.wit.R

class OverlayView(context: Context) : FrameLayout(context) {
    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val view: View = layoutInflater.inflate(R.layout.overlay_layout, this, true)
    private val phoneNumberTextView: TextView = view.findViewById(R.id.overlayPhoneNumber)
    private val infoTextView: TextView = view.findViewById(R.id.overlayInfo)
    private val handler: Handler = Handler(Looper.getMainLooper())

    private val params: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.CENTER or Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
        width = 1040
        height = 800
    }
    init {
        view.setOnTouchListener { v, event ->
            hide()
            true
        }
    }


    fun show(phoneNumber: String, info: String) {
        Log.d("OverlayView", "Showing overlay with phone number: $phoneNumber")
        phoneNumberTextView.text = phoneNumber
        infoTextView.text = info
        try {
            windowManager.addView(this, params)
        } catch (e: Exception) {
            Log.e("OverlayView", "Error showing overlay", e)
        }

        // Auto-hide after 5 seconds
        handler.postDelayed({ hide() }, 5000)
    }

    fun hide() {
        //handler.removeCallbacksAndMessages(null)
        try {
            windowManager.removeView(this)
        } catch (e: IllegalArgumentException) {
            // View is not attached, ignore
        }
    }

}
