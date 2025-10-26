package ru.varenie.aichellenge.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.varenie.aichellenge.data.TokenCounter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideTokenCounter(): TokenCounter {
        return TokenCounter()
    }
}
