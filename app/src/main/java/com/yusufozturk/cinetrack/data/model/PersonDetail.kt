package com.yusufozturk.cinetrack.data.model

import com.google.gson.annotations.SerializedName

data class PersonDetail(
    val id: Int,
    val name: String,
    val biography: String,

    @SerializedName("profile_path")
    val profilePath: String?,

    @SerializedName("known_for_department")
    val knownForDepartment: String?,

    val birthday: String?,

    @SerializedName("place_of_birth")
    val placeOfBirth: String?
)