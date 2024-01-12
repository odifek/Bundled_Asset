package com.techbeloved.bundledasset

import okio.HashingSink
import okio.blackholeSink
import okio.buffer
import okio.use

/**
 * File hashing is handled by okio. See [Okio recipes](https://square.github.io/okio/recipes/#hashing-javakotlin)
 */
class AssetFileHashingUseCase(private val assetFileProvider: AssetFileProvider = defaultAssetFileProvider) {
    operator fun invoke(assetFilePath: String): FileHash {
        val hash = HashingSink.sha256(blackholeSink()).use { hashingSink ->
            assetFileProvider.get(assetFilePath).buffer().use { source ->
                source.readAll(hashingSink)
                hashingSink.hash.hex()
            }
        }
        return FileHash(path = assetFilePath, sha256 = hash)
    }
}