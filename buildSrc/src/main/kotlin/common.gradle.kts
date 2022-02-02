plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

val raribleCommonVersion: String by project
val springBootVersion: String by project
val coroutinesVersion: String by project

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "http://nexus-ext.rarible.int/repository/maven-public/") { isAllowInsecureProtocol = true }
    maven(url = "https://repo.rarible.org/repository/maven-public/")
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))


    testImplementation("com.rarible.core:rarible-core-test-common:$raribleCommonVersion")

    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "${JavaVersion.VERSION_1_8}"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "${JavaVersion.VERSION_1_8}"
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<TestReport>("coverage")