package com.yusufozturk.cinetrack.data.model

import com.google.gson.annotations.SerializedName

data class VideoResponse(
    val results: List<Video>
)

data class Video(
    val key: String,
    val name: String,
    val site: String,
    val type: String,

    @SerializedName("official")
    val isOfficial: Boolean
)