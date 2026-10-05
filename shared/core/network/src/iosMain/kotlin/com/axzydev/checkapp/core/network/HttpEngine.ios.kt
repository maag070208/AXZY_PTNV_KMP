package com.axzydev.checkapp.core.network

import io.ktor.client.engine.darwin.Darwin

actual fun createHttpEngine() = Darwin.create()
