package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.data.datasource.ArtistRemoteDataSource
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

private class FakeArtistRemoteDataSource : ArtistRemoteDataSource {
    var artists: MutableList<ArtistInfo> = mutableListOf()

    override suspend fun getAllArtists(): List<ArtistInfo> = artists

    override suspend fun getArtistById(id: String): ArtistInfo =
        artists.first { it.id.toString() == id }
}

class ArtistRepositoryTest {

    private lateinit var fakeDataSource: FakeArtistRemoteDataSource
    private lateinit var repository: ArtistRepository

    @Before
    fun setUp() {
        fakeDataSource = FakeArtistRemoteDataSource()
        repository = ArtistRepository(fakeDataSource)
    }

    @After
    fun tearDown() {
        fakeDataSource.artists.clear()
    }

    @Test
    fun getAllArtists_successReturnsList() = runTest {
        fakeDataSource.artists.add(ArtistInfo(id = 1, name = "Artist", profileImageUrl = "url", genre = "Rock"))

        val result = repository.getAllArtists()
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(1)
    }

    @Test
    fun getArtistById_whenNotFound_returnsFailure() = runTest {
        val result = repository.getArtistById("missing")

        assertThat(result.isFailure).isTrue()
    }
}
