plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "Bean"

val isCi = System.getenv("GITHUB_ACTIONS") == "true"
buildCache {
    local {
        isEnabled = !isCi
    }
}