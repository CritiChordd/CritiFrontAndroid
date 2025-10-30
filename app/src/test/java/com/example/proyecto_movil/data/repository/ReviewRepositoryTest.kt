package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.datasource.ReviewRemoteDataSource
import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewRepositoryTest {

    private val remoteDataSource: ReviewRemoteDataSource = mockk(relaxed = true)
    private val repository = ReviewRepository(remoteDataSource)

    @After
    fun tearDown() {
        io.mockk.clearAllMocks()
    }

    @Test
    fun getReviewsByUserId_mapsDtoFieldsCorrectly() = runTest {
        val dto = ReviewDto(
            id = "1",
            content = "Increíble álbum",
            score = 4.5,
            is_low_score = false,
            album_id = 42,
            user_id = "user-123",
            firebase_user_id = "firebase-456",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-02",
            is_favorite = true,
            likesCount = 12
        )
        coEvery { remoteDataSource.getReviewsByUserId("user-123") } returns listOf(dto)

        val result = repository.getReviewsByUserId("user-123")

        assertThat(result.isSuccess).isTrue()
        val info = result.getOrNull()?.single()
        assertThat(info?.id).isEqualTo("1")
        assertThat(info?.content).isEqualTo("Increíble álbum")
        assertThat(info?.score).isEqualTo(4.5)
        assertThat(info?.isLowScore).isFalse()
        assertThat(info?.albumId).isEqualTo(42)
        assertThat(info?.userId).isEqualTo("user-123")
        assertThat(info?.firebaseUserId).isEqualTo("firebase-456")
        assertThat(info?.createdAt).isEqualTo("2024-01-01")
        assertThat(info?.updatedAt).isEqualTo("2024-01-02")
        assertThat(info?.isFavorite).isTrue()
        assertThat(info?.likesCount).isEqualTo(12)
        assertThat(info?.liked).isFalse()
    }

    @Test
    fun getAllReviews_mapsDtosIncludingDefaults() = runTest {
        val richDto = ReviewDto(
            id = "10",
            content = "Me encantó",
            score = 9.0,
            is_low_score = false,
            album_id = 99,
            user_id = "rich",
            firebase_user_id = "firebase",
            createdAt = "2024-05-05",
            updatedAt = "2024-05-06",
            is_favorite = true,
            likesCount = 3
        )
        val defaultDto = ReviewDto(id = "11")
        coEvery { remoteDataSource.getAllReviews() } returns listOf(richDto, defaultDto)

        val result = repository.getAllReviews()

        assertThat(result.isSuccess).isTrue()
        val reviews = result.getOrNull()
        assertThat(reviews).hasSize(2)
        val defaults = reviews?.last()
        assertThat(defaults?.id).isEqualTo("11")
        assertThat(defaults?.content).isEmpty()
        assertThat(defaults?.score).isEqualTo(0.0)
        assertThat(defaults?.isLowScore).isFalse()
        assertThat(defaults?.albumId).isEqualTo(0)
        assertThat(defaults?.userId).isEqualTo("")
        assertThat(defaults?.firebaseUserId).isNull()
        assertThat(defaults?.createdAt).isEmpty()
        assertThat(defaults?.updatedAt).isEmpty()
        assertThat(defaults?.isFavorite).isFalse()
        assertThat(defaults?.likesCount).isEqualTo(0)
        assertThat(defaults?.liked).isFalse()
    }

    @Test
    fun getReviewsByAlbumId_mapsDtosForAlbum() = runTest {
        val dtoOne = ReviewDto(id = "21", album_id = 77, user_id = "userA", is_low_score = true)
        val dtoTwo = ReviewDto(id = "22", album_id = 77, user_id = "userB", is_low_score = false)
        coEvery { remoteDataSource.getReviewsByAlbumId("77") } returns listOf(dtoOne, dtoTwo)

        val result = repository.getReviewsByAlbumId(77)

        assertThat(result.isSuccess).isTrue()
        val reviews = result.getOrNull()
        assertThat(reviews).hasSize(2)
        assertThat(reviews?.map { it.id }).containsExactly("21", "22")
        assertThat(reviews?.first()?.albumId).isEqualTo(77)
        assertThat(reviews?.first()?.isLowScore).isTrue()
        assertThat(reviews?.last()?.isLowScore).isFalse()
    }

    @Test
    fun getReviewById_mapsSingleDto() = runTest {
        val dto = ReviewDto(
            id = "100",
            content = "Detallada",
            score = 7.0,
            is_low_score = false,
            album_id = 5,
            user_id = "usr",
            likesCount = 8
        )
        coEvery { remoteDataSource.getReviewById("100", any()) } returns dto

        val result = repository.getReviewById("100")

        assertThat(result.isSuccess).isTrue()
        val info = result.getOrNull()
        assertThat(info?.id).isEqualTo("100")
        assertThat(info?.score).isEqualTo(7.0)
        assertThat(info?.likesCount).isEqualTo(8)
        assertThat(info?.liked).isFalse()
    }

    @Test
    fun createReview_marksDtoAsLowScoreWhenBelowThreshold() = runTest {
        val slot = slot<CreateReviewDto>()
        coEvery { remoteDataSource.createReview(capture(slot)) } returns Unit

        val result = repository.createReview(
            content = "Regular",
            score = 3,
            albumId = 5,
            userId = "user",
            firebaseUserId = "firebase",
            isFavorite = false
        )

        assertThat(result.isSuccess).isTrue()
        val dto = slot.captured
        assertThat(dto.score).isEqualTo(3)
        assertThat(dto.is_low_score).isTrue()
        assertThat(dto.album_id).isEqualTo("5")
        assertThat(dto.user_id).isEqualTo("user")
        assertThat(dto.firebase_user_id).isEqualTo("firebase")
        coVerify(exactly = 1) { remoteDataSource.createReview(any()) }
    }

    @Test
    fun createReview_marksDtoAsHighScoreWhenAboveThreshold() = runTest {
        val slot = slot<CreateReviewDto>()
        coEvery { remoteDataSource.createReview(capture(slot)) } returns Unit

        repository.createReview(
            content = "Excelente",
            score = 9,
            albumId = 12,
            userId = "user",
            firebaseUserId = "firebase",
            isFavorite = false
        )

        val dto = slot.captured
        assertThat(dto.score).isEqualTo(9)
        assertThat(dto.is_low_score).isFalse()
    }

    @Test
    fun createReview_usesEmptyUserIdWhenNullProvided() = runTest {
        val slot = slot<CreateReviewDto>()
        coEvery { remoteDataSource.createReview(capture(slot)) } returns Unit

        repository.createReview(
            content = "Sin usuario",
            score = 6,
            albumId = 1,
            userId = null,
            firebaseUserId = "firebase",
            isFavorite = false
        )

        val dto = slot.captured
        assertThat(dto.user_id).isEqualTo("")
    }

    @Test
    fun createReview_setsFavoriteAndMatchingTimestamps() = runTest {
        val slot = slot<CreateReviewDto>()
        coEvery { remoteDataSource.createReview(capture(slot)) } returns Unit

        repository.createReview(
            content = "Favorita",
            score = 8,
            albumId = 2,
            userId = "user",
            firebaseUserId = "firebase",
            isFavorite = true
        )

        val dto = slot.captured
        assertThat(dto.is_favorite).isTrue()
        assertThat(dto.createdAt).isNotEmpty()
        assertThat(dto.createdAt).isEqualTo(dto.updatedAt)
    }
}
