package com.axzydev.checkapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform