package com.ktor.plugins.routes


import com.ktor.core.Resource
import com.ktor.data.mapper.MessageMapper.toDomain
import com.ktor.data.mapper.toMessageResponseDTO
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.model.Message
import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.message.GetFileMessageUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.message.UploadFileMessageUseCase
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.last

fun Route.messagesRoutes(
    validateTokenUseCase: ValidateTokenUseCase,
    sendMessageUseCase: SendMessageUseCase,
    getAllMessagesUseCase: GetAllMessagesUseCase,
    uploadFileUseCase: UploadFileMessageUseCase,
    getFileUseCase: GetFileMessageUseCase,
) {

    route("/messages") {
        get {
            val user = call.getAuthenticatedUser(validateTokenUseCase)

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
                return@get
            }

            when (val result = getAllMessagesUseCase().last()) {
                is Resource.Success -> {
                    val response = result.data?.toList()?.map { it.toMessageResponseDTO() }
                    call.respond(HttpStatusCode.OK, response ?: emptyList<Message>())
                }

                is Resource.Error -> call.respond(
                    HttpStatusCode.InternalServerError,
                    result.message ?: "Unknown error"
                )

                else -> call.respond(HttpStatusCode.OK, emptyList<Message>())
            }
        }

        post {
            val user = call.getAuthenticatedUser(validateTokenUseCase)

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
                return@post
            }

            val messageDTO = call.receive<MessageRequestDto>()

            if (messageDTO.message.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Message text cannot be empty.")
                return@post
            }


            val message = messageDTO.toDomain()

            when (val response = sendMessageUseCase(message).last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: emptyList<Message>())
                is Resource.Error -> call.respond(
                    HttpStatusCode.InternalServerError,
                    response.message ?: "Unknown error"
                )

                else -> call.respond(HttpStatusCode.OK, emptyList<Message>())
            }
        }

        post("/upload") {
            val user = call.getAuthenticatedUser(validateTokenUseCase)

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
                return@post
            }

            val multipart = call.receiveMultipart()
            var fileBytes: ByteArray? = null
            var fileName = ""
            var contentType = "application/octet-stream"

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "fileName") fileName = part.value
                        if (part.name == "contentType") contentType = part.value
                    }
                    is PartData.FileItem -> {
                        if (part.name == "file") {
                            fileBytes = part.streamProvider().readBytes()
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (fileBytes != null && fileName.isNotEmpty()) {
                val result = uploadFileUseCase(fileBytes!!, fileName, contentType)
                when (result) {
                    is Resource.Success -> call.respond(HttpStatusCode.Created, mapOf("fileId" to result.data))
                    is Resource.Error -> call.respond(HttpStatusCode.BadRequest, result.message ?: "Upload error")
                    else -> call.respond(HttpStatusCode.InternalServerError)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Missing file or fileName")
            }

        }

    }

    get("/file/{fileId}") {
        val user = call.getAuthenticatedUser(validateTokenUseCase)

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
            return@get
        }

        val fileId = call.parameters["fileId"]
        if (fileId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "File ID is required")
            return@get
        }

        when (val result = getFileUseCase(fileId)) {
            is Resource.Success -> {
                call.respondBytes(result.data!!, ContentType.Application.OctetStream)
            }
            is Resource.Error -> call.respond(HttpStatusCode.NotFound, result.message ?: "File not found")
            else -> call.respond(HttpStatusCode.InternalServerError)
        }
    }

}