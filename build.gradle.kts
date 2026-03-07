plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}

tasks.register("syncUpdateServer") {
    group = "publishing"
    description = "根据 app 当前版本与 release_notes.txt 生成并写入 update-server/changelog.json（委托 :app:syncUpdateServer）"
    dependsOn(":app:syncUpdateServer")
}
