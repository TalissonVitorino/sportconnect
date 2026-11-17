plugins {
    // Os plugins sao declarados aqui com `apply false` para que fiquem no mesmo classloader
    // e possam ser aplicados pelos subprojetos.
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
}
