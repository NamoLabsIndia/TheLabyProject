import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "in.namolabs.laby.wear"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        applicationId = "in.namolabs.laby"
        minSdk = 33 // Wear OS 4 (Pixel Watch 1+): the S+ Bluetooth permissions the app
        // declares only exist from API 31, and API 30 would additionally require location
        // for BLE scan results, which the app deliberately refuses.
        targetSdk = libs.versions.targetSdk.get().toInt()
        // Wear releases use a separate high range because Play requires every artifact in
        // one application ID to have a unique version code across all form factors.
        versionCode = 1_000_000_003
        versionName = "0.1.2"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            vcsInfo {
                include = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

composeCompiler {
    // Kotlin 2.4.10's optional Compose group-key mapping depends on unspecified
    // class-file iteration order. Keep the normal R8 mapping, but omit that
    // augmentation until its producer is deterministic across clean builds.
    includeComposeMappingFile.set(false)
}

// Shared laby protocol stack: compiled from :app sources in place (never moved/copied by hand).
// AGP's source directory sets no longer support include/exclude filters, so a Sync task
// materializes a filtered mirror into build/sharedSrc and that directory is added as a source
// root. The app sources remain the single source of truth; extend the include list below (don't
// copy files into wear/src) when the compiler reveals a missing transitive dependency.
// Deliberately excluded: ui (except the DebugSettingsManager the mesh layer references),
// onboarding, nostr (except pure-Kotlin Bech32), net, geohash, wifiaware, hotspot, voice
// features, and the phone's foreground service.
val sharedSourceIncludes = listOf(
    "in/namolabs/laby/protocol/**",
    "in/namolabs/laby/messaging/**",
    "in/namolabs/laby/noise/**",
    "in/namolabs/laby/crypto/**",
    "in/namolabs/laby/identity/**",
    "in/namolabs/laby/mesh/**",
    "in/namolabs/laby/model/**",
    "in/namolabs/laby/sync/**",
    "in/namolabs/laby/favorites/**",
    "in/namolabs/laby/storage/AppStateStore.kt",
    "in/namolabs/laby/services/ContactDirectory.kt",
    "in/namolabs/laby/services/ContactIdentityResolver.kt",
    "in/namolabs/laby/storage/ConversationRepository.kt",
    "in/namolabs/laby/storage/ConversationStorageCipher.kt",
    "in/namolabs/laby/storage/PrivateMessageArrivalOrder.kt",
    "in/namolabs/laby/storage/SeenMessageStore.kt",
    "in/namolabs/laby/services/VerificationService.kt",
    "in/namolabs/laby/services/meshgraph/**",
    "in/namolabs/laby/service/TransportBridgeService.kt",
    "in/namolabs/laby/nostr/Bech32.kt",
    "in/namolabs/laby/nostr/GeohashAliasRegistry.kt",
    "in/namolabs/laby/features/file/FileUtils.kt",
    "in/namolabs/laby/features/voice/**",
    "in/namolabs/laby/ui/debug/DebugSettingsManager.kt",
    "in/namolabs/laby/ui/debug/DebugPreferenceManager.kt",
    "in/namolabs/laby/ui/NotificationTextUtils.kt",
    "in/namolabs/laby/util/AppConstants.kt",
    "in/namolabs/laby/util/ByteArrayExtensions.kt",
    "in/namolabs/laby/util/ByteArrayWrapper.kt",
    "in/namolabs/laby/util/BinaryEncodingUtils.kt",
)
val sharedSourceExcludes = listOf(
    "in/namolabs/laby/model/FileSharingManager.kt",
    // Legacy phone monolith and Wi-Fi Aware multiplexer; the watch composes its own service
    // (MeshCore-style) in M2 instead of reusing these.
    "in/namolabs/laby/mesh/BluetoothMeshService.kt",
    "in/namolabs/laby/mesh/UnifiedMeshService.kt",
    // Phone permission policy additionally requires location (legacy BLE); the watch app
    // declares Bluetooth permissions only, so it ships its own same-FQN variant in
    // wear/src/main (Bluetooth-only check).
    "in/namolabs/laby/mesh/BluetoothPermissionManager.kt",
)

val syncSharedAppSources = tasks.register<Sync>("syncSharedAppSources") {
    from("../app/src/main/java") {
        include(sharedSourceIncludes)
        exclude(sharedSourceExcludes)
    }
    into(layout.buildDirectory.dir("sharedSrc"))
}

// The app's own unit tests for the shared packages also run in the wear module, so shared
// behavior is continuously verified on both targets. Includes the app's JVM shims for
// android.util.Log/Base64 (app/src/test/kotlin/android) that the shared code needs on the JVM.
val syncSharedAppTests = tasks.register<Sync>("syncSharedAppTests") {
    from("../app/src/test/java") {
        include(
            "in/namolabs/laby/protocol/**",
    "in/namolabs/laby/messaging/**",
            "in/namolabs/laby/crypto/**",
            "in/namolabs/laby/mesh/**",
        )
    }
    from("../app/src/test/kotlin") {
        include(
            "android/**",
            "in/namolabs/laby/mesh/**",
            "com/laby/FileTransferTest.kt",
        )
    }
    into(layout.buildDirectory.dir("sharedTestSrc"))
}

android {
    sourceSets {
        getByName("main") {
            java.srcDir("build/sharedSrc")
            kotlin.srcDir("build/sharedSrc")
        }
        getByName("test") {
            java.srcDir("build/sharedTestSrc")
            kotlin.srcDir("build/sharedTestSrc")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(syncSharedAppSources)
}
tasks.matching { it.name.contains("UnitTest", ignoreCase = true) }.configureEach {
    dependsOn(syncSharedAppTests)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Wear Compose
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)

    // Lifecycle
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.lifecycle.process)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Cryptography (shared Noise/encryption stack)
    implementation(libs.bouncycastle.bcprov)

    // JSON (BitchatMessage model)
    implementation(libs.gson)

    // Security preferences (Noise identity persistence)
    implementation(libs.androidx.security.crypto)

    // Testing
    testImplementation(libs.bundles.testing)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
