package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.datasource.impl.retrofit.UserRetrofitDataSourceImpl
import com.example.proyecto_movil.data.datasource.services.UserRetrofitService
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.example.proyecto_movil.data.dtos.UpdateUserDto
import com.example.proyecto_movil.data.dtos.UserProfileDto
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
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
    private lateinit var service: FakeUserService
    private lateinit var dataSource: UserRetrofitDataSourceImpl

    @Before
    fun setUp() {
        service = FakeUserService()
        dataSource = UserRetrofitDataSourceImpl(service)
    }

    @After
    fun tearDown() {
        service.reviews = emptyList()
    }

    @Test
    fun getUserById_returnsDto() = runTest {
        val dto = dataSource.getUserById("u1")
        assertThat(dto.username).isEqualTo("john")
    }

    @Test
    fun getUserReviews_returnsDtos() = runTest {
        service.reviews = listOf(ReviewDto(id = "r1", content = "hi"))

        val list = dataSource.getUserReviews("u1")
        assertThat(list.map { it.id }).containsExactly("r1")
    }

    @Test
    fun updateUser_returnsUpdatedDto() = runTest {
        val updated = dataSource.updateUser("u1", UpdateUserDto(username = "pepe", bio = "bio", profile_pic = null))
        assertThat(updated.username).isEqualTo("pepe")
        assertThat(updated.bio).isEqualTo("bio")
    }
}
