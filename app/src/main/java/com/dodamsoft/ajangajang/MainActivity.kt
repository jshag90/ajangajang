package com.dodamsoft.ajangajang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dodamsoft.ajangajang.di.AppContainer
import com.dodamsoft.ajangajang.ui.nav.RootNavHost
import com.dodamsoft.ajangajang.ui.theme.AjangajangTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            AjangajangTheme {
                RootNavHost()
            }
        }
    }
}
