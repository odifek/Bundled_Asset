Bundle and Read assets as resources in kotlin multiplatform (Android, iOS and desktop)
===============================================================

After struggling for a while with this topic, I was able to arrive at an easy-to-implement solution for my use case.
Here the focus is on Android, iOS and desktop shared multiplatform. I did not attempt to implement it for JS/webassembly

#### [Compose multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
Also the solution requires enabling compose multiplatform (gradle plugin) in the shared common code. Again, I attempted to do without this but could not figure out the iOS resources. 
It would appear that `compose.components.resources` dependency has a useful task that makes this easy. So you have to add it otherwise, this solution would not work.

#### [Okio](https://square.github.io/okio/)
_Okio is a library that complements java.io and java.nio to make it much easier to access, store, and process your data_
Fortunately, it is kotlin multiplatform enabled.

We use it here to read files, hash files, etc.

Let's start with the shared `build.gradle.kts` file. I will highlight just the important parts. You can see the project for full details.
1. Enable Compose multiplatform.
2. Add dependencies (okio, compose resources, etc)
3. Add `commonMain/resources/assets` to android sourceSets. This would ensure that contents of this directory is copied at build time into android apk assets folder. Very important!
4. Create resources/assets folders in commonMain and add the asset files you want to share/read

```kotlin
plugins {
    // 1..
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrains.compose)
    // ...
}
kotlin {
    applyDefaultHierarchyTemplate()
    // ...
    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
            // 2..
            implementation(libs.squareup.okio)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            // ...
        }
        val desktopMain by getting {
            dependencies {
                // Compose is used to build the desktop UI
                implementation(compose.desktop.common)
                // ...
            }
        }
        // ...
    }
}
android {
    sourceSets["main"].apply {
        res.srcDirs("src/androidMain/res", "src/commonMain/resources/res")
        // 3..
        assets.srcDirs("src/commonMain/resources/assets")
    }
}

```

#### Reading the asset file
We can leverage the [kotlin expect/actual construct](https://kotlinlang.org/docs/multiplatform-expect-actual.html)

Create an `AssetFileProvider` interface and the corresponding expect variable. See the sample code
1. `defaultAssetFileProvider` would be created by the respective actual platforms
2. `AssetFileProvider` provides a common interface that would be implemented in the platform specific code
```kotlin
import okio.Source

// 1..
expect val defaultAssetFileProvider: AssetFileProvider
// 2..
/**
 * Exposes a single method that returns [okio.Source] with which we can read/stream the asset file
 * The path is the assets/filepath of the file stored in resources/assets
 */
fun interface AssetFileProvider {
    fun get(path: String): Source
}
```

##### Android implementation of `AssetFileProvider`
File: `AssetFileProvider.android.kt`
The comments explains it. Nothing else to add.

```kotlin
import okio.source

// For android, we have to access it the usual way assets files are accessed.
// Okio provides a simple extension to convert an InputStream to a Source.
actual val defaultAssetFileProvider: AssetFileProvider = AssetFileProvider { path ->
    AndroidInjector.application.assets.open(
        path.removePrefix("assets/")
    ).source()
}
```

##### Ios implementation of `AssetFileProvider`
File: `AssetFileProvider.ios.kt`

```kotlin
// bundled resources are kept in compose-resources path in iOS framework.
// See org.jetbrains.compose.resources.Resource.ios.kt
// This is only possible when we add compose resources dependency. Otherwise, I have not found an easy way to do it.
actual val defaultAssetFileProvider: AssetFileProvider = AssetFileProvider { path ->
    val assetFile = NSBundle.mainBundle.resourcePath + "/compose-resources/" + path
    FileSystem.SYSTEM.source(assetFile.toPath())
}
```

##### desktop implementation of `AssetFileProvider`
File: `AssetFileProvider.desktop.kt`

```kotlin
import okio.FileSystem
import okio.Path.Companion.toPath

// Okio provides a dedicated RESOURCES filesystem for accessing resources bundled in the jar file.
actual val defaultAssetFileProvider: AssetFileProvider = AssetFileProvider { path ->
    FileSystem.RESOURCES.source(path.toPath())
}
```

#### [Use the asset file - Hashing with okio](https://square.github.io/okio/recipes/#hashing-javakotlin) 

I created a [usecase](https://www.javatpoint.com/use-case-model) for this. 
```kotlin
import okio.HashingSink
import okio.blackholeSink
import okio.buffer
import okio.use
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
```

The AssetFileHashingUseCase is used like follows. See the Greetings.kt for full details

```kotlin
private val assetFileHashingUseCase = AssetFileHashingUseCase()
val hashFile = assetFileHashingUseCase("assets/lyrics/ten_thousand_reason.xml")
println("Asset file hash: ${hashFile.sha256}")
```

#### In summary
We are able access the bundled assets resource using the various constructs afforded us by kotlin compose multiplatform and okio libraries.
I hope you found this article and code samples useful for your project. Please subscribe and share. 
Thanks

#### Links to other useful resources on this topic
https://kotlinlang.slack.com/archives/C3PQML5NU/p1679358609312009?thread_ts=1678333015.534759&cid=C3PQML5NU

https://www.netguru.com/blog/kotlin-multiplatform-resources

https://www.youtube.com/watch?v=xtWzpLtCuY0

https://java73.medium.com/simple-way-to-use-common-resources-in-kotlin-multi-platform-project-95a3f886c6d9