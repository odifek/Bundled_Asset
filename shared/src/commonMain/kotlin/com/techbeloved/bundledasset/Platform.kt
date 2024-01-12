package com.techbeloved.bundledasset

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform