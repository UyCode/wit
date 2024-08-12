package com.uycode.wit

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.util.LruCache
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.uycode.wit.nav.CustomNavHost
import com.uycode.wit.nav.navigateAndPopUp
import com.uycode.wit.server.PhoneStateReceiver
import com.uycode.wit.server.PhoneStateService
import com.uycode.wit.ui.theme.WITTheme


const val BLURRED_BG_RECENT = "BLURRED_BG_RECENT"
const val BLURRED_BG_SEARCH = "BLURRED_BG_SEARCH"
const val BLURRED_BG_CONTACT = "BLURRED_BG_CONTACT"
const val BLUR_RADIUS = 50

class MainActivity : ComponentActivity() {

    lateinit var memoryCache: LruCache<String, Bitmap>


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        INSTANCE = this

        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }


        setContent {
            WITTheme {
                val navController = rememberNavController()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = topAppBarColors(
                                containerColor = Color.White,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),

                            title = {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Date Range"
                                )
                                Text(modifier = Modifier.padding(start = 45.dp), text = "Who Is This", color = Color.Blue)
                            }
                        )
                    },
                    bottomBar = {

                        val width = LocalConfiguration.current.screenWidthDp.dp
                        BottomAppBar(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(height = 95.dp, width = width)
                        ) {

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 15.dp)
                                        ) {
                                            ElevatedButton(onClick = {
                                                navController.navigateAndPopUp(Screens.RECENT.route)
                                            }) {
                                                Icon(
                                                    imageVector = Screens.RECENT.icon,
                                                    contentDescription = Screens.RECENT.title
                                                )
                                                Text(text = Screens.RECENT.title, fontSize = TextUnit(13f, TextUnitType.Sp))
                                            }

                                        }
                                        Column(modifier = Modifier.weight(1f).padding(start = 15.dp)) {
                                            ElevatedButton(onClick = {
                                                navController.navigateAndPopUp(Screens.SEARCH.route)
                                            }) {
                                                Icon(
                                                    imageVector = Screens.SEARCH.icon,
                                                    contentDescription = Screens.SEARCH.title
                                                )
                                                Text(text = Screens.SEARCH.title,fontSize = TextUnit(13f, TextUnitType.Sp))
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f).padding(start = 15.dp)) {
                                            ElevatedButton(onClick = {
                                                navController.navigateAndPopUp(Screens.CONTACT.route)
                                            }) {
                                                Icon(
                                                    imageVector = Screens.CONTACT.icon,
                                                    contentDescription = Screens.CONTACT.title
                                                )
                                                Text(text = Screens.CONTACT.title,fontSize = TextUnit(11f, TextUnitType.Sp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    CustomNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )

                }
            }
        }

        startPhoneStateService()
        PhoneStateReceiver.initializeLookup(baseContext)
    }

    private fun startPhoneStateService() {
        val serviceIntent = Intent(this, PhoneStateService::class.java)
        Log.d("PhoneStateTest", "Starting phone state service")
        startForegroundService(serviceIntent)

        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

    }


    companion object {
        private lateinit var INSTANCE: MainActivity
        fun getInstance() = INSTANCE
    }

}
