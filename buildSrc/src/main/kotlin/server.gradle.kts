plugins {
    id("common")
    id("org.springframework.boot")
}

tasks.bootJar {
    destinationDirectory.set(file("./target/boot"))
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}