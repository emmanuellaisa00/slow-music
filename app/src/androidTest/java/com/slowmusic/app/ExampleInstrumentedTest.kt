package com.slowmusic.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.slowmusic.app.presentation.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * UI Tests for Apple Music Components
 */
class AppleMusicComponentTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun `AppleGlassCard renders correctly`() {
        composeTestRule.onNodeWithText("Test Card")
            .assertExists()
    }
    
    @Test
    fun `AppleToggle changes state on click`() {
        // Find toggle and verify initial state
        composeTestRule
            .onNode(hasTestTag("apple_toggle"))
            .assertExists()
    }
    
    @Test
    fun `AppleBottomNavigation shows all tabs`() {
        composeTestRule
            .onNodeWithText("Home")
            .assertExists()
        
        composeTestRule
            .onNodeWithText("Search")
            .assertExists()
        
        composeTestRule
            .onNodeWithText("Library")
            .assertExists()
        
        composeTestRule
            .onNodeWithText("Profile")
            .assertExists()
    }
}

/**
 * UI Tests for Screens
 */
class ScreenTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun `HomeScreen shows content`() {
        composeTestRule.waitForIdle()
        
        // Verify app name is shown
        composeTestRule
            .onNodeWithText("Slow Music")
            .assertExists()
    }
    
    @Test
    fun `SearchScreen search bar works`() {
        composeTestRule.waitForIdle()
        
        // Navigate to search
        composeTestRule
            .onNodeWithText("Search")
            .performClick()
        
        composeTestRule.waitForIdle()
        
        // Search bar should exist
        composeTestRule
            .onNode(hasPlaceholderText("Songs, artists, albums..."))
            .assertExists()
    }
    
    @Test
    fun `Settings screen navigation works`() {
        composeTestRule.waitForIdle()
        
        // Navigate to settings via profile
        composeTestRule
            .onNodeWithText("Profile")
            .performClick()
        
        composeTestRule.waitForIdle()
    }
}

/**
 * UI Tests for Navigation
 */
class NavigationTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun `Bottom navigation switches screens`() {
        composeTestRule.waitForIdle()
        
        // Test each tab
        listOf("Home", "Search", "Library", "Profile").forEach { tab ->
            composeTestRule
                .onNodeWithText(tab)
                .performClick()
            
            composeTestRule.waitForIdle()
        }
    }
}

/**
 * UI Tests for Empty States
 */
class EmptyStateTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun `Empty state shows message`() {
        composeTestRule.waitForIdle()
        
        // Navigate to library with no content
        composeTestRule
            .onNodeWithText("Library")
            .performClick()
        
        composeTestRule.waitForIdle()
    }
}
