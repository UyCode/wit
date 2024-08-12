package com.uycode.wit.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uycode.wit.BLURRED_BG_KEY
import com.uycode.wit.BLUR_RADIUS
import com.uycode.wit.ISPEnum
import com.uycode.wit.MainActivity
import com.uycode.wit.PhoneInfo
import com.uycode.wit.R
import com.uycode.wit.geo.PhoneNumberLookup
import com.uycode.wit.geo.algo.LookupAlgorithm
import com.uycode.wit.util.reformatPhoneNumber
import dev.jakhongirmadaminov.glassmorphiccomposables.fastblur
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }

    var phoneInfo by remember { mutableStateOf(PhoneInfo.PhoneInfo()) }

    val maxWidth = LocalConfiguration.current.screenWidthDp

    val lookup = PhoneNumberLookup.instance()
    lookup.initialize(LocalContext.current)
    lookup.with(LookupAlgorithm.IMPL.SEQUENCE)

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(MainActivity.getInstance().memoryCache[BLURRED_BG_KEY]) }

    Box(modifier = Modifier.fillMaxSize()) {

        val captureController = rememberCaptureController()
        Capturable(
            controller = captureController,
            onCaptured = { bitmap, _ ->
                // This is captured bitmap of a content inside Capturable Composable.
                bitmap?.let {
                    fastblur(it.asAndroidBitmap(), 1f, BLUR_RADIUS)?.let { fastBlurred ->
                        // Bitmap is captured successfully. Do something with it!
                        MainActivity.getInstance().memoryCache.put(BLURRED_BG_KEY, fastBlurred)
                        capturedBitmap = fastBlurred
                    }
                }
            }

        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_autumn),
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        LaunchedEffect(key1 = true, block = {
            withContext(Dispatchers.Main) {
                if (capturedBitmap == null) captureController.capture()
            }
        })

        Row(
            Modifier
                .size(width = maxWidth.dp, height = 75.dp)
                .padding(top = 2.dp)
        ) {
            Column(modifier = Modifier.padding(start = 15.dp, end = 15.dp)) {
                OutlinedTextField(
                    suffix = {
                        ElevatedButton(onClick = {
                            val s = reformatPhoneNumber(searchQuery)
                            lookup.lookup(s)?.apply {
                                phoneInfo = PhoneInfo(
                                    1,
                                    number,
                                    name = "",
                                    isp.carrier,
                                    geoInfo.province,
                                    geoInfo.city,
                                    geoInfo.zipCode,
                                    geoInfo.areaCode
                                )
                            }
                            searchQuery  = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                            Text(text = "Search")
                        }
                    },
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(text = "Search", color = Color.Gray) },
                    modifier = Modifier
                        /*.background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Cyan,
                                    Color.White
                                )
                            )
                        )*/
                        .size(width = maxWidth.dp, height = 75.dp),
                    placeholder = { Text(text = "input 11 digits", color = Color.Gray) }
                )
            }

        }
        Row(
            modifier = Modifier
                .padding(top = 100.dp, start = 15.dp, end = 15.dp)
                .size(width = maxWidth.dp, height = 350.dp),
        ) {
            Column {
                Card(
                    onClick = { },
                    modifier = Modifier.fillMaxSize(),
                    shape = CardDefaults.elevatedShape,
                    colors = CardDefaults.cardColors().copy(Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, Color.Transparent)
                                )
                            )

                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(ISPEnum.getByNameCn(phoneInfo.isp).icon),
                                    contentDescription = "isp-icon",
                                    modifier = Modifier.size(56.dp)
                                )
                            }

                            val defaultModifier =
                                Modifier
                                    .weight(1f)
                                    .padding(start = 15.dp, top = 25.dp, bottom = 15.dp)
                            val defaultColor = Color.White

                            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = defaultModifier) {
                                    Text(text = "手机号：" + phoneInfo.number, color = defaultColor, fontWeight = FontWeight.W900)
                                }
                                Column(modifier = defaultModifier) {
                                    Text(text = "运营商：" + phoneInfo.isp, color = defaultColor, fontWeight = FontWeight.W900)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = defaultModifier) {
                                    Text(text = "省份：" + phoneInfo.province, color = defaultColor, fontWeight = FontWeight.W900)
                                }
                                Column(modifier = defaultModifier) {
                                    Text(text = "城市：" + phoneInfo.city, color = defaultColor, fontWeight = FontWeight.W900)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = defaultModifier) {
                                    Text(text = "邮编：" + phoneInfo.zip, color = defaultColor, fontWeight = FontWeight.W900)
                                }
                                Column(modifier = defaultModifier) {
                                    Text(text = "区号：" + phoneInfo.areaCode, color = defaultColor, fontWeight = FontWeight.W900)
                                }
                            }
                        }
                    }
                }

            }
        }
    }

}