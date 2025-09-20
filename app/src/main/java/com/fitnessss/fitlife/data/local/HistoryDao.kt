package com.fitnessss.fitlife.data.local

import androidx.room.*
import com.fitnessss.fitlife.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@Dao
interface HistoryDao {
    // Activity History operations
    @Query("SELECT * FROM activity_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllActivities(userId: String): Flow<List<ActivityHistory>>

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getActivitiesByDate(userId: String, date: LocalDate): Flow<List<ActivityHistory>>

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND activityType = :activityType ORDER BY timestamp DESC")
    fun getActivitiesByType(userId: String, activityType: String): Flow<List<ActivityHistory>>

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND activityType != :activityTypeToExclude ORDER BY timestamp DESC")
    fun getActivitiesOtherThan(userId: String, activityTypeToExclude: String): Flow<List<ActivityHistory>>

    @Query("SELECT * FROM activity_history WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getActivitiesByDateRange(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<ActivityHistory>>

    @Query("SELECT * FROM activity_history WHERE id = :activityId")
    suspend fun getActivityById(activityId: String): ActivityHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityHistory)

    @Update
    suspend fun updateActivity(activity: ActivityHistory)

    @Delete
    suspend fun deleteActivity(activity: ActivityHistory)

    @Query("DELETE FROM activity_history WHERE userId = :userId")
    suspend fun deleteAllActivitiesForUser(userId: String)

    // Daily Summary operations
    @Query("SELECT * FROM daily_summaries WHERE userId = :userId AND date = :date")
    suspend fun getDailySummary(userId: String, date: LocalDate): DailySummary?

    @Query("SELECT * FROM daily_summaries WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun getRecentDailySummaries(userId: String, limit: Int = 30): Flow<List<DailySummary>>

    @Query("SELECT * FROM daily_summaries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getDailySummariesByDateRange(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<DailySummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummary(summary: DailySummary)

    @Update
    suspend fun updateDailySummary(summary: DailySummary)

    // Weekly Summary operations
    @Query("SELECT * FROM weekly_summaries WHERE userId = :userId AND weekStartDate = :weekStartDate")
    suspend fun getWeeklySummary(userId: String, weekStartDate: LocalDate): WeeklySummary?

    @Query("SELECT * FROM weekly_summaries WHERE userId = :userId ORDER BY weekStartDate DESC LIMIT :limit")
    fun getRecentWeeklySummaries(userId: String, limit: Int = 12): Flow<List<WeeklySummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklySummary(summary: WeeklySummary)

    @Update
    suspend fun updateWeeklySummary(summary: WeeklySummary)

    // Monthly Summary operations
    @Query("SELECT * FROM monthly_summaries WHERE userId = :userId AND year = :year AND month = :month")
    suspend fun getMonthlySummary(userId: String, year: Int, month: Int): MonthlySummary?

    @Query("SELECT * FROM monthly_summaries WHERE userId = :userId ORDER BY year DESC, month DESC LIMIT :limit")
    fun getRecentMonthlySummaries(userId: String, limit: Int = 12): Flow<List<MonthlySummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlySummary(summary: MonthlySummary)

    @Update
    suspend fun updateMonthlySummary(summary: MonthlySummary)

    // Activity Streak operations
    @Query("SELECT * FROM activity_streaks WHERE userId = :userId AND activityType = :activityType")
    suspend fun getActivityStreak(userId: String, activityType: String): ActivityStreak?

    @Query("SELECT * FROM activity_streaks WHERE userId = :userId")
    fun getAllActivityStreaks(userId: String): Flow<List<ActivityStreak>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityStreak(streak: ActivityStreak)

    @Update
    suspend fun updateActivityStreak(streak: ActivityStreak)

    // Milestone operations
    @Query("SELECT * FROM milestones WHERE userId = :userId ORDER BY achievedAt DESC")
    fun getAllMilestones(userId: String): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE userId = :userId AND milestoneType = :milestoneType ORDER BY achievedAt DESC")
    fun getMilestonesByType(userId: String, milestoneType: String): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE userId = :userId AND achievedAt BETWEEN :startDate AND :endDate ORDER BY achievedAt DESC")
    fun getMilestonesByDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone)

    @Update
    suspend fun updateMilestone(milestone: Milestone)

    @Delete
    suspend fun deleteMilestone(milestone: Milestone)

    // Statistics and Analytics
    @Query("SELECT COUNT(*) FROM activity_history WHERE userId = :userId AND activityType = :activityType AND date BETWEEN :startDate AND :endDate")
    suspend fun getActivityCountByTypeAndDateRange(userId: String, activityType: String, startDate: LocalDate, endDate: LocalDate): Int

    @Query("SELECT SUM(duration) FROM activity_history WHERE userId = :userId AND activityType = :activityType AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalDurationByTypeAndDateRange(userId: String, activityType: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT SUM(calories) FROM activity_history WHERE userId = :userId AND activityType = :activityType AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalCaloriesByTypeAndDateRange(userId: String, activityType: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT AVG(calories) FROM activity_history WHERE userId = :userId AND activityType = :activityType AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageCaloriesByTypeAndDateRange(userId: String, activityType: String, startDate: LocalDate, endDate: LocalDate): Float?

    // Most active days
    @Query("SELECT date, COUNT(*) as activityCount FROM activity_history WHERE userId = :userId AND date BETWEEN :startDate AND :endDate GROUP BY date ORDER BY activityCount DESC LIMIT :limit")
    suspend fun getMostActiveDays(userId: String, startDate: LocalDate, endDate: LocalDate, limit: Int = 10): List<DateActivityCount>

    // Activity type distribution
    @Query("SELECT activityType, COUNT(*) as count FROM activity_history WHERE userId = :userId AND date BETWEEN :startDate AND :endDate GROUP BY activityType")
    suspend fun getActivityTypeDistribution(userId: String, startDate: LocalDate, endDate: LocalDate): List<ActivityTypeCount>

    // Recent activities for dashboard
    @Query("SELECT * FROM activity_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivities(userId: String, limit: Int = 10): Flow<List<ActivityHistory>>

    // Search activities
    @Query("SELECT * FROM activity_history WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    fun searchActivities(userId: String, query: String): Flow<List<ActivityHistory>>
}

data class DateActivityCount(
    val date: LocalDate,
    val activityCount: Int
)

data class ActivityTypeCount(
    val activityType: String,
    val count: Int
)
