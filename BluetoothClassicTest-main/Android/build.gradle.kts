buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0")  // 最新のバージョンに更新
        classpath("com.google.gms:google-services:4.3.15")  // FirebaseやGoogleサービス
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    }
}

plugins {
    id("com.android.application") version "8.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false  // Kotlinのバージョンを更新
    id("com.google.gms.google-services") version "4.4.2" apply false
}
