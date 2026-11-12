# Add project specific ProGuard rules here.

# Preserve Annotations, Signatures and Inner Classes for reflection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Preserve Gson @SerializedName annotations and fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Preserve all Remote Data Transfer Objects (DTOs)
-keep class com.goodwill.app.data.remote.dto.** { *; }

# Preserve Retrofit API Service interface and methods
-keep interface com.goodwill.app.data.remote.ApiService { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Preserve OkHttp and Logging Interceptor
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
