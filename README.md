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

### Test Coverage

✅ **Repository Integration Tests** - Complete data layer testing with network error handling  
✅ **Data Mapper Tests** - DTO ↔ Domain ↔ Entity transformations  
✅ **Use Case Tests** - Business logic validation  
✅ **Domain Model Tests** - Data integrity and operations  
✅ **Date Formatting Tests** - UI logic and edge cases

### Test Structure

```
src/
├── test/               # Unit tests
│   └── java/com/tmukas/filmvault/
│       ├── domain/     # Domain layer tests
│       │   ├── model/  # Movie model tests
│       │   └── usecase/ # Business logic tests (pagination, refresh, favorites)
│       ├── data/       # Data layer tests  
│       │   ├── repository/ # Repository integration tests (39 tests)
│       │   └── mapper/     # Data transformation tests (15 tests)
│       └── presentation/   # Presentation logic tests
│           └── movies/     # ViewModel tests (5 tests) + Date formatting tests
└── androidTest/        # Integration & UI tests (basic structure)
    └── java/com/tmukas/filmvault/
        └── ExampleInstrumentedTest.kt # Basic Android test
```

### Testing Libraries

- **JUnit 5** - Testing framework
- **AssertK** - Fluent assertion library
- **Mockk** - Mocking framework for Kotlin
- **Coroutines Test** - Async testing utilities
- **Room Testing** - Database testing utilities

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run specific test classes
./gradlew testDebugUnitTest --tests "*MovieRepositoryImplTest*"
./gradlew testDebugUnitTest --tests "*MovieMapperTest*" 

# Run instrumented tests
./gradlew connectedAndroidTest

# Run all tests with coverage
./gradlew testDebugUnitTestCoverageVerification
```

### Key Test Scenarios

**Critical functionality covered:**

- ✅ Network error handling (timeout, no internet, IO errors)
- ✅ Data pagination (first page, last page, empty response)
- ✅ Favorite status preservation during updates
- ✅ Data mapping consistency (DTO → Domain → Entity chains)
- ✅ Edge cases (null values, empty lists, invalid dates)
- ✅ Pull-to-refresh functionality
- ✅ Offline caching behavior

📋 **[See detailed testing report](TESTING_SUMMARY.md)**

## Tech Stack

- **Kotlin** - main language
- **Jetpack Compose** - UI framework
- **MVVM + MVI** - architectural pattern
- **Room** - local database
- **Retrofit** - network requests
- **Hilt** - dependency injection
- **Coroutines & Flow** - asynchronous operations
- **Custom Pagination** - manual page management for infinite scrolling

## Technical Decisions

### Custom Pagination Instead of Paging 3

This project implements **custom pagination logic** instead of using Jetpack Paging 3 library for
the following reasons:

- ✅ **Simplicity** - Custom implementation is more straightforward for this test assignment
- ✅ **Learning demonstration** - Shows understanding of pagination concepts without library
  dependency
- ✅ **Full control** - Complete control over loading states and error handling
- ✅ **Testability** - Easier to unit test custom pagination logic
- ✅ **Flexibility** - Custom solution perfectly fits the specific requirements

The pagination implementation includes:

- Manual page tracking (`nextPageToLoad`, `canLoadMore`)
- Loading states management (`Loading`, `LoadingMore`, `Content`)
- Error handling with cached data preservation
- Infinite scrolling with proper state transitions

### Package-based Architecture

Uses **package-based organization** instead of separate Gradle modules for:

- ✅ **Test assignment simplicity** - Easier to review and understand
- ✅ **Build efficiency** - Faster build times for smaller project
- ✅ **Clear separation** - Still maintains Clean Architecture principles
- 📝 **Note**: Production apps would benefit from multi-module approach

### Testing Strategy

Focused on **high-value, reliable tests** rather than comprehensive coverage:

- ✅ **Repository Integration Tests** - Critical data layer testing
- ✅ **Data Mapper Tests** - Ensures data transformation correctness
- ✅ **Use Case Tests** - Business logic validation
- ✅ **ViewModel Tests** - Basic interaction testing (without complex async state testing)
- 📝 **Avoided**: Complex UI state testing to maintain test reliability

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