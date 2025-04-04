package com.ktor

import com.ktor.di.appModule
import com.ktor.plugins.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureWebSockets()
    install(Koin) {
        slf4jLogger()
        modules(appModule)
        allowOverride(true)
        val app = this@module
        koin.loadModules(listOf(module { single { app } }))
    }

    configureHTTP()
    configureSerialization()
    configureRouting()
}
