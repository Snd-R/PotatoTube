import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")

    kotlin("plugin.serialization") version "1.8.20"
    id("com.google.devtools.ksp").version("1.8.20-1.0.11")
}

kotlin {
    android()

    jvm("desktop")


    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        }

        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.desktop.components.animatedImage)

                implementation(compose.materialIconsExtended)

                implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

                implementation("org.slf4j:slf4j-api:2.0.7")
                implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

                implementation("com.squareup.okhttp3:okhttp:4.10.0")
                implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
                implementation("com.squareup.moshi:moshi:1.14.0")
                implementation("com.charleskorn.kaml:kaml:0.53.0")

                runtimeOnly("io.socket:socket.io-client:2.1.0")

                implementation("org.apache.commons:commons-text:1.10.0")
                implementation("org.jsoup:jsoup:1.15.4")

            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.1")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.0")

                implementation("io.socket:socket.io-client:2.1.0") {
                    exclude(group = "org.json", module = "json")
                }
                implementation("com.github.tony19:logback-android:3.0.0")
                implementation("io.coil-kt:coil:2.3.0")
                implementation("io.coil-kt:coil-compose:2.3.0")
                implementation("io.coil-kt:coil-gif:2.3.0")
                implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.0.0")
                implementation("androidx.media3:media3-exoplayer:1.0.1")
                implementation("androidx.media3:media3-exoplayer-dash:1.0.1")
                implementation("androidx.media3:media3-ui:1.0.1")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation(compose.desktop.currentOs)

                implementation("ch.qos.logback:logback-core:1.4.6")
                implementation("ch.qos.logback:logback-classic:1.4.6")

                implementation("io.socket:socket.io-client:2.1.0")
                implementation("org.json:json:20230227")

                implementation("com.twelvemonkeys.imageio:imageio-core:3.9.4")
                runtimeOnly("com.twelvemonkeys.imageio:imageio-jpeg:3.9.4")
                implementation("com.madgag:animated-gif-lib:1.4")
                implementation("org.apache.tika:tika-core:2.7.0")

                implementation("com.github.javakeyring:java-keyring:1.0.2")
                implementation("dev.dirs:directories:26")

                implementation("uk.co.caprica:vlcj:4.8.2")

                implementation(project(":mpv"))
                implementation(project(":gif_decoder"))
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.myapplication.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }

    packagingOptions {
        resources.excludes.apply {
            add("META-INF/INDEX.LIST")
        }
    }
}

dependencies {
    add("kspDesktop", "com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
    add("kspAndroid", "com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
}

compose.desktop {
    application {
        mainClass = "org.snd.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "PotatoTube"
            packageVersion = "1.0.0"
            includeAllModules = true

            windows {
                menu = true
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "61DAB35E-17CB-43B0-81D5-B30E1C0830FA"
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn", "-opt-in=kotlin.ExperimentalStdlibApi")
}

tasks.register<Zip>("repackageUberJar") {
    val packageUberJarForCurrentOS = tasks.getByName("packageUberJarForCurrentOS")
    dependsOn(packageUberJarForCurrentOS)
    val file = packageUberJarForCurrentOS.outputs.files.first()
    val output = File(file.parentFile, "${file.nameWithoutExtension}-repacked.jar")
    archiveFileName.set(output.absolutePath)
    destinationDirectory.set(file.parentFile.absoluteFile)
    exclude("META-INF/*.SF")
    exclude("META-INF/*.RSA")
    exclude("META-INF/*.DSA")
    from(project.zipTree(file))
    doLast {
        delete(file)
        output.renameTo(file)
        logger.lifecycle("The repackaged jar is written to ${archiveFile.get().asFile.canonicalPath}")
    }
}
