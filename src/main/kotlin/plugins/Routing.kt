package com.ktor.plugins


import ChatConnectionManager
import com.ktor.domain.usecases.chat.*
import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.file.GetFileUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.file.UploadFileUseCase
import com.ktor.domain.usecases.message.GetMessagesByChatRoomIdUseCase
import com.ktor.domain.usecases.user.*
import com.ktor.plugins.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import webSocketRoutes

fun Application.configureRouting() {

    val findUserUseCase: FindUserUseCase by inject()
    val authenticateUserUseCase: AuthenticateUserUseCase by inject()
    val validateTokenUseCase: ValidateTokenUseCase by inject()


    val getAllMessagesUseCase: GetAllMessagesUseCase by inject()
    val sendMessageUseCase: SendMessageUseCase by inject()
    val getMessagesByChatRoomIdUseCase: GetMessagesByChatRoomIdUseCase by inject()

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
        userRoutes(findUserUseCase, authenticateUserUseCase)
        messagesRoutes( sendMessageUseCase, getAllMessagesUseCase, getMessagesByChatRoomIdUseCase)
        fileRoutes(uploadFileUseCase, getFileUseCase)
        chatRoomRoutes(
            createChatRoomUseCase,
            getAllChatRoomUseCase,
            getChatRoomByIdUseCase,
            addUserToChatRoomUseCase,
            removeChatRoomUseCase,
            removeUserFromChatRoomUseCase
        )
        webSocketRoutes(
            validateTokenUseCase,
            sendMessageUseCase,
            getMessagesByChatRoomIdUseCase,
            getFileUseCase,
            chatConnectionManager
        )
    }


}

