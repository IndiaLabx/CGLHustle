package com.cglhustle.core.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.common.error.toUserFriendlyMessage
import com.cglhustle.core.ui.state.UiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], instrumentedPackages = ["androidx.loader.content"])
class StatefulScreenWrapperTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoadingState() {
        composeTestRule.setContent {
            StatefulScreenWrapper<String>(uiState = UiState.Loading) {
                Text(text = "Should Not See Me")
            }
        }

        // Assert content is not shown in loading
        composeTestRule.onNodeWithText("Should Not See Me").assertDoesNotExist()
    }

    @Test
    fun testErrorStateWithRetry() {
        var retryClicked = false
        val transientError = NetworkError.Transient()

        composeTestRule.setContent {
            StatefulScreenWrapper<String>(
                uiState = UiState.Error(transientError),
                onRetry = { retryClicked = true }
            ) {
                Text(text = "Should Not See Me")
            }
        }

        // Assert data content is not shown
        composeTestRule.onNodeWithText("Should Not See Me").assertDoesNotExist()

        // Assert error message is displayed
        composeTestRule.onNodeWithText(transientError.toUserFriendlyMessage()).assertExists()

        // Click retry
        composeTestRule.onNodeWithText("Retry").performClick()

        assertTrue("Retry action should have been triggered", retryClicked)
    }

    @Test
    fun testSuccessState() {
        composeTestRule.setContent {
            StatefulScreenWrapper(uiState = UiState.Success("Hello User")) { data ->
                Text(text = data)
            }
        }

        // Assert content is shown
        composeTestRule.onNodeWithText("Hello User").assertExists()
    }

    @Test
    fun testSuccessStateWithTransientError() {
        val transientError = NetworkError.ServerOutage()

        composeTestRule.setContent {
            StatefulScreenWrapper(
                uiState = UiState.Success(
                    data = "Vital Content",
                    transientError = transientError
                )
            ) { data ->
                Text(text = data)
            }
        }

        // Assert BOTH content and error are shown (Non-blocking mutation failure)
        composeTestRule.onNodeWithText("Vital Content").assertExists()
        composeTestRule.onNodeWithText(transientError.toUserFriendlyMessage()).assertExists()
    }
}
