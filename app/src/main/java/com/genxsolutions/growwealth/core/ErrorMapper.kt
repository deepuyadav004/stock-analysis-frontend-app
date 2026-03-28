package com.genxsolutions.growwealth.core

import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

object ErrorMapper {
    fun toUserMessage(error: Throwable?, fallback: String): String {
        if (error == null) return fallback

        return when (error) {
            is SocketTimeoutException -> "Request timed out. Pull to refresh and try again."
            is IOException -> "Network unavailable. Check connection and try again."
            is HttpException -> mapHttpCode(error.code(), fallback)
            else -> error.message?.takeIf { it.isNotBlank() } ?: fallback
        }
    }

    private fun mapHttpCode(code: Int, fallback: String): String {
        return when (code) {
            400 -> "Invalid request. Please try again."
            401, 403 -> "Access denied for this request."
            404 -> "Requested data is not available."
            429 -> "Too many requests. Please wait and retry."
            500, 502, 503, 504 -> "Server is temporarily unavailable. Please retry shortly."
            else -> fallback
        }
    }
}
