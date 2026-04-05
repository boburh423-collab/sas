-verbose

# LibGDX
-keep class com.badlogic.gdx.** { *; }
-keep class com.badlogic.gdx.backends.android.** { *; }
-keepclassmembers class com.badlogic.gdx.backends.android.** { *; }
-keep class com.badlogic.gdx.graphics.** { *; }
-keep class com.badlogic.gdx.math.** { *; }
-keep class com.badlogic.gdx.utils.** { *; }
-keep class com.badlogic.gdx.assets.** { *; }
-keep class com.badlogic.gdx.audio.** { *; }
-keep class com.badlogic.gdx.graphics.g2d.freetype.** { *; }

# Game classes
-keep class com.towerdefense.** { *; }

# Android
-keepattributes *Annotation*
-keepclassmembers class * extends android.app.Activity { public void *(android.view.View); }
-keepclassmembers enum * { public static **[] values(); public static ** valueOf(java.lang.String); }

# Remove debug logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
