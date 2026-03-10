plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

import java.util.Calendar

android {
    namespace = "com.lottery.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lottery.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.0.5"
        // 发布时改为你的更新服务地址，例如: "https://update.your-domain.com"
        buildConfigField("String", "UPDATE_SERVER_BASE_URL", "\"http://8.148.250.148:5000\"")
        // 发布时改为你的查询服务地址，例如: "https://query.your-domain.com"
        buildConfigField("String", "QUERY_SERVER_BASE_URL", "\"http://8.148.250.148:8000\"")
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
        buildConfig = true
    }
}

android.applicationVariants.configureEach {
    outputs.configureEach {
        (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
            "zhongleme-${name}.apk"
    }
}

// 根据当前版本与 release_notes.txt 生成 update-server 所需 changelog，并写入 update-server 目录
tasks.register("syncUpdateServer") {
    group = "publishing"
    description = "根据当前版本与 release_notes.txt 生成并写入 update-server/changelog.json"
    doLast {
        val vc = android.defaultConfig.versionCode
        val vn = android.defaultConfig.versionName?.toString() ?: "1.0.0"
        val notesFile = rootProject.file("release_notes.txt")
        val releaseNotes = if (notesFile.exists()) {
            notesFile.readText().lines().filter { it.isNotBlank() && !it.trimStart().startsWith("#") }
                .joinToString("\n").trim().ifBlank { "更新内容" }
        } else "更新内容"
        val cal = Calendar.getInstance()
        val releaseDate = "${cal.get(Calendar.YEAR)}-${(cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}-${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
        val updateServerDir = rootProject.file("update-server")
        val changelogFile = File(updateServerDir, "changelog.json")
        val slurper = groovy.json.JsonSlurper()
        @Suppress("UNCHECKED_CAST")
        val existing: List<Map<String, Any>> = if (changelogFile.exists()) {
            (slurper.parseText(changelogFile.readText()) as List<*>).map { it as Map<String, Any> }
        } else emptyList()
        val newEntry = mapOf<String, Any>(
            "versionCode" to (vc ?: 1),
            "versionName" to vn,
            "releaseDate" to releaseDate,
            "releaseNotes" to releaseNotes,
            "downloadUrl" to (if (project.hasProperty("updateServerBaseUrl")) {
                (project.property("updateServerBaseUrl") as String).trimEnd('/') + "/releases/$vn/zhongleme.apk"
            } else ""),
            "minVersionCode" to 1
        )
        val updated = listOf(newEntry) + existing
        updateServerDir.mkdirs()
        changelogFile.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(updated)))
        logger.lifecycle("syncUpdateServer: 已写入 ${changelogFile.absolutePath} (v$vn / $vc)")
    }
}

// 将构建产物 APK 拷贝到 update-server/releases/{versionName}/zhongleme.apk
tasks.register("copyApkToUpdateServer") {
    group = "publishing"
    description = "将 app 的 debug APK 拷贝到 update-server/releases/{versionName}/zhongleme.apk"
    dependsOn("assembleDebug")
    doLast {
        val vn = android.defaultConfig.versionName?.toString() ?: "1.0.0"
        val apkDir = project.layout.buildDirectory.dir("outputs/apk/debug").get().asFile
        val srcApk = File(apkDir, "zhongleme-debug.apk")
        if (!srcApk.exists()) {
            throw GradleException("APK 不存在: ${srcApk.absolutePath}，请先执行 :app:assembleDebug")
        }
        val releasesVersionDir = rootProject.file("update-server/releases/$vn")
        releasesVersionDir.mkdirs()
        val destApk = File(releasesVersionDir, "zhongleme.apk")
        srcApk.copyTo(destApk, overwrite = true)
        logger.lifecycle("copyApkToUpdateServer: 已拷贝到 ${destApk.absolutePath}")
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.compose.animation:animation")

    testImplementation("junit:junit:4.13.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
