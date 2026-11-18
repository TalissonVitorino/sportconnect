# SportConnect — regras do R8 para o build release.

# Rotas type-safe da navegação (kotlinx.serialization): o NavHost encontra o serializer por reflexão.
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-keep,includedescriptorclasses class br.com.sportconnect.**$$serializer { *; }
-keepclassmembers class br.com.sportconnect.** {
    *** Companion;
}
-keepclasseswithmembers class br.com.sportconnect.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class br.com.sportconnect.** { *; }

# Ktor / OkHttp: classes opcionais que não existem no Android.
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
