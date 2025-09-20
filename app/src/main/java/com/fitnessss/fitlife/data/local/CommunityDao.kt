package com.fitnessss.fitlife.data.local

import androidx.room.*
import com.fitnessss.fitlife.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface CommunityDao {
    // Post operations
    @Query("SELECT * FROM posts WHERE isPublic = 1 ORDER BY createdAt DESC")
    fun getAllPublicPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUser(userId: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE postType = :postType ORDER BY createdAt DESC")
    fun getPostsByType(postType: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: String): Post?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    // Comment operations
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsByPost(postId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Update
    suspend fun updateComment(comment: Comment)

    @Delete
    suspend fun deleteComment(comment: Comment)

    // Like operations
    @Query("SELECT * FROM likes WHERE postId = :postId")
    fun getLikesByPost(postId: String): Flow<List<Like>>

    @Query("SELECT COUNT(*) FROM likes WHERE postId = :postId")
    suspend fun getLikeCount(postId: String): Int

    @Query("SELECT COUNT(*) FROM likes WHERE postId = :postId AND userId = :userId")
    suspend fun isPostLikedByUser(postId: String, userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: Like)

    @Delete
    suspend fun deleteLike(like: Like)

    // Forum operations
    @Query("SELECT * FROM forums ORDER BY memberCount DESC")
    fun getAllForums(): Flow<List<Forum>>

    @Query("SELECT * FROM forums WHERE category = :category")
    fun getForumsByCategory(category: String): Flow<List<Forum>>

    @Query("SELECT * FROM forums WHERE id = :forumId")
    suspend fun getForumById(forumId: String): Forum?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForum(forum: Forum)

    @Update
    suspend fun updateForum(forum: Forum)

    @Delete
    suspend fun deleteForum(forum: Forum)

    // Forum topic operations
    @Query("SELECT * FROM forum_topics WHERE forumId = :forumId ORDER BY isPinned DESC, createdAt DESC")
    fun getTopicsByForum(forumId: String): Flow<List<ForumTopic>>

    @Query("SELECT * FROM forum_topics WHERE id = :topicId")
    suspend fun getTopicById(topicId: String): ForumTopic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumTopic(topic: ForumTopic)

    @Update
    suspend fun updateForumTopic(topic: ForumTopic)

    @Delete
    suspend fun deleteForumTopic(topic: ForumTopic)

    // Forum reply operations
    @Query("SELECT * FROM forum_replies WHERE topicId = :topicId ORDER BY createdAt ASC")
    fun getRepliesByTopic(topicId: String): Flow<List<ForumReply>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumReply(reply: ForumReply)

    @Update
    suspend fun updateForumReply(reply: ForumReply)

    @Delete
    suspend fun deleteForumReply(reply: ForumReply)

    // Challenge operations
    @Query("SELECT * FROM challenges WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveChallenges(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE id = :challengeId")
    suspend fun getChallengeById(challengeId: String): Challenge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Delete
    suspend fun deleteChallenge(challenge: Challenge)

    // Challenge participant operations
    @Query("SELECT * FROM challenge_participants WHERE challengeId = :challengeId ORDER BY currentProgress DESC")
    fun getChallengeParticipants(challengeId: String): Flow<List<ChallengeParticipant>>

    @Query("SELECT * FROM challenge_participants WHERE userId = :userId")
    fun getChallengesByUser(userId: String): Flow<List<ChallengeParticipant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallengeParticipant(participant: ChallengeParticipant)

    @Update
    suspend fun updateChallengeParticipant(participant: ChallengeParticipant)

    @Delete
    suspend fun deleteChallengeParticipant(participant: ChallengeParticipant)

    // Gym operations
    @Query("SELECT * FROM gyms ORDER BY rating DESC")
    fun getAllGyms(): Flow<List<Gym>>

    @Query("SELECT * FROM gyms WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    fun getGymsByLocation(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): Flow<List<Gym>>

    @Query("SELECT * FROM gyms WHERE id = :gymId")
    suspend fun getGymById(gymId: String): Gym?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGym(gym: Gym)

    @Update
    suspend fun updateGym(gym: Gym)

    @Delete
    suspend fun deleteGym(gym: Gym)

    // Gym review operations
    @Query("SELECT * FROM gym_reviews WHERE gymId = :gymId ORDER BY createdAt DESC")
    fun getGymReviews(gymId: String): Flow<List<GymReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGymReview(review: GymReview)

    @Update
    suspend fun updateGymReview(review: GymReview)

    @Delete
    suspend fun deleteGymReview(review: GymReview)

    // Gym class operations
    @Query("SELECT * FROM gym_classes WHERE gymId = :gymId ORDER BY dayOfWeek ASC, startTime ASC")
    fun getGymClasses(gymId: String): Flow<List<GymClass>>

    @Query("SELECT * FROM gym_classes WHERE gymId = :gymId AND dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getGymClassesByDay(gymId: String, dayOfWeek: Int): Flow<List<GymClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGymClass(gymClass: GymClass)

    @Update
    suspend fun updateGymClass(gymClass: GymClass)

    @Delete
    suspend fun deleteGymClass(gymClass: GymClass)

    // Workout partner operations
    @Query("SELECT * FROM workout_partners WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveWorkoutPartners(): Flow<List<WorkoutPartner>>

    @Query("SELECT * FROM workout_partners WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    fun getWorkoutPartnersByLocation(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): Flow<List<WorkoutPartner>>

    @Query("SELECT * FROM workout_partners WHERE userId = :userId")
    suspend fun getWorkoutPartnerByUser(userId: String): WorkoutPartner?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPartner(partner: WorkoutPartner)

    @Update
    suspend fun updateWorkoutPartner(partner: WorkoutPartner)

    @Delete
    suspend fun deleteWorkoutPartner(partner: WorkoutPartner)
}
