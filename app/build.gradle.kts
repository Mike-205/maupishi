plugins {
    alias(
        libs.plugins.android.application
    )
    alias(
        libs.plugins.kotlin.compose
    )
    alias(
        libs.plugins.kotlin.serialization
    )
    alias(
        libs.plugins.ksp
    )
    alias(
        libs.plugins.androidx.room
    )
}

android {
    namespace =
        "ke.co.maupishi.app"
    compileSdk {
        version =
            release(
                37
            )
    }

    defaultConfig {
        applicationId =
            "ke.co.maupishi.app"
        minSdk =
            26
        targetSdk =
            37
        versionCode =
            1
        versionName =
            "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        localeFilters += listOf("en", "sw")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            optimization {
                enable =
                    true
                packageScope =
                    setOf(
                        "androidx.**",
                        "kotlin.**",
                        "kotlinx.**"
                    )
            }
        }
    }
    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11
        targetCompatibility =
            JavaVersion.VERSION_11
    }
    buildFeatures {
        compose =
            true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources =
                true
            all {
                it.jvmArgs(
                    "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED"
                )
            }
        }
    }
}

room {
    schemaDirectory(
        "$projectDir/schemas"
    )
}

dependencies {
    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )
    implementation(
        libs.androidx.activity.compose
    )
    implementation(
        libs.androidx.compose.material3
    )
    implementation(
        libs.androidx.compose.ui
    )
    implementation(
        libs.androidx.compose.ui.graphics
    )
    implementation(
        libs.androidx.compose.ui.tooling.preview
    )
    implementation(
        libs.androidx.core.ktx
    )
    implementation(
        libs.kotlinx.serialization.json
    )
    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )
    implementation(
        libs.androidx.room.runtime
    )
    implementation(
        libs.androidx.room.ktx
    )
    ksp(
        libs.androidx.room.compiler
    )
    testImplementation(
        libs.junit
    )
    testImplementation(
        libs.robolectric
    )
    testImplementation(
        libs.androidx.test.core
    )
    testImplementation(
        libs.kotlinx.coroutines.test
    )
    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )
    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )
    androidTestImplementation(
        libs.androidx.espresso.core
    )
    androidTestImplementation(
        libs.androidx.junit
    )
    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}