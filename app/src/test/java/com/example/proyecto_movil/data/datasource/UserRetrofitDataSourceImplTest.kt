package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.datasource.impl.retrofit.UserRetrofitDataSourceImpl
import com.example.proyecto_movil.data.datasource.services.UserRetrofitService
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.example.proyecto_movil.data.dtos.UpdateUserDto
import com.example.proyecto_movil.data.dtos.UserProfileDto
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

private class FakeUserService : UserRetrofitService {
    var user: UserProfileDto = UserProfileDto(
        id = "u1", username = "john", profile_pic = "p", bio = "b",
        followers = 0, following = 0, createdAt = "c", updatedAt = "u", followed = false
    )
    var reviews: List<ReviewDto> = emptyList()

    override suspend fun getUserById(userId: String): UserProfileDto = user

    override suspend fun getUserReviews(userId: String): List<ReviewDto> = reviews

    override suspend fun updateUser(userId: String, body: UpdateUserDto): UserProfileDto =
        user.copy(username = body.username, bio = body.bio, profile_pic = body.profile_pic ?: user.profile_pic)
}

class UserRetrofitDataSourceImplTest {
    @Test
    fun getUserById_returnsDto() = runTest {
        val service = FakeUserService()
        val ds = UserRetrofitDataSourceImpl(service)

        val dto = ds.getUserById("u1")
        assertThat(dto.username).isEqualTo("john")
    }

    @Test
    fun getUserReviews_returnsDtos() = runTest {
        val service = FakeUserService().apply {
            reviews = listOf(ReviewDto(id = "r1", content = "hi"))
        }
        val ds = UserRetrofitDataSourceImpl(service)

        val list = ds.getUserReviews("u1")
        assertThat(list.map { it.id }).containsExactly("r1")
    }

    @Test
    fun updateUser_returnsUpdatedDto() = runTest {
        val service = FakeUserService()
        val ds = UserRetrofitDataSourceImpl(service)

        val updated = ds.updateUser("u1", UpdateUserDto(username = "pepe", bio = "bio", profile_pic = null))
        assertThat(updated.username).isEqualTo("pepe")
        assertThat(updated.bio).isEqualTo("bio")
    }
}

