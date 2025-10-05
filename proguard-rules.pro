# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.dharmabit.hikemate.data.database.entities.** { *; }

# Keep Gson classes
-keep class com.google.gson.** { *; }

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }

# Keep location services
-keep class android.location.** { *; }