# SynC App ProGuard Rules

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, AnnotationDefault
-keepclassmembers class retrofit2.BuiltInConverters$* { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Gson
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.TypeAdapter
-keep class com.example.sync.data.model.** { *; }

# Jetpack DataStore
-keep class androidx.datastore.** { *; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**
