package com.techbeloved.bundledasset

class Greeting {
    private val platform: Platform = getPlatform()
    private val assetFileHashingUseCase = AssetFileHashingUseCase()

    fun greet(): String {
        return """Hello, ${platform.name}!
            |Bundled asset file hash: ${assetFileHashingUseCase("assets/lyrics/ten_thousand_reason.xml")}
        """.trimMargin()
    }
}