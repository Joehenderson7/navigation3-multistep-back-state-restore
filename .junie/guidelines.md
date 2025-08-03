# Development Guidelines

## Build/Configuration Instructions

### Project Setup
This is an Android project using **Jetpack Compose with Material 3 design** and **Navigation 3** (alpha). The project follows modern Android development practices with version catalogs and Kotlin DSL.

#### Key Dependencies & Versions
- **Android Gradle Plugin**: 8.10.1
- **Kotlin**: 2.0.21 with Compose Compiler Plugin
- **Compose BOM**: 2024.09.00
- **Navigation 3**: 1.0.0-alpha02 (experimental)
- **Target SDK**: 36, **Min SDK**: 24
- **Java Version**: 11

#### Required Build Configuration
```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    compileSdk = 36
    defaultConfig {
        minSdk = 24
        targetSdk = 36
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}
```

#### Essential Dependencies
- **Kotlin Serialization**: `kotlinx-serialization-json:1.6.3`
- **DateTime**: `kotlinx-datetime:0.5.0`
- **Navigation 3**: Alpha version for experimental navigation features
- **Material 3**: Full Material Design 3 support with dynamic theming

### Architecture Notes

#### Current Architecture Pattern
The project currently uses **traditional Compose state management** rather than strict MVI architecture:
- State management with `rememberSaveable` and `mutableStateOf`
- Tab-based navigation with animated transitions
- Composable functions with local state management

#### For MVI Implementation
To implement proper MVI architecture, consider:
- **Model**: Data classes and state representations
- **View**: Compose UI components (already implemented)
- **Intent**: User actions and events (needs implementation)
- Add ViewModel layer with state management
- Implement proper unidirectional data flow

#### Room Database Integration
Room is mentioned in requirements but not yet implemented. To add Room:
```kotlin
// Add to app/build.gradle.kts dependencies
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
```

### Material 3 Theme Configuration
The project implements Material 3 theming with:
- **Dynamic Color Support**: Android 12+ dynamic theming
- **Custom TopAppBar Colors**: Defined in `Theme.kt`
- **Light/Dark Theme Support**: Automatic system theme detection

## Testing Information

### Test Configuration
The project uses standard Android testing setup:
- **Unit Tests**: JUnit 4.13.2
- **Instrumented Tests**: AndroidX Test 1.2.1 with Espresso 3.6.1
- **Compose Testing**: `ui-test-junit4` for Compose UI testing

### Running Tests

#### Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.example.navigator3example.StandardTest"

# Run with debug output
./gradlew test --info
```

#### Instrumented Tests (UI Tests)
```bash
# Run all instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run specific UI test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigator3example.StandardsScreenTest
```

### Adding New Tests

#### Unit Test Example
```kotlin
// app/src/test/java/com/example/navigator3example/YourTest.kt
class YourTest {
    @Test
    fun yourFunction_withValidInput_returnsExpectedResult() {
        // Given
        val input = "test"
        
        // When
        val result = yourFunction(input)
        
        // Then
        assertEquals("expected", result)
    }
}
```

#### Compose UI Test Example
```kotlin
// app/src/androidTest/java/com/example/navigator3example/YourUITest.kt
@RunWith(AndroidJUnit4::class)
class YourUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun yourComposable_displaysCorrectContent() {
        composeTestRule.setContent {
            Navigator3ExampleTheme {
                YourComposable()
            }
        }
        
        composeTestRule.onNodeWithText("Expected Text").assertIsDisplayed()
    }
}
```

### Test Examples Verification
✅ **StandardTest**: Unit tests for data classes and utility functions (4/4 tests passing)
✅ **StandardsScreenTest**: Compose UI tests for screen components (2/2 tests passing)

## Additional Development Information

### Code Style & Conventions

#### File Organization
```
app/src/main/java/com/example/navigator3example/
├── MainActivity.kt                    # Entry point
├── navigation/                        # Navigation components
│   ├── NavHost.kt                    # Main navigation with tabs
│   ├── TopBar.kt                     # Top app bar component
│   ├── NavBar.kt                     # Navigation bar component
│   └── NukeGauge.kt                  # Standards screen (data entry)
└── ui/
    ├── theme/                        # Material 3 theming
    │   ├── Theme.kt                  # Main theme configuration
    │   ├── Color.kt                  # Color definitions
    │   └── Type.kt                   # Typography definitions
    └── BasicActivity.kt              # Basic activity template
```

#### Naming Conventions
- **Composables**: PascalCase (e.g., `StandardsScreen`, `TopBar`)
- **Data Classes**: PascalCase (e.g., `Standard`, `TabItem`)
- **Functions**: camelCase (e.g., `convertMillisToDate`)
- **Variables**: camelCase with descriptive names

#### State Management Patterns
- Use `rememberSaveable` for state that should survive configuration changes
- Use `mutableStateOf` for local component state
- Implement proper state hoisting for shared state

### Navigation 3 Specific Notes
- **Alpha Version**: Navigation 3 is experimental - expect API changes
- **Tab Navigation**: Implemented with `TabRow` and `AnimatedContent`
- **State Preservation**: Uses `rememberSaveable` for tab selection persistence

### Material 3 Implementation Details
- **Dynamic Theming**: Automatically adapts to system colors on Android 12+
- **Custom Colors**: Defined in `topAppBarColors()` function
- **Typography**: Uses Material 3 typography scale
- **Components**: All UI components use Material 3 variants

### Performance Considerations
- **Compose Animations**: Uses `tween(300)` for smooth tab transitions
- **State Management**: Minimal recomposition with proper state scoping
- **Memory**: Uses `rememberSaveable` to prevent state loss during process death

### Debugging Tips
- Use `@Preview` annotations for Compose component development
- Enable Compose debugging in Android Studio for layout inspection
- Use `println("[DEBUG_LOG] message")` for test debugging
- Check Navigation 3 alpha documentation for latest API changes

### Known Limitations
- Navigation 3 is in alpha - some features may be unstable
- Room database integration not yet implemented
- MVI architecture pattern not fully implemented
- Limited error handling in current implementation

### Future Development Recommendations
1. Implement proper MVI architecture with ViewModels
2. Add Room database for data persistence
3. Implement proper error handling and loading states
4. Add more comprehensive test coverage
5. Consider migrating to stable Navigation when available
6. Add accessibility support and testing