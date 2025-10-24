package com.example.proyecto_movil.data.dtos

import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
class DtoMappingTest {

    @Before
    fun setUp() {
        // No-op placeholder for symmetry with tearDown
    }

    @After
    fun tearDown() {
        // No-op placeholder for future cleanup if mappings change
    }

    @Test
    fun albumDto_toAlbumInfo_mapsAllFieldsAndDefaults() {
        val dto = AlbumDto(
            id = 10,
            title = null,
            year = null,
            coverUrl = null,
            artist = ArtistDto(id = 5, name = null, imageUrl = null, genre = null)
        )

        val info = dto.toAlbumInfo()

        assertThat(info.id).isEqualTo(10)
        assertThat(info.title).isNotEmpty()
        assertThat(info.year).isNotEmpty()
        assertThat(info.coverUrl).contains("placehold.co")
        assertThat(info.artist).isEqualTo(
            ArtistInfo(
                id = 5,
                name = "Desconocido",
                profileImageUrl = "https://placehold.co/300x300?text=No+Image",
                genre = "Sin género"
            )
        )
    }

    @Test
    fun artistDto_toArtistInfo_mapsFieldsAndDefaults() {
        val dto = ArtistDto(id = 1, name = null, imageUrl = null, genre = null)
        val info = dto.toArtistInfo()

        assertThat(info.id).isEqualTo(1)
        assertThat(info.name).isEqualTo("Desconocido")
        assertThat(info.profileImageUrl).contains("placehold.co")
        assertThat(info.genre).isEqualTo("Sin género")
    }

    @Test
    fun reviewDto_toReviewInfo_mapsAllFields() {
        val dto = ReviewDto(
            id = "r1",
            content = "Great",
            score = 9.0,
            is_low_score = false,
            album_id = 99,
            user_id = "u1",
            firebase_user_id = "fb1",
            createdAt = "c",
            updatedAt = "u",
            is_favorite = true,
            likesCount = 12
        )

        val info = dto.toReviewInfo()

        assertThat(info).isEqualTo(
            ReviewInfo(
                id = "r1",
                content = "Great",
                score = 9.0,
                isLowScore = false,
                albumId = 99,
                userId = "u1",
                firebaseUserId = "fb1",
                createdAt = "c",
                updatedAt = "u",
                isFavorite = true,
                likesCount = 12,
                liked = false
            )
        )
    }

    @Test
    fun userProfileDto_toUserUI_mapsAllFields() {
        val dto = UserProfileDto(
            id = "123",
            username = "pepe",
            profile_pic = "http://image",
            bio = "bio",
            followers = 1,
            following = 2,
            createdAt = "c",
            updatedAt = "u",
            followed = true
        )

        val ui = dto.toUserUI()
        assertThat(ui.id).isEqualTo("123")
        assertThat(ui.username).isEqualTo("pepe")
        assertThat(ui.profileImageUrl).isEqualTo("http://image")
        assertThat(ui.bio).isEqualTo("bio")
        assertThat(ui.followers).isEqualTo(1)
        assertThat(ui.following).isEqualTo(2)
        assertThat(ui.followed).isTrue()
        assertThat(ui.backendUserId).isEqualTo("123")
    }
}
