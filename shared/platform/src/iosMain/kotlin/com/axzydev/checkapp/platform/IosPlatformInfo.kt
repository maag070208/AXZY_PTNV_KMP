package com.axzydev.checkapp.platform

import platform.UIKit.UIDevice

class IosPlatformInfo : PlatformInfo {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}
