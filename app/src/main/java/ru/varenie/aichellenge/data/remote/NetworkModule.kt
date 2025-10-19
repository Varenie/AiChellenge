package ru.varenie.aichellenge.data.remote

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.varenie.aichellenge.data.repository.OpenAiRepositoryImpl
import ru.varenie.aichellenge.domain.repository.OpenAiRepository
import ru.varenie.aichellenge.domain.usecase.GenerateTechSpecUseCase
import ru.varenie.aichellenge.domain.usecase.SendMessageUseCase
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
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    @Provides
    @Singleton
    fun provideOpenAiApi(retrofit: Retrofit): OpenAiApi =
        retrofit.create(OpenAiApi::class.java)

    @Provides
    @Singleton
    fun provideRepository(api: OpenAiApi): OpenAiRepository = OpenAiRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideSendMessageUseCase(repository: OpenAiRepository): SendMessageUseCase =
        SendMessageUseCase(repository)

    @Provides
    @Singleton
    fun provideGenerateTechSpecUseCase(repository: OpenAiRepository): GenerateTechSpecUseCase =
        GenerateTechSpecUseCase(repository)
}
