package com.example.proyecto_movil.data.injection

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FireBaseHiltModule {

    @Singleton
    @Provides
    fun auth(): FirebaseAuth = Firebase.auth

    @Singleton
    @Provides
    fun firestore(): FirebaseFirestore = Firebase.firestore

    @Singleton
    @Provides
    fun storage(): FirebaseStorage = Firebase.storage
}
