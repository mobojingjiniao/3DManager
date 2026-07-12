package com.threed.manager.feature.avatars.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.threed.manager.core.design.components.AuroraBackground
import com.threed.manager.core.design.components.AuroraBottomBar
import com.threed.manager.core.design.components.AuroraCard
import com.threed.manager.core.design.components.AuroraChip
import com.threed.manager.core.design.components.AuroraFloatingAction
import com.threed.manager.core.design.components.AuroraTopBar
import com.threed.manager.core.design.components.ChipTone
import com.threed.manager.core.design.theme.AuroraTheme

/**
 * Settings — Phase 0 stub. Real implementation will bind to DataStore-backed
 * PreferencesRepository (see master plan Phase 8).
 */
@Composable
fun SettingsScreen(onTabNavigate: (String) -> Unit = {}) {
    val colors = AuroraTheme.colors
    var reduceMotion by remember { mutableStateOf(false) }
    var boldText by remember { mutableStateOf(false) }
    var invertX by remember { mutableStateOf(false) }

    AuroraBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            AuroraTopBar(title = "Settings")

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)) {

                SectionHeader("RENDER")
                AuroraCard(modifier = Modifier.fillMaxWidth()) {
                    SettingsRow("Backend", value = "Web Spark", isChip = true, chipTone = ChipTone.Violet)
                    SettingsRow("FPS target", value = "Auto", isChip = true, chipTone = ChipTone.Green)
                }
                Spacer(Modifier.height(12.dp))

                SectionHeader("SENSORS")
                AuroraCard(modifier = Modifier.fillMaxWidth()) {
                    SettingsRow("Sensitivity", value = "1.0", isChip = true, chipTone = ChipTone.Green)
                    SettingsRow("Invert X", value = if (invertX) "On" else "Off", isChip = true, chipTone = if (invertX) ChipTone.Green else ChipTone.Neutral)
                }
                Spacer(Modifier.height(12.dp))

                SectionHeader("ACCESSIBILITY")
                AuroraCard(modifier = Modifier.fillMaxWidth()) {
                    SettingsRow("Reduce motion", value = if (reduceMotion) "On" else "Off", isChip = true, chipTone = if (reduceMotion) ChipTone.Green else ChipTone.Neutral)
                    SettingsRow("Bold text", value = if (boldText) "On" else "Off", isChip = true, chipTone = if (boldText) ChipTone.Green else ChipTone.Neutral)
                }
                Spacer(Modifier.height(12.dp))

                SectionHeader("ABOUT")
                AuroraCard(modifier = Modifier.fillMaxWidth()) {
                    Text("v0.3.0 · build abc1234", color = colors.TextPrimary, style = AuroraTheme.type.bodyMd.copy(fontWeight = FontWeight.SemiBold))
                    Text("FormScan 0.9.2 · Porin SDK 1.4.0", color = colors.TextMono, style = AuroraTheme.type.monoSm)
                }

                Spacer(Modifier.weight(1f))
                AuroraBottomBar(
                    currentRoute = "settings",
                    onNavigate = onTabNavigate,
                    fab = { AuroraFloatingAction(onClick = {}) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    val colors = AuroraTheme.colors
    Text(
        text = label,
        color = colors.TextMono,
        style = AuroraTheme.type.overline,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SettingsRow(label: String, value: String, isChip: Boolean, chipTone: ChipTone) {
    val colors = AuroraTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(label, color = colors.TextPrimary, style = AuroraTheme.type.bodyMd)
        if (isChip) {
            AuroraChip(text = value, tone = chipTone, selected = true)
        } else {
            Text(value, color = colors.TextSecondary, style = AuroraTheme.type.bodyMd)
        }
    }
}