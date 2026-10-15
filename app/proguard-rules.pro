# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }

# Hilt
-keepclasseswithmembernames class * { @dagger.hilt.* <methods>; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.vk.clockdin.**$$serializer { *; }
-keepclassmembers class com.vk.clockdin.** {
    *** Companion;
}
-keepclasseswithmembers class com.vk.clockdin.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Google Sign-In / Credential Manager
-keep class com.google.android.libraries.identity.googleid.** { *; }