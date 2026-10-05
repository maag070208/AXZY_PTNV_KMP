package com.axzydev.checkapp

import androidx.compose.ui.window.ComposeUIViewController
import com.axzydev.checkapp.app.di.initKoin
import com.axzydev.checkapp.app.di.isKoinStarted

fun MainViewController() = run {
    if (!isKoinStarted()) {
        initKoin()
    }
    ComposeUIViewController { App() }
}
