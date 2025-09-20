package com.fitnessss.fitlife.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String? = null,
    val content: String,
    val images: List<String> = emptyList(),
    val postType: PostType,
    val workoutSessionId: String? = null,
    val progressPhotoId: String? = null,
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0,
    val isPublic: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class PostType {
    WORKOUT, PROGRESS, ACHIEVEMENT, GENERAL, CHALLENGE
}

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey
    val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String? = null,
    val content: String,
    val likes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "likes")
data class Like(
    @PrimaryKey
    val id: String,
    val userId: String,
    val postId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "forums")
data class Forum(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val category: ForumCategory,
    val memberCount: Int = 0,
    val postCount: Int = 0,
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ForumCategory {
    GENERAL, WORKOUTS, NUTRITION, PROGRESS, MOTIVATION, TECH_SUPPORT
}

@Entity(tableName = "forum_topics")
data class ForumTopic(
    @PrimaryKey
    val id: String,
    val forumId: String,
    val title: String,
    val content: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String? = null,
    val views: Int = 0,
    val replies: Int = 0,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "forum_replies")
data class ForumReply(
    @PrimaryKey
    val id: String,
    val topicId: String,
    val content: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String? = null,
    val likes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val challengeType: ChallengeType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val goal: Float,
    val goalUnit: String,
    val participants: Int = 0,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ChallengeType {
    WORKOUT_COUNT, WEIGHT_LOSS, MUSCLE_GAIN, STREAK, CALORIES_BURNED, STEPS
}

@Entity(tableName = "challenge_participants")
data class ChallengeParticipant(
    @PrimaryKey
    val id: String,
    val challengeId: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String? = null,
    val currentProgress: Float = 0f,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gyms")
data class Gym(
    @PrimaryKey
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String? = null,
    val website: String? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val imageUrl: String? = null,
    val isVerified: Boolean = false
)

data class GymAmenity(
    val name: String,
    val description: String,
    val iconUrl: String? = null
)

data class MembershipPlan(
    val name: String,
    val price: Float,
    val currency: String = "USD",
    val duration: String, // "monthly", "yearly", etc.
    val features: List<String> = emptyList()
)

@Entity(tableName = "gym_reviews")
data class GymReview(
    @PrimaryKey
    val id: String,
    val gymId: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String? = null,
    val rating: Int, // 1-5
    val review: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gym_classes")
data class GymClass(
    @PrimaryKey
    val id: String,
    val gymId: String,
    val name: String,
    val description: String,
    val instructor: String,
    val dayOfWeek: Int, // 1-7
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val maxParticipants: Int? = null,
    val currentParticipants: Int = 0,
    val classType: ClassType,
    val difficulty: Difficulty,
    val imageUrl: String? = null
)

enum class ClassType {
    YOGA, PILATES, SPINNING, ZUMBA, BOXING, CROSSFIT, HIIT, STRENGTH, CARDIO
}

@Entity(tableName = "workout_partners")
data class WorkoutPartner(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String? = null,
    val fitnessLevel: FitnessLevel,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val bio: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Availability(
    val dayOfWeek: Int, // 1-7
    val startTime: String, // HH:mm
    val endTime: String // HH:mm
)
