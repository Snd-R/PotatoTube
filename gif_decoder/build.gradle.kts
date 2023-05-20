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
                implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
            }
        }
    }
}
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}
