package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetEntity
import com.example.ui.theme.LocalCustomColors
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.VuRed

@Composable
fun PresetSelectorBar(
    presets: List<PresetEntity>,
    selectedPresetName: String,
    isBypassed: Boolean,
    onPresetSelected: (PresetEntity) -> Unit,
    onSaveCustomPreset: (String) -> Unit,
    onRenameCustomPreset: (Int, String) -> Unit = { _, _ -> },
    onDeleteCustomPreset: (Int) -> Unit,
    onResetFlat: () -> Unit,
    onToggleBypass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current
    val primaryColor = customColors.accent
    val cardBg = customColors.cardBg
    val borderCol = customColors.border
    val controlBg = customColors.controlBg
    val accentContainer = customColors.accentContainer

    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var presetToRename by remember { mutableStateOf<PresetEntity?>(null) }
    var renamePresetText by remember { mutableStateOf("") }
    var presetToDelete by remember { mutableStateOf<PresetEntity?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(28.dp))
            .padding(16.dp)
            .testTag("preset_selector_bar")
    ) {
        // Actions row: Title, Save Preset, Flat Reset, A/B Compare
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Presets",
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "PRESETS & PROFILES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = customColors.textSecondary,
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // A/B Compare Button
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBypassed) accentContainer else controlBg)
                        .border(
                            0.5.dp,
                            if (isBypassed) primaryColor else borderCol,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleBypass() }
                        .padding(horizontal = 8.dp)
                        .testTag("ab_compare_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "A/B Compare",
                            tint = if (isBypassed) primaryColor else customColors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isBypassed) "BYPASS (RAW)" else "A/B COMPARE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBypassed) primaryColor else customColors.textSecondary
                        )
                    }
                }

                // Quick Flat Reset Button
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(controlBg)
                        .border(0.5.dp, borderCol, RoundedCornerShape(8.dp))
                        .clickable { onResetFlat() }
                        .padding(horizontal = 8.dp)
                        .testTag("reset_flat_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Flat",
                            tint = customColors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "FLAT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = customColors.textSecondary
                        )
                    }
                }

                // Save Preset Button
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentContainer)
                        .border(0.5.dp, primaryColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable { showSaveDialog = true }
                        .padding(horizontal = 8.dp)
                        .testTag("save_preset_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Save Preset",
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "SAVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Chips list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { preset ->
                val isSelected = preset.name.equals(selectedPresetName, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) accentContainer else controlBg)
                        .border(
                            0.8.dp,
                            if (isSelected) primaryColor else borderCol,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onPresetSelected(preset) }
                        .padding(horizontal = 12.dp)
                        .testTag("preset_chip_${preset.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = primaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = preset.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) primaryColor else (if (preset.isCustom) SoftViolet else customColors.textPrimary)
                        )

                        if (preset.isCustom) {
                            // Rename action icon
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename Preset",
                                tint = customColors.textMuted,
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable {
                                        presetToRename = preset
                                        renamePresetText = preset.name
                                    }
                            )

                            // Delete action icon
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Preset",
                                tint = customColors.textMuted,
                                modifier = Modifier
                                    .size(13.dp)
                                    .clickable { presetToDelete = preset }
                            )
                        }
                    }
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Custom Preset",
                    fontWeight = FontWeight.Bold,
                    color = customColors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter a name for your current 12-band EQ, Bass, Gain, and Stereo profile:",
                        fontSize = 13.sp,
                        color = customColors.textSecondary
                    )
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        placeholder = { Text("e.g. Deep Bass Boost, Stage Vocal", color = customColors.textMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = borderCol,
                            focusedTextColor = customColors.textPrimary,
                            unfocusedTextColor = customColors.textPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preset_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            onSaveCustomPreset(newPresetName.trim())
                            newPresetName = ""
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = customColors.accentContainer),
                    modifier = Modifier.testTag("confirm_save_preset")
                ) {
                    Text("Save Preset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = customColors.textSecondary)
                }
            },
            containerColor = cardBg,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Rename Preset Dialog
    presetToRename?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToRename = null },
            title = {
                Text(
                    text = "Rename Custom Preset",
                    fontWeight = FontWeight.Bold,
                    color = customColors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter a new name for \"${preset.name}\":",
                        fontSize = 13.sp,
                        color = customColors.textSecondary
                    )
                    OutlinedTextField(
                        value = renamePresetText,
                        onValueChange = { renamePresetText = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = borderCol,
                            focusedTextColor = customColors.textPrimary,
                            unfocusedTextColor = customColors.textPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rename_preset_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renamePresetText.isNotBlank()) {
                            onRenameCustomPreset(preset.id, renamePresetText.trim())
                            presetToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = customColors.accentContainer)
                ) {
                    Text("Rename", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToRename = null }) {
                    Text("Cancel", color = customColors.textSecondary)
                }
            },
            containerColor = cardBg,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Delete Confirmation Dialog
    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = {
                Text(
                    text = "Delete Preset",
                    fontWeight = FontWeight.Bold,
                    color = customColors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete custom preset \"${preset.name}\"?",
                    color = customColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomPreset(preset.id)
                        presetToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VuRed, contentColor = Color.White)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text("Cancel", color = customColors.textSecondary)
                }
            },
            containerColor = cardBg,
            shape = RoundedCornerShape(24.dp)
        )
    }
}


