package com.yusufozturk.cinetrack.data.model

import com.google.gson.annotations.SerializedName

data class CreditsResponse(
    val cast: List<CastMember>
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,

    @SerializedName("profile_path")
    val profilePath: String?
)
