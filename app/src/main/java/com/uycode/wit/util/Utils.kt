package com.uycode.wit.util

import android.graphics.BitmapFactory
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import com.uycode.wit.Constants
import com.uycode.wit.R
import java.io.File


fun reformatPhoneNumber(s: String): String {
    return s.replace(Constants.PURE_NUMBER_REGEX.toRegex(), "")
}

@Composable
fun getResource(name: String): BitmapPainter {
    val dir = File(Environment.getExternalStorageDirectory(), "wit")
    val decodeFile = BitmapFactory.decodeFile("$dir/$name")
    val bg: BitmapPainter = if (decodeFile == null) {
        painterResource(id = R.drawable.bg_autumn) as BitmapPainter
    } else {
        BitmapPainter(decodeFile.asImageBitmap())
    }
    return bg
}