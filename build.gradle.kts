
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.ktor"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.mongodb.driver.core)
    implementation(libs.mongodb.driver.sync)
    implementation(libs.bson)
    implementation("org.litote.kmongo:kmongo-coroutine:4.6.0")

    implementation("io.ktor:ktor-server-swagger:3.1.1") // Para servir Swagger UI
    implementation("io.ktor:ktor-server-openapi:3.1.1") // Para generar OpenAPI


    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.web.socket)
    implementation(libs.ktor.server.web.socket)


    implementation("org.mindrot:jbcrypt:0.4") // USAR LA ENCRIPTACION PARA LAS CONTRASEÑAS

    testImplementation(libs.ktor.server.test.host)

    implementation(libs.logback.classic)

    testImplementation(libs.kotlin.test.junit)
    implementation(libs.koin.core)

    implementation("io.insert-koin:koin-core:4.0.3")
    // Koin for Ktor
    implementation("io.insert-koin:koin-ktor:4.0.3")
    // SLF4J Logger
    implementation("io.insert-koin:koin-logger-slf4j:4.0.3")

    implementation(libs.logback.classic)




}
