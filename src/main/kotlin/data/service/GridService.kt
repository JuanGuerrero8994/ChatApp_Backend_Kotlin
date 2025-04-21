package com.ktor.data.service

import com.ktor.domain.model.File
import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.util.idValue
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class GridFSService(database: MongoDatabase) {

    private val bucket: GridFSBucket = GridFSBuckets.create(database)

    fun uploadFile(inputStream: InputStream, fileName: String, contentType: String): String {
        val customFileId = fileName // Si el ID personalizado incluye el sufijo
        val options = GridFSUploadOptions()
            .metadata(Document("contentType", contentType).append("customFileId", customFileId)) // Añadir customFileId con sufijo
        val fileId = bucket.uploadFromStream(fileName, inputStream, options)
        println("✅ Archivo subido: $fileName con ID: ${fileId.toHexString()}")
        return fileId.toHexString() // Devuelve ObjectId convertido a string
    }

    fun downloadFile(fileId: String, outputStream: OutputStream): Boolean {
        return try {
            val objectId = ObjectId(fileId) // Convertir a ObjectId
            bucket.downloadToStream(objectId, outputStream)
            println("✅ Archivo descargado: $fileId")
            true
        } catch (e: Exception) {
            println("⚠️ Error al descargar archivo con ID: $fileId - ${e.message}")
            false
        }
    }

    fun getFile(fileId: String): File? {
        return try {
            // Extraer solo la parte base del fileId antes del sufijo
            val baseFileId = fileId.substringBefore("_") // Extrae "1732100574233.jpg" de "1732100574233.jpg_1744743969917"

            // Buscar el archivo en MongoDB utilizando solo el baseFileId
            val fileInfo = bucket.find(Document("metadata.customFileId", baseFileId)).firstOrNull()

            if (fileInfo == null) {
                println("❌ No se encontró el archivo con ID personalizado: $baseFileId")
                return null
            }

            val outputStream = ByteArrayOutputStream()

            bucket.downloadToStream(fileInfo.id, outputStream)

            val name = fileInfo.filename
            val contentType = fileInfo.metadata?.getString("contentType") ?: "application/octet-stream"
            val bytes = outputStream.toByteArray()

            println("✅ Archivo recuperado: $name ($contentType), ${bytes.size} bytes")

            val objectId = (fileInfo.id as? org.bson.BsonObjectId)?.value ?: fileInfo.id

            File(
                id = objectId.toString(),
                name = name,
                contentType = contentType,
                bytes = bytes
            )
        } catch (e: Exception) {
            println("⚠️ Error al recuperar archivo con ID: $fileId - ${e.message}")
            null
        }
    }

}
