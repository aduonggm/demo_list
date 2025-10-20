package com.nvd.demo_list.models

data class NewsFeedItem(
    val id: String,
    val userName: String,
    val userAvatar: String,
    val postTime: String,
    val postText: String,
    val postImage: String,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int
)

// Sample data generator
object NewsFeedData {
    fun getSampleData(): List<NewsFeedItem> = listOf(
        NewsFeedItem(
            id = "1",
            userName = "John Doe",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "2 giờ trước",
            postText = "Enjoying the beautiful sunset today! 🌅",
            postImage = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRagpOhxoJudkxMqDej1pKSBn_9xfLxVemxRg&s",
            likeCount = 124,
            commentCount = 23,
            shareCount = 5
        ),
        NewsFeedItem(
            id = "2",
            userName = "Jane Smith",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "4 giờ trước",
            postText = "Check out this amazing view from my trip!",
            postImage = "https://png.pngtree.com/thumb_back/fh260/background/20210908/pngtree-sky-at-dusk-natural-scenery-beautiful-photography-with-pictures-image_819933.jpg",
            likeCount = 89,
            commentCount = 15,
            shareCount = 3
        ),
        NewsFeedItem(
            id = "3",
            userName = "Mike Johnson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "6 giờ trước",
            postText = "Morning coffee ☕ #MondayMotivation",
            postImage = "https://hoanghamobile.com/tin-tuc/wp-content/uploads/2024/10/tai-anh-phong-canh-dep-1.jpg",
            likeCount = 234,
            commentCount = 45,
            shareCount = 12
        ),
        NewsFeedItem(
            id = "4",
            userName = "Sarah Wilson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "8 giờ trước",
            postText = "Beautiful flowers in the garden 🌺",
            postImage = "https://photo2.tinhte.vn/data/attachment-files/2021/07/5557920_CV.jpg",
            likeCount = 156,
            commentCount = 28,
            shareCount = 7
        ),
        NewsFeedItem(
            id = "5",
            userName = "David Brown",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "10 giờ trước",
            postText = "New adventure begins! 🚀",
            postImage = "https://cdnphoto.dantri.com.vn/aerztjLQz4WGhQnIqEocC_FLsLw=/thumb_w/960/2020/03/03/thanhbinh-1-a-3-docx-1583197236967.jpeg",
            likeCount = 345,
            commentCount = 67,
            shareCount = 18
        ),
        NewsFeedItem(
            id = "6",
            userName = "Emily Davis",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "12 giờ trước",
            postText = "Delicious meal at my favorite restaurant 🍽️",
            postImage = "https://images.pexels.com/photos/33044/sunflower-sun-summer-yellow.jpg?auto=compress&cs=tinysrgb&dpr=1&w=500",
            likeCount = 198,
            commentCount = 34,
            shareCount = 9
        ),
        NewsFeedItem(
            id = "7",
            userName = "Chris Martin",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "14 giờ trước",
            postText = "Workout complete! 💪 #FitnessGoals",
            postImage = "https://hoanghamobile.com/tin-tuc/wp-content/uploads/2023/07/hinh-dep-19.jpg",
            likeCount = 267,
            commentCount = 41,
            shareCount = 11
        ),
        NewsFeedItem(
            id = "8",
            userName = "Lisa Anderson",
            userAvatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            postTime = "16 giờ trước",
            postText = "Reading by the beach 📚",
            postImage = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYGqzCmQswExwQ9Xztcr5bOBOEqX_Gkt2YyZZKwBeUuXyrcnr5vjBUPBLpyF2xgbc6X9k&usqp=CAU",
            likeCount = 178,
            commentCount = 29,
            shareCount = 6
        )
    )
}

