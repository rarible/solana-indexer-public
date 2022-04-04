apply(plugin = "server")

val mongockVersion: String by project

dependencies {
    implementation(project(":solana-common"))
    implementation("com.github.cloudyrock.mongock:mongock-spring-v5:$mongockVersion")
    implementation("com.github.cloudyrock.mongock:mongodb-springdata-v3-driver:$mongockVersion")
}