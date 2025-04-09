package com.ktor.plugins


import com.ktor.domain.usecases.chat.CreateChatRoomUseCase
import com.ktor.domain.usecases.chat.GetAllChatRoomUseCase
import com.ktor.domain.usecases.chat.GetChatRoomByIdUseCase
import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.file.GetFileUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.file.UploadFileUseCase
import com.ktor.domain.usecases.message.AddUserToChatRoomUseCase
import com.ktor.domain.usecases.message.RemoveChatRoomUseCase
import com.ktor.domain.usecases.user.*
import com.ktor.plugins.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    val registerUserUseCase: RegisterUserUseCase by inject()
    val findUserUseCase: FindUserUseCase by inject()
    val authenticateUserUseCase: AuthenticateUserUseCase by inject()
    val validateTokenUseCase: ValidateTokenUseCase by inject()


    val getAllMessagesUseCase: GetAllMessagesUseCase by inject()
    val sendMessageUseCase: SendMessageUseCase by inject()
    val uploadFileUseCase: UploadFileUseCase by inject()
    val getFileUseCase: GetFileUseCase by inject()


    val createChatRoomUseCase: CreateChatRoomUseCase by inject()
    val getAllChatRoomUseCase: GetAllChatRoomUseCase by inject()
    val getChatRoomByIdUseCase: GetChatRoomByIdUseCase by inject()
    val addUserToChatRoomUseCase: AddUserToChatRoomUseCase by inject()
    val removeChatRoomUseCase: RemoveChatRoomUseCase by inject()


    val chatConnectionManager = ChatConnectionManager()

    routing {
        userRoutes(registerUserUseCase, findUserUseCase, authenticateUserUseCase)
        messagesRoutes(validateTokenUseCase, sendMessageUseCase, getAllMessagesUseCase)
        fileRoutes(validateTokenUseCase, uploadFileUseCase, getFileUseCase)
        webSocketRoutes(validateTokenUseCase, chatConnectionManager)
        chatRoomRoutes(validateTokenUseCase, createChatRoomUseCase, getAllChatRoomUseCase, getChatRoomByIdUseCase, addUserToChatRoomUseCase, removeChatRoomUseCase)
    }


}

