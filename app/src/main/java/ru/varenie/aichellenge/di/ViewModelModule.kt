package ru.varenie.aichellenge.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository
import ru.varenie.aichellenge.domain.usecase.GenerateCodeAndTestsUseCase

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun provideGenerateCodeAndTestsUseCase(repository: HuggingFaceRepository): GenerateCodeAndTestsUseCase {
        return GenerateCodeAndTestsUseCase(repository)
    }
}