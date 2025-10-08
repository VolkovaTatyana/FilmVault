package com.tmukas.filmvault.data.remote.api

import com.tmukas.filmvault.data.remote.dto.MoviesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApiService {

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "primary_release_date.desc",
        @Query("vote_average.gte") voteAverageGte: Double = 7.0,
        @Query("vote_count.gte") voteCountGte: Int = 100,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("include_video") includeVideo: Boolean = false,
        @Query("language") language: String = "en-US"
    ): MoviesResponseDto
}