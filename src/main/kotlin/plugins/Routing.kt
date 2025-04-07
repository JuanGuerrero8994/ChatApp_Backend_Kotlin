package com.ktor.plugins


import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.message.GetFileMessageUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.message.UploadFileMessageUseCase
import com.ktor.domain.usecases.user.*
import com.ktor.plugins.routes.chatRoutes
import com.ktor.plugins.routes.messagesRoutes
import com.ktor.plugins.routes.userRoutes
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
    val uploadFileUseCase :UploadFileMessageUseCase by inject()
    val getFileUseCase: GetFileMessageUseCase by inject()

    routing {
        userRoutes(registerUserUseCase, findUserUseCase, authenticateUserUseCase)
        messagesRoutes(validateTokenUseCase, sendMessageUseCase, getAllMessagesUseCase,uploadFileUseCase,getFileUseCase)
        chatRoutes(validateTokenUseCase)
    }


}

