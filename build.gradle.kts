plugins {
    // Os plugins sao declarados aqui com `apply false` para que fiquem no mesmo classloader
    // e possam ser aplicados pelos subprojetos.
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.androidTest) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
}

/**
 * Q1 (doc 13): checa as 4 plataformas num comando so — testes compartilhados (JVM), app Android,
 * framework iOS (simulador), desktop e web. Rodar antes de cada commit: `./gradlew checkAll`.
 */
tasks.register("checkAll") {
    group = "verification"
    description = "Testes + builds de Android, iOS (simulador), desktop e web."
    dependsOn(
        ":composeApp:desktopTest",
        ":androidApp:assembleDebug",
        ":composeApp:linkDebugFrameworkIosSimulatorArm64",
        ":composeApp:compileKotlinDesktop",
        ":composeApp:wasmJsBrowserDistribution",
    )
}
