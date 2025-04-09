package com.ktor.plugins.routes

import com.ktor.core.Resource
import com.ktor.data.mapper.toDomain
import com.ktor.data.mapper.toResponseDTO
import com.ktor.data.model.chat.ChatRoomRequestDto
import com.ktor.domain.model.ChatRoom
import com.ktor.domain.usecases.chat.CreateChatRoomUseCase
import com.ktor.domain.usecases.chat.GetAllChatRoomUseCase
import com.ktor.domain.usecases.chat.GetChatRoomByIdUseCase
import com.ktor.domain.usecases.message.AddUserToChatRoomUseCase
import com.ktor.domain.usecases.message.RemoveChatRoomUseCase
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.last

fun Route.chatRoomRoutes(
    validateTokenUseCase: ValidateTokenUseCase,
    chatRoomUseCase: CreateChatRoomUseCase,
    getAllChatRoom: GetAllChatRoomUseCase,
    getChatRoomByIdUseCase: GetChatRoomByIdUseCase,
    addUserToChatRoomUseCase: AddUserToChatRoomUseCase,
    removeChatRoomUseCase: RemoveChatRoomUseCase
) {

    route("/chatrooms") {

        // 🟢 Crear sala
        post {
            val request = call.receive<ChatRoomRequestDto>()
            val chat = request.toDomain()

            when (val result = chatRoomUseCase(chat).last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, result.data?.toResponseDTO() ?: "")
                is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, result.message ?: "Unknown error")
                else -> call.respond(HttpStatusCode.InternalServerError, "")
            }
        }

        // 🔵 Obtener todas las salas
        get {
            when (val result = getAllChatRoom().last()) {
                is Resource.Success -> {
                    val response = result.data?.map { it.toResponseDTO() } ?: emptyList()
                    call.respond(HttpStatusCode.OK, response)
                }
                is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, result.message ?: "Error desconocido")
                else -> call.respond(HttpStatusCode.InternalServerError, "")
            }
        }

        // 🔍 Obtener sala por ID
        get("{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Falta el ID de la sala")

            when (val result = getChatRoomByIdUseCase(id).last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, result.data?.toResponseDTO() ?: "")
                is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, result.message ?: "Error al obtener la sala")
                else -> call.respond(HttpStatusCode.InternalServerError, "")
            }
        }

        // ➕ Agregar usuario a sala
        post("{id}/add-user") {
            val chatRoomId = call.parameters["id"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Falta el ID de la sala")

            val userId = call.receive<String>()

            when (val result = addUserToChatRoomUseCase(chatRoomId, userId).last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, result.data ?: "")
                is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, result.message ?: "Error al agregar usuario")
                else -> call.respond(HttpStatusCode.InternalServerError, "")
            }
        }

        // ❌ Eliminar sala
        delete("{id}") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Falta el ID de la sala")

            when (val result = removeChatRoomUseCase(id).last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, true)
                is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, result.message ?: "Error al eliminar sala")
                else -> call.respond(HttpStatusCode.InternalServerError, false)
            }
        }
    }
}
