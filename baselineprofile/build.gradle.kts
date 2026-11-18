plugins {
    alias(libs.plugins.androidTest)
    alias(libs.plugins.baselineprofile)
}

/**
 * Gera o Baseline Profile do :androidApp (A3 do backlog 11). Roda a jornada de BaselineProfileGenerator
 * num aparelho/emulador conectado: `./gradlew :androidApp:generateBaselineProfile`.
 */
android {
    namespace = "br.com.sportconnect.baselineprofile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 28
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    targetProjectPath = ":androidApp"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
