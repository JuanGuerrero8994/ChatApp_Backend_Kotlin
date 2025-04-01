package com.ktor.domain.model



data class User(
    val id: String,
    val username: String,
    val email: String,  // Incluir email para autenticación y validaciones futuras
    val passwordHash: String  // El hash de la contraseña debe ser guardado de forma segura
)