import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

/**
 * Android fica HABILITADO por padrao. O AGP exige um Android SDK ja na fase de configuracao;
 * em maquinas sem SDK use `-PdisableAndroid=true` (ver README). Nesse modo o plugin
 * `com.android.kotlin.multiplatform.library` e o target Android simplesmente nao sao aplicados
 * (e o modulo `:androidApp` sai do build, ver settings.gradle.kts).
 *
 * Desde o AGP 9 o app Android vive no modulo `:androidApp`; este modulo e uma biblioteca KMP.
 */
val androidEnabled: Boolean = providers.gradleProperty("disableAndroid")
    .map { !it.toBoolean() }
    .getOrElse(true)

if (androidEnabled) {
    apply(plugin = libs.plugins.androidMultiplatformLibrary.get().pluginId)
} else {
    logger.lifecycle("SportConnect: alvo Android DESABILITADO (disableAndroid=true).")
}

// C23: versao do app gerada a partir do gradle.properties (antes "0.1.0" fixo no Perfil).
val appVersionName = providers.gradleProperty("sportconnect.versionName")
val generateAppInfo = tasks.register("generateAppInfo") {
    val outDir = layout.buildDirectory.dir("generated/appinfo/kotlin")
    val version = appVersionName
    inputs.property("versionName", version)
    outputs.dir(outDir)
    doLast {
        val file = outDir.get().file("br/com/sportconnect/AppInfo.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package br.com.sportconnect
            |
            |/** Gerado pelo Gradle (composeApp/build.gradle.kts, tarefa generateAppInfo). Nao edite. */
            |object AppInfo {
            |    const val VERSION_NAME: String = "${version.get()}"
            |}
            |""".trimMargin(),
        )
    }
}

kotlin {
    sourceSets.commonMain { kotlin.srcDir(generateAppInfo) }
    if (androidEnabled) {
        // Equivale a `androidLibrary { }`; o plugin e aplicado condicionalmente, entao sem accessor tipado.
        (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("androidLibrary") {
            namespace = "br.com.sportconnect.shared"
            compileSdk = libs.versions.android.compileSdk.get().toInt()
            minSdk = libs.versions.android.minSdk.get().toInt()
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
            androidResources {
                enable = true
            }
            // Roda o commonTest tambem na JVM do Android (`:composeApp:testAndroidHostTest`).
            withHostTest {}
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static(rootDirPath)
                    static(projectDirPath)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain = getByName("desktopMain")

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.noArg)

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
        }
        commonTest.dependencies {
            implementation(libs.multiplatform.settings.test)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        if (androidEnabled) {
            androidMain.dependencies {
                // Pedido de permissao (localizacao) a partir do Compose.
                implementation(libs.androidx.activity.compose)
                implementation(libs.kotlinx.coroutines.android)
                // Login com Google (B8, atras de FeatureFlags.SOCIAL_LOGIN).
                implementation(libs.androidx.credentials)
                implementation(libs.androidx.credentials.play.services)
                implementation(libs.googleid)
                implementation(libs.ktor.client.okhttp)
            }
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

if (androidEnabled) {
    dependencies {
        // O plugin Android-KMP nao tem build variants: tooling do Compose vai para o runtime classpath.
        "androidRuntimeClasspath"(libs.compose.ui.tooling)
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "br.com.sportconnect.resources"
    generateResClass = always
}

compose.desktop {
    application {
        mainClass = "br.com.sportconnect.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SportConnect"
            packageVersion = "1.0.0"
            description = "SportConnect - esportes amadores, quadras e rateio"
        }
    }
}
