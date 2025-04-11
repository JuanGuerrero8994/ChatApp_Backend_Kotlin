package com.ktor

import com.ktor.di.appModule
import com.ktor.plugins.*
import com.ktor.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
        allowOverride(true)
        val app = this@module
        koin.loadModules(listOf(module { single { app } }))
    }
    configureSerialization()
    configureHTTP()
    configureWebSockets()
    configureRouting()
}
