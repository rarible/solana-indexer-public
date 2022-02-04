val raribleCommonVersion: String by project

dependencies {
    api(project(":solana-common"))
    api("com.rarible.core:rarible-core-test-common:$raribleCommonVersion")
}
