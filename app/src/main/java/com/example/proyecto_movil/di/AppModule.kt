package com.example.proyecto_movil.di

import com.example.proyecto_movil.data.datasource.AlbumRemoteDataSource
import com.example.proyecto_movil.data.datasource.ArtistRemoteDataSource
import com.example.proyecto_movil.data.datasource.ReviewRemoteDataSource
import com.example.proyecto_movil.data.datasource.impl.firestore.AlbumFirestoreDataSourceImpl
import com.example.proyecto_movil.data.datasource.impl.firestore.ReviewFirestoreDataSourceImpl
import com.example.proyecto_movil.data.datasource.impl.firestore.UserFirestoreDataSourceImpl
import com.example.proyecto_movil.data.datasource.impl.firestore.NotificationsFirestoreDataSourceImpl
import com.example.proyecto_movil.data.datasource.impl.retrofit.*
import com.example.proyecto_movil.data.datasource.services.*
import com.example.proyecto_movil.data.repository.*
import com.example.proyecto_movil.data.datasource.NotificationsRemoteDataSource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://10.0.2.2:5000/api/"

    // ---------------- Retrofit ----------------
    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // Services
    @Singleton @Provides
    fun provideUserRetrofitService(retrofit: Retrofit): UserRetrofitService =
        retrofit.create(UserRetrofitService::class.java)

    @Singleton @Provides
    fun provideAlbumRetrofitService(retrofit: Retrofit): AlbumRetrofitService =
        retrofit.create(AlbumRetrofitService::class.java)

    @Singleton @Provides
    fun provideReviewRetrofitService(retrofit: Retrofit): ReviewRetrofitService =
        retrofit.create(ReviewRetrofitService::class.java)

    @Singleton @Provides
    fun providePlaylistRetrofitService(retrofit: Retrofit): PlaylistRetrofitService =
        retrofit.create(PlaylistRetrofitService::class.java)

    @Singleton @Provides
    fun provideArtistRetrofitService(retrofit: Retrofit): ArtistRetrofitService =
        retrofit.create(ArtistRetrofitService::class.java)

    // DataSources Retrofit
    @Singleton @Provides
    fun provideAlbumRetrofitDataSource(service: AlbumRetrofitService) =
        AlbumRetrofitDataSourceImpl(service)

    @Singleton @Provides
    fun provideReviewRetrofitDataSource(service: ReviewRetrofitService) =
        ReviewRetrofitDataSourceImplement(service)

    @Singleton @Provides
    fun provideUserRemoteDataSource(service: UserRetrofitService) =
        UserRetrofitDataSourceImpl(service)

    @Singleton @Provides
    fun providePlaylistRemoteDataSource(service: PlaylistRetrofitService) =
        PlaylistRetrofitDataSourceImpl(service)

    @Singleton @Provides
    fun provideArtistRemoteDataSource(service: ArtistRetrofitService): ArtistRemoteDataSource =
        ArtistRetrofitDataSourceImpl(service)

    // ---------------- Firestore DS ----------------
    @Singleton
    @Provides
    fun provideUserFirestoreDataSourceImpl(firestore: FirebaseFirestore) =
        UserFirestoreDataSourceImpl(firestore)

    @Singleton
    @Provides
    fun provideReviewFirestoreDataSource(firestore: FirebaseFirestore) =
        ReviewFirestoreDataSourceImpl(firestore)

    @Singleton
    @Provides
    fun provideReviewRemoteDataSource(
        firestoreDataSource: ReviewFirestoreDataSourceImpl
    ): ReviewRemoteDataSource = firestoreDataSource

    @Singleton
    @Provides
    fun provideNotificationsFirestoreDS(firestore: FirebaseFirestore) =
        NotificationsFirestoreDataSourceImpl(firestore)

    @Singleton
    @Provides
    fun provideNotificationsRemoteDataSource(
        firestoreDS: NotificationsFirestoreDataSourceImpl
    ): NotificationsRemoteDataSource = firestoreDS

    @Singleton
    @Provides
    fun provideAlbumFirestoreDataSource(
        firestore: FirebaseFirestore
    ): AlbumFirestoreDataSourceImpl = AlbumFirestoreDataSourceImpl(firestore)

    @Singleton
    @Provides
    fun provideAlbumRemoteDataSource(
        firestoreDataSource: AlbumFirestoreDataSourceImpl
    ): AlbumRemoteDataSource = firestoreDataSource

    // ---------------- Repositorios ----------------
    @Singleton @Provides
    fun provideAlbumRepository(ds: AlbumRemoteDataSource) = AlbumRepository(ds)

    @Singleton @Provides
    fun provideReviewRepository(ds: ReviewRemoteDataSource) = ReviewRepository(ds)

    @Singleton @Provides
    fun providePlaylistRepository(ds: PlaylistRetrofitDataSourceImpl) = PlaylistRepository(ds)

    @Singleton @Provides
    fun provideUserRepository(
        retrofitDs: UserRetrofitDataSourceImpl,
        firestoreDs: UserFirestoreDataSourceImpl
    ) = UserRepository(retrofitDs, firestoreDs)

    @Singleton @Provides
    fun provideArtistRepository(ds: ArtistRemoteDataSource) = ArtistRepository(ds)

    @Singleton @Provides
    fun provideStorageRemoteDataSource(storage: FirebaseStorage) =
        com.example.proyecto_movil.data.datasource.StorageRemoteDataSource(storage)

    @Singleton @Provides
    fun provideStorageRepository(ds: com.example.proyecto_movil.data.datasource.StorageRemoteDataSource) =
        StorageRepository(ds)

    @Singleton @Provides
    fun provideNotificationsRepository(ds: NotificationsRemoteDataSource) = NotificationsRepository(ds)
}
