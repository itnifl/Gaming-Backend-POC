# KryoNet needs reflection
-keep class com.esotericsoftware.** { *; }
-keep class com.example.network.packets.** { *; }

# Keep LibGDX classes
-keep class com.badlogic.** { *; }
-keepclassmembers class com.badlogic.** { *; }

# Don't warn about missing optional dependencies
-dontwarn com.esotericsoftware.**
-dontwarn org.objenesis.**
