package com.fitnessss.fitlife.di

import android.content.Context
import androidx.room.Room
import com.fitnessss.fitlife.data.local.*
import com.fitnessss.fitlife.data.service.HistoryLoggingService
import com.fitnessss.fitlife.data.service.DataClearingService
import com.fitnessss.fitlife.data.service.FirebaseService
import com.fitnessss.fitlife.data.service.FirebaseAuthService
import com.fitnessss.fitlife.data.service.DataSyncManager
import com.fitnessss.fitlife.data.service.BiometricAuthManager
import com.fitnessss.fitlife.data.service.SessionManager
import com.fitnessss.fitlife.data.service.AIService
import com.fitnessss.fitlife.data.service.BackupManager
import com.fitnessss.fitlife.data.service.WebRTCService
import com.fitnessss.fitlife.data.service.TwilioTokenService
import com.fitnessss.fitlife.data.service.CallNotificationService
import com.fitnessss.fitlife.data.service.MessageNotificationService
import com.fitnessss.fitlife.data.repository.CallRepository
import com.fitnessss.fitlife.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.FirebaseApp

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fitlife_database"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideWorkoutDao(database: AppDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    fun provideNutritionDao(database: AppDatabase): NutritionDao {
        return database.nutritionDao()
    }

    @Provides
    fun provideProgressDao(database: AppDatabase): ProgressDao {
        return database.progressDao()
    }

    @Provides
    fun provideCommunityDao(database: AppDatabase): CommunityDao {
        return database.communityDao()
    }

    @Provides
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideHistoryLoggingService(historyDao: HistoryDao): HistoryLoggingService {
        return HistoryLoggingService(historyDao)
    }

    @Provides
    @Singleton
    fun provideDataClearingService(historyDao: HistoryDao): DataClearingService {
        return DataClearingService(historyDao)
    }

    // Firebase Services
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        // Use default instance first, which should automatically use the bucket from google-services.json
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuthService(auth: FirebaseAuth, firestore: FirebaseFirestore): FirebaseAuthService {
        return FirebaseAuthService(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideFirebaseService(firestore: FirebaseFirestore, auth: FirebaseAuth): FirebaseService {
        return FirebaseService(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideDataSyncManager(firebaseService: FirebaseService, firebaseAuthService: FirebaseAuthService): DataSyncManager {
        return DataSyncManager(firebaseService, firebaseAuthService)
    }

    @Provides
    @Singleton
    fun provideBiometricAuthManager(@ApplicationContext context: Context): BiometricAuthManager {
        return BiometricAuthManager(context)
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAIService(@ApplicationContext context: Context): AIService {
        return AIService(context)
    }

    @Provides
    @Singleton
    fun provideBackupManager(
        firebaseService: FirebaseService,
        firebaseAuthService: FirebaseAuthService,
        @ApplicationContext context: Context
    ): BackupManager {
        return BackupManager(firebaseService, firebaseAuthService, context)
    }
    
    @Provides
    @Singleton
    fun provideCallRepository(firestore: FirebaseFirestore, auth: FirebaseAuth): CallRepository {
        return CallRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storage: FirebaseStorage
    ): ChatRepository {
        // Ensure max retry times for storage uploads are reasonable via default instance
        return ChatRepository(firestore, auth, storage)
    }
    
    @Provides
    @Singleton
    fun provideWebRTCService(@ApplicationContext context: Context, twilioTokenService: TwilioTokenService): WebRTCService {
        return WebRTCService(context, twilioTokenService)
    }
    
    @Provides
    @Singleton
    fun provideCallNotificationService(@ApplicationContext context: Context): CallNotificationService {
        return CallNotificationService(context)
    }
    
    @Provides
    @Singleton
    fun provideMessageNotificationService(@ApplicationContext context: Context): MessageNotificationService {
        return MessageNotificationService(context)
    }

}
