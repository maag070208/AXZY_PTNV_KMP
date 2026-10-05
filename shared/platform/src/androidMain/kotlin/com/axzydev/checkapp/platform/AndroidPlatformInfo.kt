package com.axzydev.checkapp.platform

import android.os.Build

class AndroidPlatformInfo : PlatformInfo {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}
