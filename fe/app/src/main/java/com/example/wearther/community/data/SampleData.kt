package com.example.wearther.community.data

object SampleData {
    val sampleFriendSuggestions = listOf(
        FriendSuggestion(
            id = "user1",
            name = "김패션",
            profileImage = "https://example.com/profile1.jpg",
            mutualFriends = 5,
            isFollowing = false
        ),
        FriendSuggestion(
            id = "user2",
            name = "이스타일",
            profileImage = "https://example.com/profile2.jpg",
            mutualFriends = 3,
            isFollowing = true
        ),
        FriendSuggestion(
            id = "user3",
            name = "박코디",
            profileImage = "https://example.com/profile3.jpg",
            mutualFriends = 8,
            isFollowing = false
        ),
        FriendSuggestion(
            id = "user4",
            name = "최트렌드",
            profileImage = "https://example.com/profile4.jpg",
            mutualFriends = 2,
            isFollowing = false
        ),
        FriendSuggestion(
            id = "user5",
            name = "정패션",
            profileImage = "https://example.com/profile5.jpg",
            mutualFriends = 6,
            isFollowing = true
        )
    )
}
