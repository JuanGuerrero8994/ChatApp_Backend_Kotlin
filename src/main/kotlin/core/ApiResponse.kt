package com.ktor.core

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val data: T? = null,                      // Datos que devuelve la API (pueden ser de cualquier tipo)
    val messages: List<String> = emptyList(),  // Lista de mensajes, que puede incluir errores o éxitos
    val status: String,                       // El estado de la respuesta: "success", "error", etc.
    val code: Int? = null                     // Opcionalmente, se puede agregar un código de estado HTTP (por ejemplo, 200, 400, 404)
)