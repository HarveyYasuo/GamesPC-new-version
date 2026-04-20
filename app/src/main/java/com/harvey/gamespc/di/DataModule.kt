package com.harvey.gamespc.di

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.harvey.gamespc.data.repository.FirebaseGameRepository
import com.harvey.gamespc.data.repository.GameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }

    @Provides
    @Singleton
    fun provideGameRepository(databaseReference: DatabaseReference): GameRepository {
        return FirebaseGameRepository(databaseReference)
    }

    /**
     * Como FirebaseGameRepository es una implementación concreta, 
     * Hilt necesita saber cómo proporcionarla si el ViewModel la pide directamente.
     */
    @Provides
    @Singleton
    fun provideFirebaseGameRepository(databaseReference: DatabaseReference): FirebaseGameRepository {
        return FirebaseGameRepository(databaseReference)
    }
}
