package com.uycode.wit.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.provider.CallLog
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.uycode.wit.BLURRED_BG_RECENT
import com.uycode.wit.BLUR_RADIUS
import com.uycode.wit.Constants
import com.uycode.wit.ISPEnum
import com.uycode.wit.MainActivity
import com.uycode.wit.PhoneInfo
import com.uycode.wit.geo.PhoneNumberLookup
import com.uycode.wit.geo.algo.LookupAlgorithm
import com.uycode.wit.util.ShowDialog
import com.uycode.wit.util.getResource
import com.uycode.wit.util.reformatPhoneNumber
import dev.jakhongirmadaminov.glassmorphiccomposables.GlassmorphicColumn
import dev.jakhongirmadaminov.glassmorphiccomposables.Place
import dev.jakhongirmadaminov.glassmorphiccomposables.fastblur
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

@Composable
fun RecentScreen() {
    val context = LocalContext.current
    PhoneRecentList(numbers = readContactsAndPhoneNumbers(context), context = context)
}


@SuppressLint("HardwareIds")
fun readContactsAndPhoneNumbers(context: Context): List<PhoneInfo> {
    val phoneNumbers = mutableListOf<String>()

    // Read contacts
    val cursor = context.contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
        null, null, "${CallLog.Calls.DATE} DESC"
    )

    cursor?.use {
        while (it.moveToNext()) {

            val phoneNumberIndex =
                it.getColumnIndex(CallLog.Calls.NUMBER)
            val phoneNumber = it.getString(phoneNumberIndex)
            phoneNumbers.add(phoneNumber)
            if (phoneNumbers.size == Constants.SEARCH_LIMIT) {
                break
            }

        }
    }

    val lookup = PhoneNumberLookup.instance()
    lookup.initialize(context)
    lookup.with(LookupAlgorithm.IMPL.SEQUENCE)
    // Now you have a list of phone numbers
    val result = mutableListOf<PhoneInfo>()
    for (number in phoneNumbers) {
        val finalNumber = reformatPhoneNumber(number)
        val randomLong = (1000..10000).random()

        if (finalNumber.length != Constants.PHONE_NUMBER_LENGTH) {
            result.add(
                PhoneInfo(
                    id = randomLong,
                    number = number,
                    name = "",
                    isp = ISPEnum.UNKNOWN.nameCn,
                    province = ISPEnum.UNKNOWN.nameCn,
                    city = ISPEnum.UNKNOWN.nameCn,
                    zip = ISPEnum.UNKNOWN.nameCn,
                    areaCode = ISPEnum.UNKNOWN.nameCn
                )
            )
        } else {
            lookup.lookup(finalNumber)?.apply {
                // generate random long number
                result.add(
                    PhoneInfo(
                        id = randomLong,
                        number = number,
                        name = "",
                        isp = isp.carrier,
                        province = geoInfo.province,
                        city = geoInfo.city,
                        zip = geoInfo.zipCode,
                        areaCode = geoInfo.areaCode
                    )
                )
            }
        }

        // Handle the result
    }
    Log.d("WIT-DEBUG-Res", "Number: $result")
    return result
}


