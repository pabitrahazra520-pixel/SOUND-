package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepVioletContainer
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioControlBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SystemSessionDialog(
    currentSessionId: Int,
    onDismiss: () -> Unit,
    onSetSessionId: (Int) -> Unit
) {
    var inputSessionId by remember { mutableStateOf(if (currentSessionId == 0) "0" else currentSessionId.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsInputComponent,
                    contentDescription = null,
                    tint = LavenderAccent,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Audio Session Routing",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("audio_session_dialog")
            ) {
                Text(
                    text = "The 12-Band Equalizer, Bass Boost, and Master Gain processor can attach directly to Android system audio output or external players.",
                    fontSize = 12.5.sp,
                    color = TextSecondary
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioControlBg)
                        .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = LavenderAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Session 0: Global Output (Default)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LavenderAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Applies 12-Band EQ, Bass Boost, and Pre-Amp Gain system-wide to all audio streams, music players (Spotify, YouTube Music), and games.",
                        fontSize = 10.5.sp,
                        color = TextMuted
                    )
                }

                OutlinedTextField(
                    value = inputSessionId,
                    onValueChange = { inputSessionId = it },
                    label = { Text("Audio Session ID", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LavenderAccent,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val id = inputSessionId.toIntOrNull() ?: 0
                    onSetSessionId(id)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = DeepVioletContainer)
            ) {
                Text("Apply Route", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = StudioCardBg,
        shape = RoundedCornerShape(24.dp)
    )
}

