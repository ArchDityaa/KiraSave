# Room and OkHttp ship their own consumer rules. Keep model names used by Room only.
-keep class com.hakira.kirasave.data.** { *; }
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