@Composable
fun PhoneRecentList(numbers: List<PhoneInfo>, context: Context) {


    var shouldShowDialog by remember { mutableStateOf(false) }

    var phoneInfo by remember { mutableStateOf(PhoneInfo.PhoneInfo()) }

    if (shouldShowDialog) {
        ShowDialog(phoneInfo, context, onDismiss = { shouldShowDialog = !shouldShowDialog })
    }


    val scrollState = rememberScrollState()

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(MainActivity.getInstance().memoryCache[BLURRED_BG_RECENT]) }

    val childMeasures = remember {
        mutableStateListOf<Place>().apply {
            addAll(numbers.map { Place() })
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val captureController = rememberCaptureController()
        Capturable(
            controller = captureController,
            onCaptured = { bitmap, _ ->
                // This is captured bitmap of a content inside Capturable Composable.
                bitmap?.let {
                    fastblur(it.asAndroidBitmap(), 1f, BLUR_RADIUS)?.let { fastBlurred ->
                        // Bitmap is captured successfully. Do something with it!
                        MainActivity.getInstance().memoryCache.put(BLURRED_BG_RECENT, fastBlurred)
                        capturedBitmap = fastBlurred
                    }
                }
            }

        ) {
            Image(
                painter = getResource("bg_recent.jpg"),
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        LaunchedEffect(key1 = true, block = {
            withContext(Main) {
                if (capturedBitmap == null) captureController.capture()
            }
        })



        capturedBitmap?.let { capturedImage ->
            GlassmorphicColumn(
                modifier = Modifier.padding(
                    top = 25.dp,
                    bottom = 25.dp,
                    start = 25.dp,
                    end = 25.dp
                ),
                scrollState = scrollState,
                childMeasures = childMeasures,
                targetBitmap = capturedImage,
                dividerSpace = 10,
                blurRadius = 10,
                drawOnTop = { path ->
                    val strokeColor = Color(0x80ffffff)
                    val transparent = Color.Transparent
                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = Stroke(1f),
                    )
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(listOf(strokeColor, transparent)),
                        blendMode = BlendMode.Screen
                    )

                },
                content = {
                    numbers.forEachIndexed { index, it ->

                        Box(
                            modifier = Modifier
                                //.background(brush = Brush.verticalGradient(listOf(Color.Gray, Color.Transparent)))
                                .onGloballyPositioned {
                                    childMeasures[index] = Place(it.size, it.positionInParent())
                                }
                                .width(450.dp)
                                .height(125.dp)
                                .padding(15.dp)
                                .clickable(onClick = {
                                    shouldShowDialog = !shouldShowDialog; phoneInfo = it
                                })
                        ) {
                            Log.d("WIT-DEBUG", "Size of the childMeasures: ${childMeasures[index]}")
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(all = 15.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = it.number,
                                            modifier = Modifier.padding(PaddingValues(start = 5.dp)),
                                            fontSize = TextUnit(4.0F, TextUnitType.Em),
                                            textAlign = TextAlign.Left,
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = it.province,
                                            modifier = Modifier.padding(PaddingValues(start = 35.dp)),
                                            fontSize = TextUnit(4.0F, TextUnitType.Em)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = it.city,
                                            modifier = Modifier.padding(PaddingValues(start = 55.dp)),
                                            fontSize = TextUnit(4.0F, TextUnitType.Em)
                                        )
                                    }


                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 15.dp, top = 0.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Icon(
                                            painter = painterResource(ISPEnum.getByNameCn(it.isp).icon),
                                            contentDescription = "hello",
                                            modifier = Modifier.padding(PaddingValues(start = 5.dp))
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = it.isp,
                                            modifier = Modifier.padding(PaddingValues(start = 15.dp)),
                                            fontSize = TextUnit(4.5F, TextUnitType.Em)
                                        )
                                    }
                                }

                            }
                        }
                    }
                },
            )
        }
    }


    /*

        LazyColumn(
            contentPadding = PaddingValues(top = 1.dp, start = 5.dp, bottom = 1.dp)
        ) {

            items(numbers.size) { index ->
                val a = numbers[index]
                ElevatedCard(
                    modifier = Modifier
                        .size(width = 450.dp, height = 95.dp)
                        .clickable(onClick = {
                            shouldShowDialog = !shouldShowDialog; phoneInfo = a
                        })
                        .padding(start = 15.dp, top = 5.dp, end = 5.dp, bottom = 5.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 5.dp,
                        pressedElevation = 10.dp
                    )
                ) {
                    */
    /*Box(
                        modifier = Modifier
                            .fillMaxSize()
                            // .border(0.dp, color = Color.Magenta, shape = RectangleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.LightGray, Color.White)
                                )
                            )
                    ) {*//*

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(all = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = a.number,
                                    modifier = Modifier.padding(PaddingValues(start = 5.dp)),
                                    fontSize = TextUnit(4.0F, TextUnitType.Em),
                                    textAlign = TextAlign.Left,
                                )
                            }
                            Column {
                                Text(
                                    text = a.province,
                                    modifier = Modifier.padding(PaddingValues(start = 35.dp)),
                                    fontSize = TextUnit(4.0F, TextUnitType.Em)
                                )
                            }
                            Column {
                                Text(
                                    text = a.city,
                                    modifier = Modifier.padding(PaddingValues(start = 55.dp)),
                                    fontSize = TextUnit(4.0F, TextUnitType.Em)
                                )
                            }


                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 15.dp, top = 0.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Icon(
                                    painter = painterResource(ISPEnum.getByNameCn(a.isp).icon),
                                    contentDescription = "hello",
                                    modifier = Modifier.padding(PaddingValues(start = 5.dp))
                                )
                            }
                            Column {
                                Text(
                                    text = a.isp,
                                    modifier = Modifier.padding(PaddingValues(start = 15.dp)),
                                    fontSize = TextUnit(4.5F, TextUnitType.Em)
                                )
                            }
                        }

                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(PaddingValues(top = 2.dp)),
                        thickness = 0.dp
                    )

                //}

            }
        }
    }
*/


}

