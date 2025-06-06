package com.ktor.plugins

import io.ktor.server.application.*
import io.ktor.server.config.*


/**
 * Establishes connection with a MongoDB database.
 *
 * The following configuration properties (in application.yaml/application.conf) can be specified:
 * * `db.mongo.user` username for your database
 * * `db.mongo.password` password for the user
 * * `db.mongo.host` host that will be used for the database connection
 * * `db.mongo.port` port that will be used for the database connection
 * * `db.mongo.maxPoolSize` maximum number of connections to a MongoDB server
 * * `db.mongo.database.name` name of the database
 *
 * IMPORTANT NOTE: in order to make MongoDB connection working, you have to start a MongoDB server first.
 * See the instructions here: https://www.mongodb.com/docs/manual/administration/install-community/
 * all the paramaters above
 *
 * @returns [MongoDatabase] instance
 * */

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase

fun connectToMongoDB(application: Application): MongoDatabase {
    val host = application.environment.config.tryGetString("db.mongo.host") ?: "127.0.0.1"
    val port = application.environment.config.tryGetString("db.mongo.port") ?: "27017"
    val maxPoolSize = application.environment.config.tryGetString("db.mongo.maxPoolSize")?.toInt() ?: 20
    val databaseName = application.environment.config.tryGetString("db.mongo.database.name") ?: "ChatApp"

    val uri = "mongodb://$host:$port/?maxPoolSize=$maxPoolSize&w=majority"

    val mongoClient = MongoClients.create(uri)
    val database = mongoClient.getDatabase(databaseName)

    application.monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return database
}