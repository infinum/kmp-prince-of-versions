package com.infinum.princeofversions

import platform.UIKit.UIDevice

private class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

public actual fun getPlatform(): Platform = IOSPlatform()