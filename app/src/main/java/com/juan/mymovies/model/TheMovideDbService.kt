package com.juan.mymovies.model

import com.juan.mymovies.model.MovieDbResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMovideDbService {

    @GET("movie/popular")
    suspend fun listPopularMovies(
        @Query("api_key") apiKey : String,
        @Query("region") region: String) : MovieDbResult
}