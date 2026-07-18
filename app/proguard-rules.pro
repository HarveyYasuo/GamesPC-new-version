# Proguard rules for Retrofit, OkHttp, and Kotlinx Serialization
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
# By default, the flags in this file are appended to flags specified
# by Android Gradle plugin. See
# https://developer.android.com/studio/build/shrink-code.html#keep-code

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Reglas para Retrofit, OkHttp y Okio ---
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn retrofit2.Platform$Java8

# --- Reglas para Kotlinx Serialization ---
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class ** {
    @kotlinx.serialization.Transient <fields>;
}
-if @kotlinx.serialization.Serializable class ** { *; }
-keep class <1> { *; }
-if @kotlinx.serialization.Serializable class ** {
    public static ** Companion;
}
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-dontnote kotlinx.serialization.SerializationKt

# --- Reglas para tus clases de modelo (Data classes) ---
# Reemplaza "com.harvey.gamespc.data.model" si tus modelos están en otro paquete.
-keep class com.harvey.gamespc.data.model.** { *; }
-keep class com.harvey.gamespc.data.** { *; }
-keep class com.harvey.gamespc.data.remote.** { *; }

# Castar SDK & Go Rules


# Regla adicional para evitar la ofuscación de nombres de clases, métodos y campos del SDK.
# Esto es crucial si el código nativo busca clases de Java por su nombre.


# --- Reglas para ExoPlayer / Media3 --- 
# Rules for ExoPlayer (Media3) to prevent issues with R8/ProGuard in release builds


# Keep all Parcelable implementations
-keep class * implements android.os.Parcelable { 
  public static final android.os.Parcelable$Creator *; 
}

# Keep enums
-keepclassmembers enum * { 
    public static **[] values(); 
    public static ** valueOf(java.lang.String); 
}

# Keep the Message data class and its fields from being obfuscated
-keep class com.harvey.gamespc.ui.screens.Message { *; }
-keepclassmembers class com.harvey.gamespc.ui.screens.Message {
    <fields>;
}

# Keep the MessageStatus enum and its values
-keep enum com.harvey.gamespc.ui.screens.MessageStatus {
    <fields>;
    <methods>;
}

# Rules for Firebase Realtime Database (general)
# See https://firebase.google.com/docs/android/setup#r8
-keepclassmembers class com.google.firebase.database.DataSnapshot {
  <init>(...);
}
-keep class com.google.firebase.database.GenericTypeIndicator
-keep class com.google.firebase.database.GenericTypeIndicator { <init>(...); }

# --- Unity Ads Mediation Rules ---
# Essential for Unity Ads SDK and its mediation adapter
-keep class com.unity3d.ads.** { *; }
-keep class com.unity3d.services.** { *; }
-keep class com.google.ads.mediation.unity.** { *; }

# Keep all JNI-accessible methods
-keepclasseswithmembers class * {
    native <methods>;
}

# --- Hilt (Dagger) Rules for R8 Full Mode ---
# Retain generic type information and annotations
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# Ensure Hilt's generated entry points are not stripped or renamed
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.AndroidEntryPoint class *

# Explicitly keep constructors for Hilt-injected classes and ViewModels
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# General optimization rules for better crash reporting
-keepattributes SourceFile, LineNumberTable
