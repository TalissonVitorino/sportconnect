plugins {
    // AGP 9 traz Kotlin embutido: nao e preciso aplicar org.jetbrains.kotlin.android.
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "br.com.sportconnect"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "br.com.sportconnect.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            // R8 + remocao de recursos: Compose sem otimizacao e bem mais lento (A2).
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        // Igual ao release, mas assinado com a chave de debug: e o build para avaliar fluidez no aparelho
        // (`./gradlew :androidApp:installBenchmark`). O debug NAO serve para medir desempenho.
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(projects.composeApp)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    // Instala o Baseline Profile no aparelho (A3): codigo das jornadas principais ja vem compilado.
    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(projects.baselineprofile)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    debugImplementation(libs.compose.ui.tooling)
}

// A3: perfil gerado em src/main/generated/baselineProfiles (versionado). Regerar com
// `./gradlew :androidApp:generateBaselineProfile` com um emulador/aparelho conectado (API 33+).
baselineProfile {
    mergeIntoMain = true
}
