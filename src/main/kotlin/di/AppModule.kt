package com.ktor.di

import com.ktor.data.repository.MessageRepositoryImpl
import com.ktor.data.repository.UserRepositoryImpl
import com.ktor.domain.repository.MessageRepository
import com.ktor.domain.repository.UserRepository
import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.user.AuthenticateUserUseCase
import com.ktor.domain.usecases.user.FindUserUseCase
import com.ktor.domain.usecases.user.RegisterUserUseCase
import com.ktor.plugins.connectToMongoDB
import com.mongodb.client.MongoDatabase
import org.koin.dsl.module

val appModule = module {

    single<MongoDatabase> { connectToMongoDB(get()) }
    single<MessageRepository> { MessageRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get())}


    // USE CASE AUTH
    factory { RegisterUserUseCase(get()) }
    factory { FindUserUseCase(get()) }
    factory { AuthenticateUserUseCase(get()) }


    // USE CASE MESSAGES
    single { SendMessageUseCase(get()) }
    single { GetAllMessagesUseCase(get()) }
}
