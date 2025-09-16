package com.tmukas.filmvault.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesResponseDto(
    @SerialName("page")
    val page: Int?,
    @SerialName("results")
    val results: List<MovieDto>?,
    @SerialName("total_pages")
    val totalPages: Int?,
    @SerialName("total_results")
    val totalResults: Int?
)