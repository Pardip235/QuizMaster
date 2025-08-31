package com.pardip.quizmaster.data.di

import com.pardip.quizmaster.data.repository.KahootRepositoryImpl
import com.pardip.quizmaster.domain.repository.KahootRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindKahootRepository(
        impl: KahootRepositoryImpl
    ): KahootRepository
}
