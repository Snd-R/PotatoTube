plugins {
    kotlin("multiplatform")
}

group = "org.snd"
version = "unspecified"

repositories {
    mavenCentral()
}

kotlin {
    jvm { withJava() }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("net.java.dev.jna:jna:5.13.0")

                implementation("org.slf4j:slf4j-api:2.0.7")
                implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

            }
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "19"
    targetCompatibility = "19"
    options.compilerArgs.add("--enable-preview")
}
