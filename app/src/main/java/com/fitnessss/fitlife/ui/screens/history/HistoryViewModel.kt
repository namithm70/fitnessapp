package com.fitnessss.fitlife.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.snapshotFlow
import com.fitnessss.fitlife.data.local.HistoryDao
import com.fitnessss.fitlife.data.model.*
import com.fitnessss.fitlife.data.service.HistoryLoggingService
import com.fitnessss.fitlife.data.UserProgressManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DatePeriod
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyDao: HistoryDao,
    private val historyLoggingService: HistoryLoggingService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val currentUserId = "current_user" // TODO: Get from auth

    init {
        // Initial data load
        loadHistoryData()

        // Observe changes in UserProgressManager's workout sessions
        // to automatically refresh the history screen.
        viewModelScope.launch {
            snapshotFlow { UserProgressManager.workoutSessions.value }
                .collect {
                    println("DEBUG: HistoryViewModel detected a change in workout sessions. Reloading data.")
                    loadHistoryData()
                }
        }
    }

    private fun loadHistoryData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, activities = emptyList())

                // 1. Fetch non-workout activities from the database
                val nonWorkoutActivitiesFlow = historyDao.getActivitiesOtherThan(currentUserId, ActivityType.WORKOUT_COMPLETED.name)
                
                nonWorkoutActivitiesFlow.collect { nonWorkoutActivities ->
                    println("DEBUG: HistoryViewModel loaded ${nonWorkoutActivities.size} non-workout activities from database")

                    // 2. Get all workout sessions from the single source of truth: UserProgressManager
                    val workoutSessions = UserProgressManager.workoutSessions.value
                    println("DEBUG: HistoryViewModel loaded ${workoutSessions.size} workout sessions from UserProgressManager")
                    
                    // 3. Convert workout sessions to the ActivityHistory format for display
                    val workoutActivities = workoutSessions.map { session ->
                        ActivityHistory(
                            id = session.id,
                            userId = currentUserId,
                            activityType = ActivityType.WORKOUT_COMPLETED,
                            title = "Completed ${session.workoutName}",
                            description = "Completed ${session.workoutName} (${session.workoutType})",
                            date = kotlinx.datetime.LocalDate(session.date.year, session.date.monthValue, session.date.dayOfMonth),
                            timestamp = kotlinx.datetime.LocalDateTime(session.date.year, session.date.monthValue, session.date.dayOfMonth, session.date.hour, session.date.minute, session.date.second),
                            duration = session.duration,
                            calories = session.caloriesBurned,
                            notes = "Workout Type: ${session.workoutType}"
                        )
                    }
                    
                    // 4. Combine the two lists and sort them by date
                    val allActivities = (nonWorkoutActivities + workoutActivities)
                        .sortedByDescending { it.timestamp }
                    
                    println("DEBUG: HistoryViewModel combined all activities. Total count: ${allActivities.size}")
                    
                    // 5. Update the UI state
                    _uiState.value = _uiState.value.copy(
                        activities = allActivities,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load history: ${e.message}"
                )
            }
        }
    }

    fun filterByDateRange(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            try {
                val activities = historyDao.getActivitiesByDateRange(currentUserId, startDate, endDate)
                    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

                val dailySummaries = historyDao.getDailySummariesByDateRange(currentUserId, startDate, endDate)
                    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

                activities.collect { activitiesList ->
                    // Get workout sessions from UserProgressManager within the date range
                    val workoutSessions = UserProgressManager.workoutSessions.value.filter { session ->
                        val sessionDate = kotlinx.datetime.LocalDate(
                            session.date.year,
                            session.date.monthValue,
                            session.date.dayOfMonth
                        )
                        sessionDate >= startDate && sessionDate <= endDate
                    }
                    
                    // Convert filtered workout sessions to ActivityHistory format
                    val workoutActivities = workoutSessions.map { session ->
                        ActivityHistory(
                            id = session.id,
                            userId = currentUserId,
                            activityType = ActivityType.WORKOUT_COMPLETED,
                            title = "Completed ${session.workoutName}",
                            description = "Completed ${session.workoutName} (${session.workoutType}) with ${session.exercisesCompleted} exercises and ${session.setsCompleted} sets (${session.caloriesBurned} calories burned)",
                            date = kotlinx.datetime.LocalDate(
                                session.date.year,
                                session.date.monthValue,
                                session.date.dayOfMonth
                            ),
                            timestamp = kotlinx.datetime.LocalDateTime(
                                session.date.year,
                                session.date.monthValue,
                                session.date.dayOfMonth,
                                session.date.hour,
                                session.date.minute,
                                session.date.second
                            ),
                            duration = session.duration,
                            calories = session.caloriesBurned,
                            notes = "Workout Type: ${session.workoutType}"
                        )
                    }
                    
                    // Combine and filter activities
                    val allActivities = (activitiesList + workoutActivities)
                        .distinctBy { it.id }
                        .sortedByDescending { it.timestamp }
                    
                    _uiState.value = _uiState.value.copy(
                        activities = allActivities,
                        selectedStartDate = startDate,
                        selectedEndDate = endDate
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error filtering data"
                )
            }
        }
    }

    fun filterByActivityType(activityType: ActivityType?) {
        viewModelScope.launch {
            try {
                val activities = if (activityType != null) {
                    historyDao.getActivitiesByType(currentUserId, activityType.name)
                        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
                } else {
                    historyDao.getAllActivities(currentUserId)
                        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
                }

                activities.collect { activitiesList ->
                    // Get workout sessions from UserProgressManager if filtering for workouts or showing all
                    val workoutActivities = if (activityType == null || activityType == ActivityType.WORKOUT_COMPLETED) {
                        val workoutSessions = UserProgressManager.workoutSessions.value
                        workoutSessions.map { session ->
                            ActivityHistory(
                                id = session.id,
                                userId = currentUserId,
                                activityType = ActivityType.WORKOUT_COMPLETED,
                                title = "Completed ${session.workoutName}",
                                description = "Completed ${session.workoutName} (${session.workoutType}) with ${session.exercisesCompleted} exercises and ${session.setsCompleted} sets (${session.caloriesBurned} calories burned)",
                                date = kotlinx.datetime.LocalDate(
                                    session.date.year,
                                    session.date.monthValue,
                                    session.date.dayOfMonth
                                ),
                                timestamp = kotlinx.datetime.LocalDateTime(
                                    session.date.year,
                                    session.date.monthValue,
                                    session.date.dayOfMonth,
                                    session.date.hour,
                                    session.date.minute,
                                    session.date.second
                                ),
                                duration = session.duration,
                                calories = session.caloriesBurned,
                                notes = "Workout Type: ${session.workoutType}"
                            )
                        }
                    } else {
                        emptyList()
                    }
                    
                    // Combine and filter activities
                    val allActivities = (activitiesList + workoutActivities)
                        .distinctBy { it.id }
                        .sortedByDescending { it.timestamp }
                    
                    _uiState.value = _uiState.value.copy(
                        activities = allActivities,
                        selectedActivityType = activityType
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error filtering by activity type"
                )
            }
        }
    }

    fun searchActivities(query: String) {
        viewModelScope.launch {
            try {
                val activities = historyDao.searchActivities(currentUserId, query)
                    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

                activities.collect { activities ->
                    _uiState.value = _uiState.value.copy(
                        activities = activities,
                        searchQuery = query
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error searching activities"
                )
            }
        }
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedActivityType = null,
            selectedStartDate = null,
            selectedEndDate = null
        )
        loadHistoryData()
    }
    
    fun refreshData() {
        println("DEBUG: HistoryViewModel.refreshData() called")
        loadHistoryData()
    }

    fun addActivity(activity: ActivityHistory) {
        viewModelScope.launch {
            try {
                historyDao.insertActivity(activity)
                // Refresh data
                loadHistoryData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error adding activity"
                )
            }
        }
    }

    fun updateDailySummary(summary: DailySummary) {
        viewModelScope.launch {
            try {
                historyDao.updateDailySummary(summary)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error updating daily summary"
                )
            }
        }
    }

    fun updateActivityStreak(streak: ActivityStreak) {
        viewModelScope.launch {
            try {
                historyDao.updateActivityStreak(streak)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error updating activity streak"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Clear any existing fake data and ensure only real user activities are shown
    fun clearAllData() {
        viewModelScope.launch {
            try {
                // Clear all activities for the current user
                historyDao.deleteAllActivitiesForUser(currentUserId)
                // Refresh the UI to show empty state
                loadHistoryData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error clearing data"
                )
            }
        }
    }




}

data class HistoryUiState(
    val activities: List<ActivityHistory> = emptyList(),
    val dailySummaries: List<DailySummary> = emptyList(),
    val weeklySummaries: List<WeeklySummary> = emptyList(),
    val monthlySummaries: List<MonthlySummary> = emptyList(),
    val activityStreaks: List<ActivityStreak> = emptyList(),
    val milestones: List<Milestone> = emptyList(),
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val selectedActivityType: ActivityType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val activitiesGroupedByDate: Map<LocalDate, List<ActivityHistory>> by lazy {
        activities.groupBy { it.date }
    }

    val totalActivities: Int get() = activities.size
    val totalWorkouts: Int get() = activities.count { it.activityType == ActivityType.WORKOUT_COMPLETED }
    val totalWorkoutTime: Int get() = activities.filter { it.activityType == ActivityType.WORKOUT_COMPLETED }.sumOf { it.duration ?: 0 }
    val totalCaloriesBurned: Int get() = activities.sumOf { it.calories ?: 0 }
    val totalCaloriesConsumed: Int get() = activities.filter { it.activityType == ActivityType.NUTRITION_LOGGED }.sumOf { it.calories ?: 0 }
    val totalWaterIntake: Float get() = 0f // TODO: Implement water intake tracking separately
}
