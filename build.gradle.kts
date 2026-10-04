import org.gradle.api.tasks.Exec

plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.kapt") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

allprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}

tasks.register<Exec>("generateGoBridge") {
    workingDir(rootDir)
    commandLine("bash", "scripts/build-android-bridge.sh")
}
