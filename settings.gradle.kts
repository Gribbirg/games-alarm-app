pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "SmartAlarm"
include(":app")
include(":core:common")
include(":core:data")
include(":core:alarm")
include(":core:ui")
include(":feature:alarms")
include(":feature:games")
include(":feature:games:calc")
include(":feature:games:memory")
include(":feature:games:equation")
include(":feature:games:sorting")
include(":feature:games:pairs")
include(":feature:games:sequence")
include(":feature:games:stroop")
include(":feature:games:oddoneout")
include(":feature:games:maze")
include(":feature:games:anagram")
include(":feature:games:truefalse")
include(":feature:games:demo")
include(":feature:records")
include(":feature:profile")
include(":feature:settings")
