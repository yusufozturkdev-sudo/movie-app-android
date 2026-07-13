package com.yusufozturk.cinetrack.data.model

import com.google.gson.annotations.SerializedName

data class RequestTokenResponse(
    val success: Boolean,
    @SerializedName("request_token") val requestToken: String
)

data class CreateSessionBody(
    @SerializedName("request_token") val requestToken: String
)

data class SessionResponse(
    val success: Boolean,
    @SerializedName("session_id") val sessionId: String
)

data class AccountResponse(
    val id: Int,
    val username: String,
    val name: String?
)

data class WatchlistRequestBody(
    @SerializedName("media_type") val mediaType: String,
    @SerializedName("media_id") val mediaId: Int,
    val watchlist: Boolean
)

data class StatusResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_message") val statusMessage: String
)

data class RateMovieBody(
    val value: Double
)

data class DeleteSessionBody(
    @SerializedName("session_id") val sessionId: String
)
