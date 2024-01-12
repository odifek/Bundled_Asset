package com.techbeloved.bundledasset

actual fun getPlatform(): Platform  = object :Platform {
    override val name: String = "Desktop platform"
}