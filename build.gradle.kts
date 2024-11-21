// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
buildscript {
    repositories {
        google()  // Ensure Google's Maven repository is included
        mavenCentral()
    }
    dependencies {
        // Add the Google services plugin classpath
        classpath("com.google.gms:google-services:4.4.2")  // Use the latest version
    }
}