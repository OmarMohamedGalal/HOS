package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.navigation.HosNavHost
import com.example.ui.theme.HosSurface
import com.example.ui.theme.HosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            HosApp()
        }
    }
}

@Composable
fun HosApp() {
    HosTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = HosSurface
        ) {
            HosNavHost()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HosAppPreview() {
    HosApp()
}
