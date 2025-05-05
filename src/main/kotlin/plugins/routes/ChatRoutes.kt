package com.ktor.plugins.routes

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.data.mapper.toDomain
import com.ktor.data.mapper.toResponseDTO
import com.ktor.data.model.chat.ChatRoomRequestDto
import com.ktor.domain.usecases.chat.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.last

fun Route.chatRoomRoutes(
    chatRoomUseCase: CreateChatRoomUseCase,
    getAllChatRoom: GetAllChatRoomUseCase,
    getChatRoomByIdUseCase: GetChatRoomByIdUseCase,
    addUserToChatRoomUseCase: AddUserToChatRoomUseCase,
    removeChatRoomUseCase: RemoveChatRoomUseCase,
    removeUserFromChatRoomUseCase: RemoveUserFromChatRoomUseCase,
) {
    route("/chatrooms") {

        // 🟢 Crear sala
        post {
            val request = call.receive<ChatRoomRequestDto>()
            val chat = request.toDomain()

            val result = chatRoomUseCase(chat).last()
            call.respond(HttpStatusCode.fromValue(result.code!!), result)
        }

        // 🔵 Obtener todas las salas
        get {
            val result = getAllChatRoom().last()
            call.respond(HttpStatusCode.fromValue(result.code!!), result)
        }

        // 🔍 Obtener sala por ID
        get("{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(status = "Error", messages = listOf("Falta el ID de la sala"), code = 400))

            val result = getChatRoomByIdUseCase(id).last()
            call.respond(HttpStatusCode.fromValue(result.code!!), result)
        }

        // ➕ Agregar usuario a sala
        post("{id}/add-user") {
            val chatRoomId = call.parameters["id"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(status = "Error", messages = listOf("Falta el ID de la sala"), code = 400))

            val userId = call.parameters["userId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(status = "Error", messages = listOf("Falta el ID del usuario"), code = 400))

            val result = addUserToChatRoomUseCase(chatRoomId, userId).last()
            call.respond(HttpStatusCode.fromValue(result.code!!), result)
        }

        // ❌ Eliminar el usuario de la sala
        delete("{id}/remove-user") {
            val chatRoomId = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(status = "Error", messages = listOf("Falta el ID de la sala"), code = 400))

            val userId = call.parameters["userId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(status = "Error", messages = listOf("Falta el ID del usuario"), code = 400))

            val result = removeUserFromChatRoomUseCase(chatRoomId, userId).last()
            call.respond(HttpStatusCode.fromValue(result.code!!), result)
        }

        // ❌ Eliminar sala
        delete("{id}/") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(status = "Error", messages = listOf("Falta el ID de la sala"), code = 400))

            val result = removeChatRoomUseCase(id).last()
            call.respond(HttpStatusCode.fromValue(result.code!!), result)
        }

        get("{id}/messages") {
            val chatRoomId = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(status = "Error", messages = listOf("Falta el ID de la sala"), code = 400)
                )

            // Lógica para obtener los mensajes de esa sala
            val result = getChatRoomByIdUseCase(chatRoomId).last()

            if (result.status == "Success") {
                call.respond(HttpStatusCode.OK, result.data!!.toResponseDTO())
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 401), result)
            }
        }
    }
}