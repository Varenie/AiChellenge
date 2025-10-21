package ru.varenie.aichellenge.data.remote

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.varenie.aichellenge.data.repository.HuggingFaceRepositoryImpl
import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideHuggingFaceClient(okHttpClient: OkHttpClient, gson: Gson): HuggingFaceClient =
        HuggingFaceClient(okHttpClient, gson)

    @Provides
    @Singleton
    fun provideRepository(client: HuggingFaceClient): HuggingFaceRepository =
        HuggingFaceRepositoryImpl(client)
}
