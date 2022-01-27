rootProject.name = "solana-indexer"

include(
    "solana-common",
    "solana-api",
    "solana-indexer",
    "util-borsh"
)

pluginManagement {
    val springBootVersion: String by settings
    val kotlinVersion: String by settings

    plugins {
        id("org.springframework.boot") version springBootVersion apply false
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.spring") version kotlinVersion apply false
    }
}
