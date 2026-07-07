package com.threed.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threed.manager.core.design.Theme

// Phase 0 / verification screen.
//
// Intentionally minimal: a single [Text] on a [Surface]. Compose's first
// render is expensive on software-rendered emulators (no hardware GLES 2.0)
// and any heavy layout triggers ANR.
//
// Real UI lives in feature/* Compose screens (Phase 1+).
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF101112)) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "3DManager", color = Color.White, fontSize = 32.sp)
                    }
                }
            }
        }
    }
}