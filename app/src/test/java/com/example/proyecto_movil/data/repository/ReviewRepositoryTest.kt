package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.datasource.ReviewRemoteDataSource
import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.example.proyecto_movil.data.dtos.toReviewInfo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

private class FakeReviewRemoteDataSource : ReviewRemoteDataSource {
    val reviews: MutableList<ReviewDto> = mutableListOf()
    var lastCreated: CreateReviewDto? = null

    override suspend fun getAllReviews(): List<ReviewDto> = reviews

    override suspend fun getReviewById(id: String, currentUserId: String): ReviewDto =
        reviews.first { it.id == id }

    override suspend fun getReviewsByUserId(userId: String): List<ReviewDto> =
        reviews.filter { it.user_id == userId }

    override suspend fun getReviewsByAlbumId(albumId: String): List<ReviewDto> =
        reviews.filter { it.album_id.toString() == albumId }

    override suspend fun createReview(review: CreateReviewDto) {
        lastCreated = review
    }

    override suspend fun deleteReview(id: String) {}
    override suspend fun updateReview(id: String, review: CreateReviewDto) {}
    override suspend fun getReviewReplies(id: String): List<ReviewDto> = emptyList()

    override fun listenAllReviews(): Flow<List<ReviewInfo>> = flowOf(reviews.map { it.toReviewInfo() })

    override suspend fun sendOrDeleteReviewLike(reviewId: String, userId: String) {}
}

class ReviewRepositoryTest {

    private lateinit var fakeDataSource: FakeReviewRemoteDataSource
    private lateinit var repository: ReviewRepository

    @Before
    fun setUp() {
        fakeDataSource = FakeReviewRemoteDataSource()
        repository = ReviewRepository(fakeDataSource)
    }

    @After
    fun tearDown() {
        fakeDataSource.reviews.clear()
        fakeDataSource.lastCreated = null
    }

    @Test
    fun getAllReviews_successMapsToInfo() = runTest {
        fakeDataSource.reviews += ReviewDto(
            id = "r1",
            content = "Great",
            score = 9.0,
            is_low_score = false,
            album_id = 1,
            user_id = "u1",
            firebase_user_id = "fb",
            createdAt = "c",
            updatedAt = "u",
            is_favorite = true,
            likesCount = 0
        )

        val result = repository.getAllReviews()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().first().id).isEqualTo("r1")
    }

    @Test
    fun createReview_populatesDtoFields() = runTest {
        val result = repository.createReview(
            content = "Nice",
            score = 3,
            albumId = 5,
            userId = "user",
            firebaseUserId = "fb",
            isFavorite = true
        )

        assertThat(result.isSuccess).isTrue()
        val created = fakeDataSource.lastCreated
        assertThat(created).isNotNull()
        assertThat(created!!.is_low_score).isTrue()
        assertThat(created.album_id).isEqualTo("5")
    }
}
