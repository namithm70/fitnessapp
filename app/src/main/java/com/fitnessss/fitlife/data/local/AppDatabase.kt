package com.fitnessss.fitlife.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fitnessss.fitlife.data.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@Database(
    entities = [
        User::class,
        Workout::class,
        Exercise::class,
        WorkoutSession::class,
        Food::class,
        Meal::class,
        NutritionLog::class,
        WaterLog::class,
        MealPlan::class,
        NutritionGoals::class,
        ProgressMeasurement::class,
        ProgressPhoto::class,
        PersonalRecord::class,
        Achievement::class,
        UserAchievement::class,
        WorkoutStats::class,
        Post::class,
        Comment::class,
        Like::class,
        Forum::class,
        ForumTopic::class,
        ForumReply::class,
        Challenge::class,
        ChallengeParticipant::class,
        Gym::class,
        GymReview::class,
        GymClass::class,
        WorkoutPartner::class,
        ActivityHistory::class,
        DailySummary::class,
        WeeklySummary::class,
        MonthlySummary::class,
        ActivityStreak::class,
        Milestone::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun progressDao(): ProgressDao
    abstract fun communityDao(): CommunityDao
    abstract fun historyDao(): HistoryDao

}

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun fromActivityType(value: ActivityType): String {
        return value.name
    }

    @TypeConverter
    fun toActivityType(value: String): ActivityType {
        return ActivityType.valueOf(value)
    }

    @TypeConverter
    fun fromMood(value: Mood?): String? {
        return value?.name
    }

    @TypeConverter
    fun toMood(value: String?): Mood? {
        return value?.let { Mood.valueOf(it) }
    }

    @TypeConverter
    fun fromEnergyLevel(value: EnergyLevel?): String? {
        return value?.name
    }

    @TypeConverter
    fun toEnergyLevel(value: String?): EnergyLevel? {
        return value?.let { EnergyLevel.valueOf(it) }
    }

    @TypeConverter
    fun fromMilestoneType(value: MilestoneType): String {
        return value.name
    }

    @TypeConverter
    fun toMilestoneType(value: String): MilestoneType {
        return MilestoneType.valueOf(value)
    }





    @TypeConverter
    fun fromFitnessLevel(value: FitnessLevel): String {
        return value.name
    }

    @TypeConverter
    fun toFitnessLevel(value: String): FitnessLevel {
        return FitnessLevel.valueOf(value)
    }

    @TypeConverter
    fun fromFitnessGoalList(value: List<FitnessGoal>): String {
        return value.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toFitnessGoalList(value: String): List<FitnessGoal> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { FitnessGoal.valueOf(it) }
    }

    @TypeConverter
    fun fromMuscleGroupList(value: List<MuscleGroup>): String {
        return value.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toMuscleGroupList(value: String): List<MuscleGroup> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { MuscleGroup.valueOf(it) }
    }

    @TypeConverter
    fun fromWorkoutType(value: WorkoutType): String {
        return value.name
    }

    @TypeConverter
    fun toWorkoutType(value: String): WorkoutType {
        return WorkoutType.valueOf(value)
    }

    @TypeConverter
    fun fromDifficulty(value: Difficulty): String {
        return value.name
    }

    @TypeConverter
    fun toDifficulty(value: String): Difficulty {
        return Difficulty.valueOf(value)
    }

    @TypeConverter
    fun fromEquipmentList(value: List<Equipment>): String {
        return value.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toEquipmentList(value: String): List<Equipment> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { Equipment.valueOf(it) }
    }

    @TypeConverter
    fun fromMealType(value: MealType): String {
        return value.name
    }

    @TypeConverter
    fun toMealType(value: String): MealType {
        return MealType.valueOf(value)
    }

    @TypeConverter
    fun fromPhotoType(value: PhotoType): String {
        return value.name
    }

    @TypeConverter
    fun toPhotoType(value: String): PhotoType {
        return PhotoType.valueOf(value)
    }

    @TypeConverter
    fun fromRecordType(value: RecordType): String {
        return value.name
    }

    @TypeConverter
    fun toRecordType(value: String): RecordType {
        return RecordType.valueOf(value)
    }

    @TypeConverter
    fun fromAchievementCategory(value: AchievementCategory): String {
        return value.name
    }

    @TypeConverter
    fun toAchievementCategory(value: String): AchievementCategory {
        return AchievementCategory.valueOf(value)
    }

    @TypeConverter
    fun fromPostType(value: PostType): String {
        return value.name
    }

    @TypeConverter
    fun toPostType(value: String): PostType {
        return PostType.valueOf(value)
    }

    @TypeConverter
    fun fromForumCategory(value: ForumCategory): String {
        return value.name
    }

    @TypeConverter
    fun toForumCategory(value: String): ForumCategory {
        return ForumCategory.valueOf(value)
    }

    @TypeConverter
    fun fromChallengeType(value: ChallengeType): String {
        return value.name
    }

    @TypeConverter
    fun toChallengeType(value: String): ChallengeType {
        return ChallengeType.valueOf(value)
    }

    @TypeConverter
    fun fromClassType(value: ClassType): String {
        return value.name
    }

    @TypeConverter
    fun toClassType(value: String): ClassType {
        return ClassType.valueOf(value)
    }

    @TypeConverter
    fun fromGender(value: Gender?): String? {
        return value?.name
    }

    @TypeConverter
    fun toGender(value: String?): Gender? {
        return value?.let { Gender.valueOf(it) }
    }

    @TypeConverter
    fun fromNutritionInfo(value: NutritionInfo?): String? {
        return value?.let { "${it.calories},${it.protein},${it.carbs},${it.fat}" }
    }

    @TypeConverter
    fun toNutritionInfo(value: String?): NutritionInfo? {
        return value?.let {
            val parts = it.split(",")
            if (parts.size == 4) {
                NutritionInfo(
                    calories = parts[0].toFloatOrNull() ?: 0f,
                    protein = parts[1].toFloatOrNull() ?: 0f,
                    carbs = parts[2].toFloatOrNull() ?: 0f,
                    fat = parts[3].toFloatOrNull() ?: 0f
                )
            } else null
        }
    }

    @TypeConverter
    fun fromBodyMeasurements(value: BodyMeasurements?): String? {
        return value?.let { "${it.chest},${it.waist},${it.hips},${it.biceps},${it.forearms},${it.thighs},${it.calves},${it.neck},${it.shoulders}" }
    }

    @TypeConverter
    fun toBodyMeasurements(value: String?): BodyMeasurements? {
        return value?.let {
            val parts = it.split(",")
            if (parts.size == 9) {
                BodyMeasurements(
                    chest = parts[0].toFloatOrNull(),
                    waist = parts[1].toFloatOrNull(),
                    hips = parts[2].toFloatOrNull(),
                    biceps = parts[3].toFloatOrNull(),
                    forearms = parts[4].toFloatOrNull(),
                    thighs = parts[5].toFloatOrNull(),
                    calves = parts[6].toFloatOrNull(),
                    neck = parts[7].toFloatOrNull(),
                    shoulders = parts[8].toFloatOrNull()
                )
            } else null
        }
    }

    @TypeConverter
    fun fromAchievementRequirements(value: AchievementRequirements?): String? {
        return value?.let { "${it.workoutCount},${it.totalWorkoutTime},${it.consecutiveDays},${it.weightLoss},${it.muscleGain},${it.personalRecords},${it.streakDays}" }
    }

    @TypeConverter
    fun toAchievementRequirements(value: String?): AchievementRequirements? {
        return value?.let {
            val parts = it.split(",")
            if (parts.size == 7) {
                AchievementRequirements(
                    workoutCount = parts[0].toIntOrNull(),
                    totalWorkoutTime = parts[1].toIntOrNull(),
                    consecutiveDays = parts[2].toIntOrNull(),
                    weightLoss = parts[3].toFloatOrNull(),
                    muscleGain = parts[4].toFloatOrNull(),
                    personalRecords = parts[5].toIntOrNull(),
                    streakDays = parts[6].toIntOrNull()
                )
            } else null
        }
    }

    @TypeConverter
    fun fromGymAmenityList(value: List<GymAmenity>): String {
        return value.joinToString(",") { "${it.name}|${it.description}|${it.iconUrl}" }
    }

    @TypeConverter
    fun toGymAmenityList(value: String): List<GymAmenity> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { 
            val parts = it.split("|")
            if (parts.size == 3) {
                GymAmenity(
                    name = parts[0],
                    description = parts[1],
                    iconUrl = if (parts[2] == "null") null else parts[2]
                )
            } else GymAmenity("", "", null)
        }
    }

    @TypeConverter
    fun fromMembershipPlanList(value: List<MembershipPlan>): String {
        return value.joinToString(",") { "${it.name}|${it.price}|${it.duration}" }
    }

    @TypeConverter
    fun toMembershipPlanList(value: String): List<MembershipPlan> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { 
            val parts = it.split("|")
            if (parts.size == 3) {
                MembershipPlan(
                    name = parts[0],
                    price = parts[1].toFloatOrNull() ?: 0f,
                    duration = parts[2]
                )
            } else MembershipPlan("", 0f, "USD", "")
        }
    }

    @TypeConverter
    fun fromAvailabilityList(value: List<Availability>): String {
        return value.joinToString(",") { "${it.dayOfWeek}|${it.startTime}|${it.endTime}" }
    }

    @TypeConverter
    fun toAvailabilityList(value: String): List<Availability> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { 
            val parts = it.split("|")
            if (parts.size == 3) {
                Availability(
                    dayOfWeek = parts[0].toIntOrNull() ?: 1,
                    startTime = parts[1],
                    endTime = parts[2]
                )
            } else Availability(1, "", "")
        }
    }
}
