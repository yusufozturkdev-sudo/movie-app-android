package com.yusufozturk.cinetrack.data.api

import com.yusufozturk.cinetrack.data.model.AccountResponse
import com.yusufozturk.cinetrack.data.model.CreateSessionBody
import com.yusufozturk.cinetrack.data.model.DeleteSessionBody
import com.yusufozturk.cinetrack.data.model.MovieDetail
import com.yusufozturk.cinetrack.data.model.MovieResponse
import com.yusufozturk.cinetrack.data.model.RateMovieBody
import com.yusufozturk.cinetrack.data.model.RequestTokenResponse
import com.yusufozturk.cinetrack.data.model.SessionResponse
import com.yusufozturk.cinetrack.data.model.StatusResponse
import com.yusufozturk.cinetrack.data.model.VideoResponse
import com.yusufozturk.cinetrack.data.model.WatchlistRequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): MovieDetail

    @GET("discover/movie")
    suspend fun discoverMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): VideoResponse

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String
    ): MovieResponse

    // --- Kimlik doğrulama ---

    @GET("authentication/token/new")
    suspend fun createRequestToken(
        @Query("api_key") apiKey: String
    ): RequestTokenResponse

    @POST("authentication/session/new")
    suspend fun createSession(
        @Query("api_key") apiKey: String,
        @Body body: CreateSessionBody
    ): SessionResponse

    @HTTP(method = "DELETE", path = "authentication/session", hasBody = true)
    suspend fun deleteSession(
        @Query("api_key") apiKey: String,
        @Body body: DeleteSessionBody
    ): StatusResponse

    @GET("account")
    suspend fun getAccount(
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String
    ): AccountResponse

    // --- Hesaba bağlı watchlist ---

    @POST("account/{account_id}/watchlist")
    suspend fun updateWatchlist(
        @Path("account_id") accountId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String,
        @Body body: WatchlistRequestBody
    ): StatusResponse

    @GET("account/{account_id}/watchlist/movies")
    suspend fun getAccountWatchlist(
        @Path("account_id") accountId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String
    ): MovieResponse

    // --- Hesaba bağlı puanlama ---

    @POST("movie/{movie_id}/rating")
    suspend fun rateMovie(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String,
        @Body body: RateMovieBody
    ): StatusResponse

    @GET("account/{account_id}/rated/movies")
    suspend fun getRatedMovies(
        @Path("account_id") accountId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String
    ): MovieResponse
}