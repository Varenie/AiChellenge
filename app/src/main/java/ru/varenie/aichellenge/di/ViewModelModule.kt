package ru.varenie.aichellenge.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import ru.varenie.aichellenge.data.TokenCounter
import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository
import ru.varenie.aichellenge.domain.usecase.GenerateCodeAndTestsUseCase
import ru.varenie.aichellenge.domain.usecase.SendCustomMessageUseCase
import ru.varenie.aichellenge.domain.usecase.SendMessageUseCase
import ru.varenie.aichellenge.domain.usecase.SummarizeUseCase

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun provideGenerateCodeAndTestsUseCase(repository: HuggingFaceRepository): GenerateCodeAndTestsUseCase {
        return GenerateCodeAndTestsUseCase(repository)
    }

    @Provides
    fun provideSummarizeUseCase(repository: HuggingFaceRepository): SummarizeUseCase {
        return SummarizeUseCase(repository)
    }

    @Provides
    fun provideSendMessageUseCase(
        repository: HuggingFaceRepository,
        tokenCounter: TokenCounter,
        summarizeUseCase: SummarizeUseCase
    ): SendMessageUseCase {
        return SendMessageUseCase(repository, tokenCounter, summarizeUseCase)
    }

    @Provides
    fun provideSendCustomMessageUseCase(
        repository: HuggingFaceRepository,
        tokenCounter: TokenCounter,
        summarizeUseCase: SummarizeUseCase
    ): SendCustomMessageUseCase {
        return SendCustomMessageUseCase(repository, tokenCounter, summarizeUseCase)
    }
}