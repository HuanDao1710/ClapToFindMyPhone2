
plugins {
    id("com.android.application")
}

tasks.register<Wrapper>("wrapper") {
    gradleVersion = "8.1.1"
}

tasks.register("prepareKotlinBuildScriptModel") {
}

android {
    namespace = "com.example.claptofindmyphone_version2"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.claptofindmyphone_version2"
        minSdk = 31
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // https://mvnrepository.com/artifact/com.github.fracpete/musicg
    implementation("com.github.fracpete:musicg:1.4.2.2")

}