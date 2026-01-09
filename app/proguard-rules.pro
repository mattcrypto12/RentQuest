# Add project specific ProGuard rules here.

# Keep Solana Mobile Wallet Adapter
-keep class com.solana.mobilewalletadapter.** { *; }
-keep class com.solanamobile.** { *; }

# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.rentquest.app.**$$serializer { *; }
-keepclassmembers class com.rentquest.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.rentquest.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# NovaCrypto Base58
-keep class io.github.novacrypto.** { *; }
