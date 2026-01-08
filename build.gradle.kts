plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.fatbinary)
}

group = "org.acme"
version = "0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.bundles.ktor.client)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.kotlinx.coroutines)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

fatBinary {
    mainClass = "io.github.cdsap.ghacli.Main"
    name = "ghacli"
}
tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
}
