# Squadfy release rules (spec 011, AC-011-01). Most libraries ship consumer rules; these cover the rest.

# kotlinx.serialization: keep generated serializers and companions of @Serializable classes
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class **$$serializer { *; }
-keep,includedescriptorclasses class com.kikepb.**$$serializer { *; }
-keepclassmembers class com.kikepb.** {
    *** Companion;
}
-keepclasseswithmembers class com.kikepb.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Typed navigation routes are @Serializable objects/classes
-keep class com.kikepb.**.navigation.** { *; }
-keep class org.kikepb.squadfy.**.navigation.** { *; }

# Ktor / OkHttp / coroutines: optional JVM classes not present on Android
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn io.ktor.util.debug.**
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keep class io.ktor.serialization.kotlinx.json.** { *; }

# Room: entities and DAOs are generated and referenced reflectively by the database implementation
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Firebase Messaging service declared in the manifest
-keep class org.kikepb.squadfy.push.SquadfyMessagingService { *; }

# Koin (no reflection) and Compose need nothing extra
