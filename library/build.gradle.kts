import com.vanniktech.maven.publish.SonatypeHost

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "io.github.kangyee.vapcache.library"
    compileSdk = 33

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.compose)
    implementation(libs.okio)
    implementation(platform(libs.compose.bom))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

mavenPublishing {
    coordinates("io.github.kangyee", "vapcache", "1.0.0")

    pom {
        name.set("VapCache")
        description.set("VAP (Video Animation Player) cache library for easier fetching of resources from the web.")
        inceptionYear.set("2023")
        url.set("https://github.com/kangyee/vapcache-android/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("kangyee")
                name.set("KangYee")
                url.set("https://github.com/kangyee/")
            }
        }
        scm {
            url.set("https://github.com/kangyee/vapcache-android/")
            connection.set("scm:git:git://github.com/kangyee/vapcache-android.git")
            developerConnection.set("scm:git:ssh://git@github.com/kangyee/vapcache-android.git")
        }
    }

    publishToMavenCentral(SonatypeHost.S01, true)
    signAllPublications()
}