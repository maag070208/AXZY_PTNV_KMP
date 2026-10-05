package com.axzydev.checkapp.core.network

import io.ktor.client.engine.okhttp.OkHttp

actual fun createHttpEngine() = OkHttp.create()
