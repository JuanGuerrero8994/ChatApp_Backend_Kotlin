package com.ktor.di

import com.ktor.data.repository.FileRepositoryImpl
import com.ktor.data.repository.MessageRepositoryImpl
import com.ktor.data.repository.UserRepositoryImpl
import com.ktor.data.service.GridFSService
import com.ktor.domain.repository.FileRepository
import com.ktor.domain.repository.MessageRepository
import com.ktor.domain.repository.UserRepository
import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.file.GetFileUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.file.UploadFileUseCase
import com.ktor.domain.usecases.user.AuthenticateUserUseCase
import com.ktor.domain.usecases.user.FindUserUseCase
import com.ktor.domain.usecases.user.RegisterUserUseCase
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import com.ktor.plugins.connectToMongoDB
import com.mongodb.client.MongoDatabase
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val appModule = module {

    single { HttpClient(CIO) { install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
                maxFrameSize = Long.MAX_VALUE }}
    }

    single<MongoDatabase> { connectToMongoDB(get()) }
    single{GridFSService(get())  }
    single<MessageRepository> { MessageRepositoryImpl(get(),get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<FileRepository>{ FileRepositoryImpl(get())  }


    // USE CASE AUTH
    factory { ValidateTokenUseCase(get()) }
    factory { RegisterUserUseCase(get()) }
    factory { FindUserUseCase(get()) }
    factory { AuthenticateUserUseCase(get()) }


    // USE CASE MESSAGES
    single { SendMessageUseCase(get()) }
    single { GetAllMessagesUseCase(get()) }
    //single { GetFileMessageUseCase(get()) }
    //single { UploadFileMessageUseCase(get()) }


    //USE CASES FILE
    factory { UploadFileUseCase(get()) }
    factory { GetFileUseCase(get()) }


    //USE CASE CHAT ROOM
    /*factory { AddUserToChatRoomUseCase(get()) }
    factory { CreateChatRoomUseCase(get()) }
    factory { GetAllChatRoomUseCase(get()) }
    factory { GetChatRoomByIdUseCase(get()) }
    factory { RemoveChatRoomUseCase(get()) }*/
}
