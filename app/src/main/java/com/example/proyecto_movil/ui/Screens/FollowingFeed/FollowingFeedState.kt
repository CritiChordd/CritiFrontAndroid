package com.example.proyecto_movil.ui.Screens.FollowingFeed

import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo

data class FeedItem(
    val review: ReviewInfo,
    val author: UserInfo?
)

data class FollowingFeedState(
    val isLoading: Boolean = true,
    val items: List<FeedItem> = emptyList(),
    val error: String? = null,
    val openUserId: String? = null,
    val openReviewId: String? = null
)
