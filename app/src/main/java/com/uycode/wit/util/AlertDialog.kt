package com.uycode.wit.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.uycode.wit.ISPEnum
import com.uycode.wit.PhoneInfo


@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier
            .fillMaxWidth()
            .height(325.dp),
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            OutlinedButton(
                modifier = Modifier.padding(PaddingValues(0.dp)),
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Call")
            }
        },
        dismissButton = {
            OutlinedButton(
                modifier = Modifier.padding(PaddingValues(0.dp)),
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}


@Composable
fun ShowDialog(phone: PhoneInfo, context: Context, onDismiss: () -> Unit) {
    if (phone.id == 0) return

    AlertDialogExample(
        onDismissRequest = { onDismiss() },
        onConfirmation = {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phone.number}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onDismiss()
        },
        dialogTitle = phone.number,
        dialogText = "省份：${phone.province}        城市：${phone.city}\n\n运营商：${phone.isp}\n\n邮编：${phone.zip}\n\n区号：${phone.areaCode}",
        icon = ImageVector.vectorResource(id = ISPEnum.getByNameCn(phone.isp).icon)
    )
}