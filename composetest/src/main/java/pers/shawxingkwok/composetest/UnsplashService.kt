package pers.shawxingkwok.composetest

import androidx.annotation.VisibleForTesting
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// Same package components: retrofit

/* Or declare this if retrofit is from another package
    @VisibleForTesting
    internal var retrofit = com.xx.retrofit
*/

interface UnsplashService {
    companion object : UnsplashService by retrofit.create(UnsplashService::class.java)

    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("client_id") clientId: String = "UNSPLASH_ACCESS_KEY"
    )
    : UnsplashSearchResponse
}

data class UnsplashSearchResponse(
    @field:SerializedName("results") val results: List<UnsplashPhoto>,
    @field:SerializedName("total_pages") val totalPages: Int
)

data class UnsplashPhoto(
    @field:SerializedName("id") val id: String,
)