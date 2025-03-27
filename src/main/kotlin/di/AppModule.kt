package com.ktor.di

import com.ktor.data.repository.MessageRepositoryImpl
import com.ktor.domain.repository.MessageRepository
import com.ktor.domain.usecases.GetAllMessagesUseCase
import com.ktor.domain.usecases.SendMessageUseCase
import com.ktor.plugins.connectToMongoDB
import com.mongodb.client.MongoDatabase
import org.koin.dsl.module
import io.ktor.server.application.*

val appModule = module {

    single<MongoDatabase> { connectToMongoDB(get()) }
    single<MessageRepository> { MessageRepositoryImpl(get()) }

    // Use cases
    single { SendMessageUseCase(get()) }
    single { GetAllMessagesUseCase(get()) }
}
