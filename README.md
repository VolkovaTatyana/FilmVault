# FilmVault

Android application for browsing movies and adding them to favorites.

## 🚀 Setup

### Prerequisites

- Android Studio (latest version)
- TMDB API Key

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/VolkovaTatyana/FilmVault.git
   cd FilmVault
   ```

2. **Get TMDB API Key**
    - Go to [TMDB website](https://www.themoviedb.org/)
    - Create an account and login
    - Navigate to Settings > API
    - Generate your API Key

3. **Configure API Key**
    - Open `local.properties` file (create if doesn't exist)
    - Add your API key:
   ```properties
   API_KEY="your_tmdb_api_key_here"
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and click Run ▶️

### ⚠️ Important Notes

- Never commit `local.properties` to version control
- Each developer needs their own TMDB API key
- The app won't work without a valid API key

## 🏛️ Architecture

The project uses **Clean Architecture** with layer separation and is designed to be **testable,
scalable, and flexible**.

### 📁 Domain Layer (`domain/`)
- `model/` - Domain models (Movie)
- `repository/` - Repository interfaces
- `usecase/` - Business logic

### 📁 Data Layer (`data/`)
- `local/` - Local database (Room)
- `remote/` - Network layer (Retrofit + TMDB API)
- `repository/` - Repository implementations

### 📁 Presentation Layer (`presentation/`)

- `MainActivity.kt` - Main entry UI component
- `movies/` - Movies list screen
- `favorites/` - Favorites screen
- `ui/theme/` - App themes and styles

### 📦 Module Structure

**Important Note**: This application uses package-based organization instead of separate Gradle
modules for **simplification purposes** as this is a test assignment. In a real-world production
application, the following would be implemented as **separate Gradle modules**:

- `:app` - Application module
- `:domain` - Business logic module
- `:data` - Data layer module
- `:presentation` - UI layer module

This modular approach provides:

- Better build times through parallel compilation
- Clearer dependency boundaries
- Enhanced reusability
- Improved team collaboration

## 🧪 Testing

The application is thoroughly tested with a focus on **testability, reliability, and maintainability
**:

### Test Types

- **Unit Tests** - Domain models, use cases, and business logic
- **Integration Tests** - Repository implementations and data flow
- **UI Tests** - User interactions and navigation flows

### Test Structure

```
src/
├── test/               # Unit tests
│   └── java/com/tmukas/filmvault/
│       ├── domain/     # Domain layer tests
│       ├── data/       # Data layer tests
│       └── presentation/ # Presentation logic tests
└── androidTest/        # Integration & UI tests
    └── java/com/tmukas/filmvault/
        ├── database/   # Room database tests
        ├── api/        # Network layer tests
        └── ui/         # Compose UI tests
```

### Testing Libraries

- **JUnit 5** - Testing framework
- **AssertK** - Fluent assertion library
- **Mockk** - Mocking framework
- **Compose Testing** - UI testing for Jetpack Compose
- **Room Testing** - Database testing utilities

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run all tests with coverage
./gradlew testDebugUnitTestCoverageVerification
```

## Tech Stack

- **Kotlin** - main language
- **Jetpack Compose** - UI framework
- **MVVM + MVI** - architectural pattern
- **Room** - local database
- **Retrofit** - network requests
- **Hilt** - dependency injection
- **Coroutines & Flow** - asynchronous operations
- **Custom Pagination** - manual page management for infinite scrolling

## API

The app uses [The Movie Database (TMDB) API](https://www.themoviedb.org/documentation/api) to fetch
movie data.

## Features

- ✅ Browse movies list with pagination
- ✅ Group movies by months
- ✅ Add/remove movies from favorites
- ✅ Offline viewing (cached data)
- ✅ Pull-to-refresh
- ✅ Tab navigation between All movies and Favorites
- ✅ Error handling with retry functionality