package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.datasource.impl.retrofit.ReviewRetrofitDataSourceImplement
import com.example.proyecto_movil.data.datasource.services.ReviewRetrofitService
import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Test

private class FakeReviewService : ReviewRetrofitService {
    var reviews: MutableList<ReviewDto> = mutableListOf()

    override suspend fun getAllReviews(): List<ReviewDto> = reviews
    override suspend fun getReviewById(id: String): ReviewDto = reviews.first { it.id == id }
    override suspend fun getReviewsByUserId(userId: String): List<ReviewDto> = reviews.filter { it.user_id == userId }
    override suspend fun getReviewsByAlbumId(albumId: String): List<ReviewDto> = reviews.filter { it.album_id.toString() == albumId }
    override suspend fun createReview(review: CreateReviewDto) {
        reviews.add(
            ReviewDto(
                id = "new",
                content = review.content,
                score = review.score.toDouble(),
                is_low_score = review.is_low_score,
                album_id = review.album_id.toIntOrNull() ?: 0,
                user_id = review.user_id,
                firebase_user_id = review.firebase_user_id,
                createdAt = review.createdAt,
                updatedAt = review.updatedAt,
                is_favorite = review.is_favorite
            )
        )
    }
    override suspend fun updateReview(id: String, review: CreateReviewDto) {}
    override suspend fun deleteReview(id: String) { reviews.removeIf { it.id == id } }
    override suspend fun getReviewReplies(id: String): List<ReviewDto> = emptyList()
}

class ReviewRetrofitDataSourceImplementTest {

    @Test
    fun getAllReviews_returnsDtos() = runTest {
        val service = FakeReviewService().apply {
            reviews.add(ReviewDto(id = "r1", content = "c"))
        }
        val ds = ReviewRetrofitDataSourceImplement(service)

        val list = ds.getAllReviews()
        assertThat(list).hasSize(1)
        assertThat(list.first().id).isEqualTo("r1")
    }

    @Test
    fun listenAllReviews_emptyFlow() = runTest {
        val ds = ReviewRetrofitDataSourceImplement(FakeReviewService())
        val emitted = ds.listenAllReviews().firstOrNull()
        assertThat(emitted).isNull()
    }
}

