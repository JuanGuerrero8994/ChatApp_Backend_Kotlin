package com.ktor.data.service

import com.ktor.domain.model.File
import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import org.bson.Document
import org.bson.types.ObjectId
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class GridFSService(private val database: MongoDatabase) {

    private val bucket: GridFSBucket = GridFSBuckets.create(database)

    fun uploadFile(inputStream: InputStream, fileName: String, contentType: String): String {
        val options = GridFSUploadOptions()
            .metadata(Document("contentType", contentType))
        val fileId = bucket.uploadFromStream(fileName, inputStream, options)
        return fileId.toHexString()
    }

    fun downloadFile(fileId: String, outputStream: OutputStream): Boolean {
        return try {
            bucket.downloadToStream(ObjectId(fileId), outputStream)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getFile(fileId: String): File? {
        return try {

            val objectId = try {
                ObjectId(fileId)
            } catch (e: IllegalArgumentException) {
                println("❌ ID inválido: $fileId")
                return null
            }

            val fileInfo = bucket.find(Document("_id", objectId)).firstOrNull()
            if (fileInfo == null) {
                println("❌ No se encontró el archivo con ID: $fileId")
                return null
            }

            val outputStream = ByteArrayOutputStream()
            bucket.downloadToStream(objectId, outputStream)

            val name = fileInfo.filename
            val contentType = fileInfo.metadata?.getString("contentType") ?: "application/octet-stream"
            val bytes = outputStream.toByteArray()

            println("✅ Archivo recuperado: $name ($contentType), ${bytes.size} bytes")

            File(
                id = fileId,
                name = name,
                contentType = contentType,
                bytes = bytes
            )
        } catch (e: Exception) {
            println("⚠️ Error al recuperar archivo: ${e.message}")
            null
        }
    }

}