package com.example.util.simpletimetracker

object Deps {
    const val kotlin =
        "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val coroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    const val javax =
        "javax.inject:javax.inject:${Versions.javax}"
    const val timber =
        "com.jakewharton.timber:timber:${Versions.timber}"

    object Androidx {
        const val appcompat =
            "androidx.appcompat:appcompat:${Versions.appcompat}"
        const val constraintLayout =
            "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
        const val recyclerView =
            "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
        const val lifecycleExtensions =
            "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycleExtensions}"
        const val room =
            "androidx.room:room-runtime:${Versions.room}"
        const val viewpager2 =
            "androidx.viewpager2:viewpager2:${Versions.viewpager2}"
        const val cardView =
            "androidx.cardview:cardview:${Versions.cardView}"
        const val material =
            "com.google.android.material:material:${Versions.material}"
    }

    object Google {
        const val dagger =
            "com.google.dagger:dagger:${Versions.dagger}"
        const val flexBox =
            "com.google.android:flexbox:${Versions.flexBox}"
    }

    object Ktx {
        const val core =
            "androidx.core:core-ktx:${Versions.coreKtx}"
        const val fragment =
            "androidx.fragment:fragment-ktx:${Versions.fragmentKtx}"
        const val liveDataCore =
            "androidx.lifecycle:lifecycle-livedata-core-ktx:${Versions.liveDataCoreKtx}"
        const val liveData =
            "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.liveDataKtx}"
        const val viewModel =
            "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.viewModelKtx}"
        const val room =
            "androidx.room:room-ktx:${Versions.roomKtx}"
        const val navigationFragment =
            "androidx.navigation:navigation-fragment-ktx:${Versions.navigationKtx}"
        const val navigationUi =
            "androidx.navigation:navigation-ui-ktx:${Versions.navigationKtx}"
    }

    object Kapt {
        const val room =
            "androidx.room:room-compiler:${Versions.room}"
        const val dagger =
            "com.google.dagger:dagger-compiler:${Versions.dagger}"
    }

    object Test {
        const val junit =
            "junit:junit:${Versions.junit}"
    }

    object UiTest {
        const val junit =
            "androidx.test.ext:junit:${Versions.junitUi}"
        const val espresso =
            "androidx.test.espresso:espresso-core:${Versions.espresso}"
    }
}