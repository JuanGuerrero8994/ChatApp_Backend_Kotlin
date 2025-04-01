package com.ktor.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*


import java.util.*

fun Application.configureSecurity() {
    val secret = "your_secret_key" // Define tu clave secreta para firmar el JWT
    val issuer = "your_app_name"
    val audience = "your_audience"

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Access to the '/users' route"
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

fun Application.generateToken(username: String): String {
    val secret = "your_secret_key"
    val issuer = "your_app_name"
    val audience = "your_audience"

    return JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + 3600000)) // Token válido por 1 hora
        .sign(Algorithm.HMAC256(secret))
}
