import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.3.10"
}

val releaseKeystoreProperties = providers
    .fileContents(rootProject.layout.projectDirectory.file("keystore.properties"))
    .asText
    .orNull
    ?.let { contenido ->
        try {
            Properties().apply { load(contenido.reader()) }
        } catch (_: IllegalArgumentException) {
            null
        }
    }

android {
    namespace = "com.example.tiotecpreventivo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.tiotecpreventivo"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val releaseSigning = releaseKeystoreProperties?.let { propiedades ->
        fun propiedadCompleta(nombre: String): String? =
            propiedades.getProperty(nombre)?.takeIf { it.isNotBlank() }

        val ruta = propiedadCompleta("storeFile") ?: return@let null
        val claveAlmacen = propiedadCompleta("storePassword") ?: return@let null
        val alias = propiedadCompleta("keyAlias") ?: return@let null
        val claveLlave = propiedadCompleta("keyPassword") ?: return@let null
        val archivo = rootDir.resolve(ruta)
        if (!archivo.isFile) return@let null

        signingConfigs.create("release") {
            storeFile = archivo
            storePassword = claveAlmacen
            keyAlias = alias
            keyPassword = claveLlave
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            signingConfig = releaseSigning
            optimization {
                enable = false
            }
        }
    }
    testCoverage {
        jacocoVersion = "0.8.16"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

pluginManager.withPlugin("jacoco") {
    tasks.withType<Test>().matching { it.name == "testDebugUnitTest" }.configureEach {
        extensions.configure<JacocoTaskExtension> {
            // Robolectric carga clases de la app sin ubicación de código.
            isIncludeNoLocationClasses = true
            includes = listOf("com.example.tiotecpreventivo.*")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.runtime)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.robolectric:robolectric:4.17")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.work:work-runtime:2.11.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material:material-icons-core")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
}
