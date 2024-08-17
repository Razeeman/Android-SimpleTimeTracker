import com.example.util.simpletimetracker.Deps

plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(Deps.javax)
    api(Deps.coroutines)
    api(Deps.timber)
    api(Deps.kotlin)

    testImplementation(Deps.Test.junit)
    testImplementation(Deps.Test.mockito)
    testImplementation(Deps.Test.mockitoInline)
}
