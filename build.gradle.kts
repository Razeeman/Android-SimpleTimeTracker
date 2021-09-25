// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter() // jcenter will keep repos indefinitely and some repos wasn't moved yet (ex. flexbox)
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath(com.example.util.simpletimetracker.BuildPlugins.gradle)
        classpath(com.example.util.simpletimetracker.BuildPlugins.kotlin)
        classpath(com.example.util.simpletimetracker.BuildPlugins.ktlint)
        classpath(com.example.util.simpletimetracker.BuildPlugins.hilt)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}
