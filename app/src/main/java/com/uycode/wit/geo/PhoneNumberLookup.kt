package com.uycode.wit.geo

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import android.util.LruCache
import androidx.compose.ui.platform.LocalContext
import com.uycode.wit.Constants.Companion.PHONE_DATA_NAME
import com.uycode.wit.R
import com.uycode.wit.geo.algo.BinarySearchAlgorithm
import com.uycode.wit.geo.algo.LookupAlgorithm
import com.uycode.wit.geo.algo.ProspectBinarySearchAlgorithm
import com.uycode.wit.geo.algo.SequenceSearchAlgorithm
import com.uycode.wit.geo.data.PhoneNumberInfo
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.ref.WeakReference

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/8/29 11:09
 * @Version 1.0
 * @Description 手机号码查询
 */

class PhoneNumberLookup private constructor() {


    private var srcPhoneBytes: ByteArray? = null
    private var algoType: LookupAlgorithm.IMPL = LookupAlgorithm.IMPL.BINARY_SEARCH
    private val algorithmCache =
        mutableMapOf<LookupAlgorithm.IMPL, WeakReference<LookupAlgorithm>?>()

    fun with(algorithm: LookupAlgorithm.IMPL): PhoneNumberLookup {
        this.algoType = algorithm
        return this
    }

    fun lookup(phoneNumber: String): PhoneNumberInfo? {
        if (srcPhoneBytes == null) {
            throw IllegalStateException("Something happens when loading $PHONE_DATA_NAME")
        }
        if (algorithmCache[algoType]?.get() == null) {
            algorithmCache[algoType] = WeakReference(
                when (algoType) {
                    LookupAlgorithm.IMPL.BINARY_SEARCH -> BinarySearchAlgorithm(srcPhoneBytes!!)
                    LookupAlgorithm.IMPL.BINARY_SEARCH_PROSPECT -> ProspectBinarySearchAlgorithm(
                        srcPhoneBytes!!
                    )

                    else -> SequenceSearchAlgorithm(srcPhoneBytes!!)
                }
            )
        }
        return algorithmCache[algoType]?.get()?.lookup(phoneNumber)
    }

    fun initialize(context: Context) {
        try {
            manageFileCaching(PHONE_DATA_NAME, context).inputStream().use { geoStream ->
                //applicationContext?.resources?.openRawResource(R.raw.phone)?.use { geoStream ->

                ByteArrayOutputStream().use { baos ->
                    val buffer = ByteArray(1024 * 4)
                    var n = geoStream.read(buffer)

                    while (-1 != n) {
                        baos.write(buffer, 0, n)
                        n = geoStream.read(buffer)
                    }
                    srcPhoneBytes = baos.toByteArray()
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to load $PHONE_DATA_NAME")
        }

    }

    companion object {
        private var INSTANCE: PhoneNumberLookup? = null
        fun instance(): PhoneNumberLookup {
            if (INSTANCE == null) {
                synchronized(PhoneNumberLookup::class) {
                    if (INSTANCE == null) {
                        INSTANCE = PhoneNumberLookup()
                    }
                }
            }
            return INSTANCE!!
        }

        private fun getFileFromCache(fileName: String, context: Context): File? {
            val cacheFile = File(context.cacheDir, fileName)
            return if (cacheFile.exists()) {
                return cacheFile
            } else {
                null
            }
        }

        fun manageFileCaching(fileName: String, context: Context): File {
            var cachedFile = getFileFromCache(fileName, context)
            try {
                return if (cachedFile != null) {
                    cachedFile
                } else {
                    cachedFile = File(context.cacheDir, fileName)
                    val dir = File(Environment.getExternalStorageDirectory(), "wit")
                    val file = File(dir, fileName)
                    file.inputStream().use { input ->
                        FileOutputStream(cachedFile).use {
                            input.copyTo(it)
                        }
                    }
                    file
                }
            } catch (e: Exception) {
                Log.e("PhoneNumberLookup", "Failed to load $PHONE_DATA_NAME", e)
                return File(context.cacheDir, fileName)
            }
        }


    }
}