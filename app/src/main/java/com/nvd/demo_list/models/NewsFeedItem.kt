package com.nvd.demo_list.models

// Enum for post privacy settings
enum class PostPrivacy {
    PUBLIC, FRIENDS, ONLY_ME
}

// Enum for different types of reactions
enum class ReactionType {
    LIKE, LOVE, HAHA, WOW, SAD, ANGRY
}

// Data class for post reactions
data class ReactionCount(
    val like: Int = 0,
    val love: Int = 0,
    val haha: Int = 0,
    val wow: Int = 0,
    val sad: Int = 0,
    val angry: Int = 0
) {
    fun getTotalCount(): Int = like + love + haha + wow + sad + angry

    fun getTopReactions(): List<ReactionType> {
        val reactions = mutableListOf<Pair<ReactionType, Int>>()
        if (like > 0) reactions.add(ReactionType.LIKE to like)
        if (love > 0) reactions.add(ReactionType.LOVE to love)
        if (haha > 0) reactions.add(ReactionType.HAHA to haha)
        if (wow > 0) reactions.add(ReactionType.WOW to wow)
        if (sad > 0) reactions.add(ReactionType.SAD to sad)
        if (angry > 0) reactions.add(ReactionType.ANGRY to angry)

        return reactions.sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
}

data class NewsFeedItem(
    val id: String,
    val userName: String,
    val userAvatar: String,
    val postTime: String,
    val postText: String,
    val postImage: String? = null,
    val privacy: PostPrivacy = PostPrivacy.PUBLIC,
    val reactions: ReactionCount = ReactionCount(),
    val commentCount: Int,
    val shareCount: Int,
    val isSponsored: Boolean = false,
    val location: String? = null
)

// Sample data generator
object NewsFeedData {
    fun getSampleData(): List<NewsFeedItem> = listOf(
        NewsFeedItem(
            id = "1",
            userName = "John Doe",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "2 giờ trước",
            postText = "Enjoying the beautiful sunset today! 🌅 Nothing beats this view!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/da_0.jpg",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 45, love = 67, wow = 12),
            commentCount = 23,
            shareCount = 5,
            location = "Nha Trang Beach"
        ),
        NewsFeedItem(
            id = "2",
            userName = "Jane Smith",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "4 giờ trước",
            postText = "Check out this amazing view from my trip! Can't believe how beautiful this place is 😍",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/da_1.jpg",
            privacy = PostPrivacy.FRIENDS,
            reactions = ReactionCount(like = 34, love = 45, wow = 10),
            commentCount = 15,
            shareCount = 3,
            location = "Da Lat"
        ),
        NewsFeedItem(
            id = "3",
            userName = "Mike Johnson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "6 giờ trước",
            postText = "Morning coffee ☕ #MondayMotivation\n\nStarting the week right with a good cup of coffee and positive vibes!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/xa_0.jpg",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 150, love = 56, haha = 28),
            commentCount = 45,
            shareCount = 12
        ),
        NewsFeedItem(
            id = "4",
            userName = "Sarah Wilson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "8 giờ trước",
            postText = "Beautiful flowers in the garden 🌺 Spring has finally arrived!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/xa_1.jpg",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 78, love = 68, wow = 10),
            commentCount = 28,
            shareCount = 7,
            location = "My Garden"
        ),
        NewsFeedItem(
            id = "5",
            userName = "David Brown",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "10 giờ trước",
            postText = "New adventure begins! 🚀\n\nExcited to announce my new journey. Life is about taking chances and making memories!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/xc_0.jpg",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 189, love = 134, wow = 22),
            commentCount = 67,
            shareCount = 18,
            isSponsored = false
        ),
        NewsFeedItem(
            id = "6",
            userName = "Emily Davis",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "12 giờ trước",
            postText = "Delicious meal at my favorite restaurant 🍽️ Highly recommend to everyone!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/xc_1.jpg",
            privacy = PostPrivacy.FRIENDS,
            reactions = ReactionCount(like = 98, love = 78, haha = 22),
            commentCount = 34,
            shareCount = 9,
            location = "The Golden Lotus Restaurant"
        ),
        NewsFeedItem(
            id = "7",
            userName = "Tech Store Pro",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "14 giờ trước",
            postText = "🔥 BIG SALE! Up to 50% off on all electronics! Don't miss out!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/da_0.jpg",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 234, love = 33),
            commentCount = 41,
            shareCount = 11,
            isSponsored = true
        ),
        NewsFeedItem(
            id = "8",
            userName = "Lisa Anderson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "16 giờ trước",
            postText = "Reading by the beach 📚 Perfect way to spend the weekend!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/da_1.jpg",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 89, love = 67, haha = 22),
            commentCount = 29,
            shareCount = 6,
            location = "Phu Quoc Island"
        ),
        NewsFeedItem(
            id = "9",
            userName = "Tom Wilson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "1 ngày trước",
            postText = "Feeling grateful for all the amazing people in my life ❤️ Thank you all for your support!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/xa_0.jpg",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 234, love = 189, haha = 12),
            commentCount = 78,
            shareCount = 15
        ),
        NewsFeedItem(
            id = "10",
            userName = "Anna Martinez",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "1 ngày trước",
            postText = "Just finished my new painting! What do you think? 🎨",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/xa_1.jpg",
            privacy = PostPrivacy.FRIENDS,
            reactions = ReactionCount(like = 156, love = 89, wow = 34),
            commentCount = 52,
            shareCount = 8
        )
    )

    fun getSamplePdfData(): List<NewsFeedItem> = listOf(
        NewsFeedItem(
            id = "1",
            userName = "John Doe",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "2 giờ trước",
            postText = "Enjoying the beautiful sunset today! 🌅 Nothing beats this view!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/pdf_1.pdf",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 45, love = 67, wow = 12),
            commentCount = 23,
            shareCount = 5,
            location = "Nha Trang Beach"
        ),
        NewsFeedItem(
            id = "2",
            userName = "Jane Smith",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "4 giờ trước",
            postText = "Check out this amazing view from my trip! Can't believe how beautiful this place is 😍",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/pdf_2.pdf",
            privacy = PostPrivacy.FRIENDS,
            reactions = ReactionCount(like = 34, love = 45, wow = 10),
            commentCount = 15,
            shareCount = 3,
            location = "Da Lat"
        ),
        NewsFeedItem(
            id = "3",
            userName = "Mike Johnson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "6 giờ trước",
            postText = "Morning coffee ☕ #MondayMotivation\n\nStarting the week right with a good cup of coffee and positive vibes!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/pdf_3.pdf",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 150, love = 56, haha = 28),
            commentCount = 45,
            shareCount = 12
        ),
        NewsFeedItem(
            id = "4",
            userName = "Sarah Wilson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "8 giờ trước",
            postText = "Beautiful flowers in the garden 🌺 Spring has finally arrived!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/pdf_1.pdf",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 78, love = 68, wow = 10),
            commentCount = 28,
            shareCount = 7,
            location = "My Garden"
        ),
        NewsFeedItem(
            id = "5",
            userName = "David Brown",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "10 giờ trước",
            postText = "New adventure begins! 🚀\n\nExcited to announce my new journey. Life is about taking chances and making memories!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/pdf_2.pdf",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 189, love = 134, wow = 22),
            commentCount = 67,
            shareCount = 18,
            isSponsored = false
        ),
        NewsFeedItem(
            id = "6",
            userName = "Emily Davis",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "12 giờ trước",
            postText = "Delicious meal at my favorite restaurant 🍽️ Highly recommend to everyone!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/pdf_3.pdf",
            privacy = PostPrivacy.FRIENDS,
            reactions = ReactionCount(like = 98, love = 78, haha = 22),
            commentCount = 34,
            shareCount = 9,
            location = "The Golden Lotus Restaurant"
        ),
        NewsFeedItem(
            id = "7",
            userName = "Tech Store Pro",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "14 giờ trước",
            postText = "🔥 BIG SALE! Up to 50% off on all electronics! Don't miss out!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/pdf_1.pdf",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 234, love = 33),
            commentCount = 41,
            shareCount = 11,
            isSponsored = true
        ),
        NewsFeedItem(
            id = "8",
            userName = "Lisa Anderson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "16 giờ trước",
            postText = "Reading by the beach 📚 Perfect way to spend the weekend!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/pdf_2.pdf",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 89, love = 67, haha = 22),
            commentCount = 29,
            shareCount = 6,
            location = "Phu Quoc Island"
        ),
        NewsFeedItem(
            id = "9",
            userName = "Tom Wilson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "1 ngày trước",
            postText = "Feeling grateful for all the amazing people in my life ❤️ Thank you all for your support!",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/pdf_3.pdf",
            privacy = PostPrivacy.PUBLIC,
            reactions = ReactionCount(like = 234, love = 189, haha = 12),
            commentCount = 78,
            shareCount = 15
        ),
        NewsFeedItem(
            id = "10",
            userName = "Anna Martinez",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "1 ngày trước",
            postText = "Just finished my new painting! What do you think? 🎨",
            postImage = "https://raw.githubusercontent.com/truongRikkeisoft/poster_image/refs/heads/main/pdf_1.pdf",
            privacy = PostPrivacy.FRIENDS,
            reactions = ReactionCount(like = 156, love = 89, wow = 34),
            commentCount = 52,
            shareCount = 8
        )
    )
}

