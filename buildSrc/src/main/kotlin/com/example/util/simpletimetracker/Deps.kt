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
            "com.google.dagger:hilt-android:${Versions.dagger}"
        const val flexBox =
            "com.google.android.flexbox:flexbox:${Versions.flexBox}"
        const val services =
            "com.google.android.gms:play-services-wearable:${Versions.services}"
        const val gson =
            "com.google.code.gson:gson:${Versions.gson}"
        const val desugaring =
            "com.android.tools:desugar_jdk_libs:${Versions.desugaring}"
    }

    object Emoji {
        const val emojiBundled =
            "androidx.emoji2:emoji2-bundled:${Versions.emoji}"
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
            "androidx.room:room-ktx:${Versions.room}"
        const val navigationFragment =
            "androidx.navigation:navigation-fragment-ktx:${Versions.navigationKtx}"
        const val navigationUi =
            "androidx.navigation:navigation-ui-ktx:${Versions.navigationKtx}"
        const val activity =
            "androidx.activity:activity-ktx:${Versions.activityKtx}"
    }

    object Compose {
        const val activity =
            "androidx.activity:activity-compose:${Versions.compose_version}"
        const val ui =
            "androidx.compose.ui:ui:${Versions.compose_version}"
        const val uiToolingPreview =
            "androidx.compose.ui:ui-tooling-preview:${Versions.compose_version}"
        const val materialIcons =
            "androidx.compose.material:material-icons-core:${Versions.compose_icons}"
        const val wearNavigation =
            "androidx.wear.compose:compose-navigation:${Versions.wear_compose_version}"
        const val wearMaterial =
            "androidx.wear.compose:compose-material:${Versions.wear_compose_version}"
        const val wearFoundation =
            "androidx.wear.compose:compose-foundation:${Versions.wear_compose_version}"
        const val wearToolingPreview =
            "androidx.wear:wear-tooling-preview:${Versions.wear_compose_tooling_preview}"
        const val horologist =
            "com.google.android.horologist:horologist-compose-layout:${Versions.horologist}"
        const val hilt =
            "androidx.hilt:hilt-navigation-compose:${Versions.compose_hilt}"
        const val uiTooling =
            "androidx.compose.ui:ui-tooling:${Versions.compose_version}"
    }

    object Wear {
        const val complications =
            "androidx.wear.watchface:watchface-complications-data-source-ktx:${Versions.wear_complications}"
        const val wearOngoing =
            "androidx.wear:wear-ongoing:${Versions.wear_ongoing}"
    }

    object Kapt {
        const val room =
            "androidx.room:room-compiler:${Versions.room}"
        const val dagger =
            "com.google.dagger:hilt-compiler:${Versions.dagger}"
        const val metadata =
            "org.jetbrains.kotlinx:kotlinx-metadata-jvm:${Versions.metadata_jvm}"
    }

    object Test {
        const val junit =
            "junit:junit:${Versions.junit}"
        const val mockito =
            "org.mockito:mockito-core:${Versions.mockito}"
        const val mockitoInline =
            "org.mockito:mockito-inline:${Versions.mockito}"
        const val coroutines =
            "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutinesTest}"
    }

    object UiTest {
        const val junit =
            "androidx.test.ext:junit:${Versions.junitUi}"
        const val espresso =
            "androidx.test.espresso:espresso-core:${Versions.espresso}"
        const val espressoContrib =
            "androidx.test.espresso:espresso-contrib:${Versions.espresso}"
        const val espressoIdling =
            "androidx.test.espresso:espresso-idling-resource:${Versions.espresso}"
        const val dagger =
            "com.google.dagger:hilt-android-testing:${Versions.dagger}"
        const val room =
            "androidx.room:room-testing:${Versions.room}"
    }
}