package com.ktor.data.mapper

import com.ktor.data.model.file.FileRequestDTO
import com.ktor.data.model.file.FileResponseDTO
import com.ktor.domain.model.File
import org.bson.Document
import java.util.*


// 🔹 Desde Mongo Document → Dominio
fun Document.toDomain(bytes: ByteArray): File = File(
    id = this.getObjectId("_id").toHexString(),
    name = this.getString("filename"),
    contentType = this.getString("contentType"),
    bytes = bytes
)

// 🔹 Desde Dominio → FileResponseDTO
fun File.toResponse(): FileResponseDTO {
    return FileResponseDTO(
        id = id ?: "",
        name = name,
        contentType = contentType

    )
}

// 🔹 Desde ResponseDTO → Dominio (opcional)
fun FileRequestDTO.toDomain(): File {
    return File(
        name = name,
        contentType = contentType,
        bytes = Base64.getDecoder().decode(bytesBase64)
    )
}