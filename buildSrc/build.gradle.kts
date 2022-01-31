plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://repo.rarible.org/repository/maven-public/")
}

val kotlinVersion: String by project
val springBootVersion: String by project

dependencies {
    implementation("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}
