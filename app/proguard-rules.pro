# Proguard rules for clockdin
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
