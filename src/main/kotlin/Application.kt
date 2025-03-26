package com.ktor

import com.ktor.di.appModule
import com.ktor.plugins.configureHTTP
import com.ktor.plugins.configureRouting
import com.ktor.plugins.configureSecurity
import com.ktor.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)  // Incluir el módulo de Koin
    }

    configureHTTP()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
