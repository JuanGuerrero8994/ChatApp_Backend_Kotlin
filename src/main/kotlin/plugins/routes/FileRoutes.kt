package com.ktor.plugins.routes

import com.ktor.core.Resource
import com.ktor.data.mapper.toDomain
import com.ktor.data.mapper.toResponse
import com.ktor.data.model.file.FileRequestDTO
import com.ktor.domain.model.File
import com.ktor.domain.usecases.file.GetFileUseCase
import com.ktor.domain.usecases.file.UploadFileUseCase
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.last
import kotlinx.io.readByteArray
import java.util.*

fun Route.fileRoutes(
    validateTokenUseCase:ValidateTokenUseCase,
    uploadFileUseCase: UploadFileUseCase,
    getFileUseCase: GetFileUseCase
) {

    post("/upload") {
        /*val user = call.getAuthenticatedUser(validateTokenUseCase)

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
            return@post
        }*/

        val multipart = call.receiveMultipart()
        var fileBytes: ByteArray? = null
        var fileName: String? = null
        var contentType: String? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    // ✅ Usamos `provider()` en vez de `streamProvider()`
                    val channel = part.provider()
                    fileBytes = channel.readRemaining().readByteArray() // ✅ lectura correcta
                    fileName = part.originalFileName
                    contentType = part.contentType?.toString()
                }
                else -> {}
            }
            part.dispose()
        }

        if (fileBytes == null || fileName == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid file upload request")
            return@post
        }

        val fileDTO = FileRequestDTO(
            name = fileName!!,
            contentType = contentType ?: "application/octet-stream",
            bytesBase64 = Base64.getEncoder().encodeToString(fileBytes!!)
        )

        when (val result = uploadFileUseCase(fileDTO.toDomain()).last()) {
            is Resource.Success -> {
                val response = result.data?.toResponse()
                call.respond(HttpStatusCode.OK, response ?: mapOf("message" to "File uploaded but no response"))
            }
            is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, result.message ?: "Unknown error")
            else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
        }
    }

    get("/file/{id}") { //✅ GET /file/{id} → Devuelve metadata de la imagen (nombre, tipo, URL para verla).

        /*val user = call.getAuthenticatedUser(validateTokenUseCase)

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
            return@get
        }*/

        val fileId = call.parameters["id"]
        if (fileId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Missing file ID")
            return@get
        }

        when (val result = getFileUseCase(fileId).last()) {
            is Resource.Success -> {
                val file = result.data
                if (file != null) {
                    val contentType = ContentType.parse(file.contentType)
                    val fileBytes = file.bytes
                    val totalLength = fileBytes.size.toLong()

                    val rangeHeader = call.request.headers[HttpHeaders.Range]
                    if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                        // Parse range: e.g. "bytes=0-1023"
                        val rangeValue = rangeHeader.removePrefix("bytes=").split("-")
                        val start = rangeValue[0].toLongOrNull() ?: 0L
                        val end = rangeValue.getOrNull(1)?.toLongOrNull() ?: (totalLength - 1)

                        if (start >= totalLength || end >= totalLength || start > end) {
                            call.respond(HttpStatusCode.RequestedRangeNotSatisfiable)
                            return@get
                        }

                        val contentRange = "bytes $start-$end/$totalLength"
                        val partialBytes = fileBytes.sliceArray(start.toInt()..end.toInt())

                        call.response.header(HttpHeaders.ContentRange, contentRange)
                        call.response.header(HttpHeaders.AcceptRanges, "bytes")
                        call.respondBytes(
                            bytes = partialBytes,
                            contentType = contentType,
                            status = HttpStatusCode.PartialContent
                        )
                    } else {
                        // No Range header, send full content
                        call.response.header(HttpHeaders.AcceptRanges, "bytes")
                        call.respondBytes(
                            bytes = fileBytes,
                            contentType = contentType,
                            status = HttpStatusCode.OK
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "File not found")
                }
            }

            is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, result.message ?: "Unknown error")
            else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
        }
    }

    get("/file/content/{id}") {  //Devuelve el binario del video o imagen, según el tipo de archivo.


        val fileId = call.parameters["id"]
        if (fileId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Missing file ID")
            return@get
        }

        when (val result = getFileUseCase(fileId).last()) {
            is Resource.Success -> {
                val file = result.data
                if (file != null) {
                    val contentType = ContentType.parse(file.contentType)
                    val fileBytes = file.bytes
                    val totalLength = fileBytes.size.toLong()

                    val rangeHeader = call.request.headers[HttpHeaders.Range]
                    if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                        val rangeValue = rangeHeader.removePrefix("bytes=").split("-")
                        val start = rangeValue[0].toLongOrNull() ?: 0L
                        val end = rangeValue.getOrNull(1)?.toLongOrNull() ?: (totalLength - 1)

                        if (start >= totalLength || end >= totalLength || start > end) {
                            call.respond(HttpStatusCode.RequestedRangeNotSatisfiable)
                            return@get
                        }

                        val contentRange = "bytes $start-$end/$totalLength"
                        val partialBytes = fileBytes.sliceArray(start.toInt()..end.toInt())

                        call.response.header(HttpHeaders.ContentRange, contentRange)
                        call.response.header(HttpHeaders.AcceptRanges, "bytes")
                        call.respondBytes(partialBytes, contentType, HttpStatusCode.PartialContent)
                    } else {
                        call.response.header(HttpHeaders.AcceptRanges, "bytes")
                        call.respondBytes(fileBytes, contentType, HttpStatusCode.OK)
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "File not found")
                }
            }

            is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, result.message ?: "Unknown error")
            else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
        }
    }

}




