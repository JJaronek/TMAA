buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.4")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.51" apply false
    // Zde je verze KSP přesně pasující na Kotlin 1.9.0
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
}