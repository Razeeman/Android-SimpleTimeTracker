package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.navigation.ActionResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [ResolverModule::class])
interface TestResolverModule {

    @Binds
    @Singleton
    fun bindActionResolver(impl: TestActionResolverImpl): ActionResolver
}