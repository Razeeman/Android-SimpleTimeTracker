package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.navigation.ActionResolver
import com.example.util.simpletimetracker.navigation.ActionResolverImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ResolverModule {

    @Binds
    @Singleton
    fun bindActionResolver(impl: ActionResolverImpl): ActionResolver
}