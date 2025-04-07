package com.ktor.data.service

import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import org.bson.Document
import org.bson.types.ObjectId
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
}