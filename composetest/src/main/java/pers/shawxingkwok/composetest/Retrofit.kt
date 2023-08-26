package pers.shawxingkwok.composetest

import androidx.annotation.VisibleForTesting
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pers.shawxingkwok.ktutil.fastLazy
import retrofit2.Retrofit

private const val BASE_URL = "https://api.unsplash.com/"

@VisibleForTesting
internal var logger by fastLazy {
    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
}

// Use this to get logger if it is recycled after being used
@VisibleForTesting
internal var getLogger = {
    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
}

@VisibleForTesting
internal var client by fastLazy {
    OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()
}

var retrofit = Retrofit.Builder().baseUrl(BASE_URL).client(client).build()
    @VisibleForTesting
    internal set