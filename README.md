# FitLife - Health & Fitness Gym Android App

A comprehensive Android mobile application for gym members and fitness enthusiasts to track progress, access workout plans, and engage with fitness content anytime, anywhere.

## 🏋️ Features

### Core Features
- **User Registration & Profile Management**
  - Email/password registration
  - Social login (Google, Facebook) via Firebase Authentication
  - Interactive onboarding flow
  - Profile dashboard with photo upload and metrics

- **Workout Library & Plans**
  - Browse and filter workouts by muscle group, type, equipment, duration
  - Exercise details with sets, reps, rest time
  - Embedded videos (YouTube API) or offline GIF animations
  - AI-powered personalized workout suggestions
  - Swipe-to-complete exercise tracking with timers and notes

- **Progress Tracking**
  - Mobile-optimized charts for weight trends, body measurements, workout consistency
  - Date-stamped before/after photo gallery
  - Gamified badges & milestone unlock animations
  - Weekly/monthly summaries with PDF export

- **Nutrition & Meal Planning**
  - Searchable food database with barcode scanner
  - Quick-add foods and nutrition logging
  - Visual progress bars for daily macro goals
  - Water intake tracker
  - Drag-and-drop weekly meal planner with auto grocery list

- **Community Features**
  - In-app discussion forums with push notifications
  - Monthly challenges with leaderboards
  - Social feed for sharing workouts, photos, achievements
  - Location-based workout partner matching

- **Educational Content**
  - Mobile reading mode for blog-style articles
  - Video tutorials via YouTube API
  - Fitness calculators (BMI, calorie needs, body fat, 1RM)

- **Gym Integration**
  - Map-based gym locator with navigation
  - Class schedules with calendar integration
  - Equipment availability updates
  - Gym reviews and ratings

## 🏗️ Architecture

### Tech Stack
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: Hilt (Dagger Hilt)
- **Local Database**: Room Persistence Library
- **Backend**: Firebase (Authentication, Firestore, Storage, Messaging)
- **Networking**: Retrofit & OkHttp
- **Image Loading**: Coil
- **Navigation**: Android Navigation Component (Compose)
- **Charts**: Vico Compose
- **Camera**: CameraX
- **Maps**: Google Maps SDK
- **Barcode Scanning**: ZXing Android Embedded
- **Animations**: Lottie
- **YouTube Integration**: Android YouTube Player

### Project Structure
```
app/src/main/java/com/fitnessss/fitlife/
├── data/
│   ├── local/           # Room database and DAOs
│   ├── model/           # Data classes and entities
│   └── remote/          # API services and repositories
├── di/                  # Hilt dependency injection modules
├── navigation/          # Navigation components
├── ui/
│   ├── screens/         # Compose UI screens
│   │   ├── auth/        # Authentication screens
│   │   ├── dashboard/   # Dashboard screen
│   │   ├── workout/     # Workout-related screens
│   │   ├── nutrition/   # Nutrition screens
│   │   ├── progress/    # Progress tracking screens
│   │   ├── community/   # Community features
│   │   └── profile/     # Profile management
│   └── theme/           # App theming and styling
└── utils/               # Utility classes and extensions
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 34 or later
- Kotlin 1.9.0 or later
- Google Play Services (for maps and location features)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/fitlife-android.git
   cd fitlife-android
   ```

2. **Set up Firebase**
   - Create a new Firebase project
   - Add your Android app to the project
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Authentication, Firestore, Storage, and Messaging services

3. **Configure Google Maps API**
   - Get a Google Maps API key from Google Cloud Console
   - Add the key to your `local.properties` file:
     ```properties
     MAPS_API_KEY=your_maps_api_key_here
     ```

4. **Build and run**
   ```bash
   ./gradlew build
   ```

### Configuration

#### Firebase Setup
1. Enable Email/Password authentication
2. Configure Google Sign-In
3. Set up Firestore security rules
4. Configure Firebase Storage for user uploads

#### API Keys Required
- **Firebase**: `google-services.json`
- **Google Maps**: For gym locator and location features
- **YouTube API**: For embedded workout videos
- **Nutrition API**: For food database (optional)

## 📱 Screenshots

The app includes the following main screens:

### Authentication
- Login screen with email/password and social login
- Registration screen with profile setup
- Onboarding flow for new users

### Main App
- **Dashboard**: Overview of daily stats, workout of the day, quick actions
- **Workouts**: Browse workout library, filter by categories, view details
- **Nutrition**: Daily nutrition summary, food logging, meal planning
- **Progress**: Progress charts, photo tracking, measurements
- **Community**: Social feed, forums, challenges, workout partners
- **Profile**: User profile, settings, achievements

### Detailed Screens
- Workout detail with exercise list and tracking
- Meal planner with weekly calendar view
- Food search with barcode scanning
- Progress photos with before/after comparison
- Body measurements with charts and history
- Profile editing with form fields and settings

## 🔧 Development

### Building for Development
```bash
./gradlew assembleDebug
```

### Running Tests
```bash
./gradlew test
```

### Code Style
The project follows Kotlin coding conventions and uses ktlint for code formatting.

### Key Dependencies

#### Core Android
- `androidx.core:core-ktx`
- `androidx.lifecycle:lifecycle-runtime-ktx`
- `androidx.activity:activity-compose`

#### Compose
- `androidx.compose.ui:ui`
- `androidx.compose.material3:material3`
- `androidx.compose.ui:ui-tooling-preview`

#### Firebase
- `com.google.firebase:firebase-auth`
- `com.google.firebase:firebase-firestore`
- `com.google.firebase:firebase-storage`
- `com.google.firebase:firebase-messaging`

#### Database
- `androidx.room:room-runtime`
- `androidx.room:room-ktx`
- `androidx.room:room-compiler`

#### Navigation
- `androidx.navigation:navigation-compose`

#### Dependency Injection
- `com.google.dagger:hilt-android`
- `androidx.hilt:hilt-navigation-compose`

#### Networking
- `com.squareup.retrofit2:retrofit`
- `com.squareup.retrofit2:converter-gson`
- `com.squareup.okhttp3:logging-interceptor`

#### UI Components
- `io.coil-kt:coil-compose` (Image loading)
- `androidx.camera:camera-camera2` (Camera)
- `com.google.android.gms:play-services-maps` (Maps)
- `com.airbnb.android:lottie-compose` (Animations)
- `com.patrykandpatrick.vico:compose` (Charts)

## 🎯 Roadmap

### Phase 1 (Current)
- ✅ Basic app structure and navigation
- ✅ UI screens and components
- ✅ Data models and Room database
- ✅ Dependency injection setup
- ✅ Theme and styling

### Phase 2 (Next)
- 🔄 Firebase integration
- 🔄 Authentication flow
- 🔄 Real data persistence
- 🔄 Basic workout tracking

### Phase 3 (Future)
- 📋 AI-powered workout suggestions
- 📋 Advanced progress analytics
- 📋 Social features and community
- 📋 Gym integration
- 📋 Offline capabilities
- 📋 Push notifications

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Material Design 3 for the UI components
- Firebase for backend services
- Google Maps for location features
- YouTube API for video content
- The fitness community for inspiration and feedback

## 📞 Support

For support, email support@fitlife.com or create an issue in this repository.

---

**FitLife** - Your journey to a healthier lifestyle starts here! 💪
