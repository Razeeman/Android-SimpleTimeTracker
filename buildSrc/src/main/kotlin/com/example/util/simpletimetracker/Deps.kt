package com.example.util.simpletimetracker

object deps {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    const val javax = "javax.inject:javax.inject:${Versions.javax}"
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    object androidx {
        const val appcompat =
            "androidx.appcompat:appcompat:${Versions.appcompat}"
        const val constraintlayout =
            "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
        const val recyclerview =
            "androidx.recyclerview:recyclerview:${Versions.recyclerview}"
        const val lifecycle_extensions =
            "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle_extensions}"
        const val room =
            "androidx.room:room-runtime:${Versions.room}"
        const val viewpager2 =
            "androidx.viewpager2:viewpager2:${Versions.viewpager2}"
        const val cardView =
            "androidx.cardview:cardview:${Versions.cardView}"
        const val material =
            "com.google.android.material:material:${Versions.material}"
    }

    object google {
        const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
        const val flexBox = "com.google.android:flexbox:${Versions.flexBox}"
    }

    object ktx {
        const val core =
            "androidx.core:core-ktx:${Versions.core_ktx}"
        const val fragment =
            "androidx.fragment:fragment-ktx:${Versions.fragment_ktx}"
        const val livedata_core =
            "androidx.lifecycle:lifecycle-livedata-core-ktx:${Versions.livedata_core_ktx}"
        const val livedata =
            "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.livedata_ktx}"
        const val viewmodel =
            "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.viewmodel_ktx}"
        const val room =
            "androidx.room:room-ktx:${Versions.room_ktx}"
        const val navigation_fragment =
            "androidx.navigation:navigation-fragment-ktx:${Versions.navigation_ktx}"
        const val navigation_ui =
            "androidx.navigation:navigation-ui-ktx:${Versions.navigation_ktx}"
    }

    object kapt {
        const val room = "androidx.room:room-compiler:${Versions.room}"
        const val dagger = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    }

    object test {
        const val junit = "junit:junit:${Versions.junit}"
    }

    object uitest {
        const val junit = "androidx.test.ext:junit:${Versions.junit_ui}"
        const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    }
}