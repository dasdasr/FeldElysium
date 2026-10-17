plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.example.benchmark"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
        targetSdk = 37

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        create("benchmark1") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark1"
    }
}
