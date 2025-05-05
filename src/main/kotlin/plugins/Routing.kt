package com.ktor.plugins


import ChatConnectionManager
import com.ktor.domain.usecases.chat.*
import com.ktor.domain.usecases.file.GetFileUseCase
import com.ktor.domain.usecases.file.UploadFileUseCase
import com.ktor.domain.usecases.message.MessageUseCases
import com.ktor.domain.usecases.token.ValidateTokenUseCase
import com.ktor.domain.usecases.user.*
import com.ktor.plugins.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import webSocketRoutes

fun Application.configureRouting() {

    val authenticateUserUseCase: AuthenticateUserUseCase by inject()
    val validateTokenUseCase: ValidateTokenUseCase by inject()

    val messageUseCases :MessageUseCases by inject()

    val uploadFileUseCase: UploadFileUseCase by inject()
    val getFileUseCase: GetFileUseCase by inject()


    val createChatRoomUseCase: CreateChatRoomUseCase by inject()
    val getAllChatRoomUseCase: GetAllChatRoomUseCase by inject()
    val getChatRoomByIdUseCase: GetChatRoomByIdUseCase by inject()
    val addUserToChatRoomUseCase: AddUserToChatRoomUseCase by inject()
    val removeChatRoomUseCase: RemoveChatRoomUseCase by inject()
    val removeUserFromChatRoomUseCase: RemoveUserFromChatRoomUseCase by inject()

    val chatConnectionManager = ChatConnectionManager()

    routing {
        userRoutes(authenticateUserUseCase)
        fileRoutes(uploadFileUseCase, getFileUseCase)
        messagesRoutes(messageUseCases)
        chatRoomRoutes(createChatRoomUseCase, getAllChatRoomUseCase, getChatRoomByIdUseCase, addUserToChatRoomUseCase, removeChatRoomUseCase, removeUserFromChatRoomUseCase)
        webSocketRoutes(validateTokenUseCase = validateTokenUseCase, messageUseCases = messageUseCases , getFileUseCase =  getFileUseCase, chatConnectionManager = chatConnectionManager)
    }


}

