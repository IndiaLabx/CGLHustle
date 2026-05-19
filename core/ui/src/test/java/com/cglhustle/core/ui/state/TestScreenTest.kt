package com.cglhustle.core.ui.state

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.cglhustle.core.common.error.NetworkError
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@Composable
fun TestScreen(state: UiState<String>) {
    when (state) {
        is UiState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.testTag("LoadingIndicator"))
        }
        is UiState.Error -> {
            Text(text = state.error.toUserFriendlyMessage(), modifier = Modifier.testTag("ErrorText"))
        }
        is UiState.Success -> {
            Text(text = state.data, modifier = Modifier.testTag("SuccessData"))
            state.transientError?.let { error ->
                Snackbar(modifier = Modifier.testTag("TransientErrorSnackbar")) {
                    Text(text = error.toUserFriendlyMessage())
                }
            }
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class TestScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsLoadingIndicator() {
        composeTestRule.setContent {
            TestScreen(state = UiState.Loading)
        }

        composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() {
        val error = NetworkError.NotFound()
        composeTestRule.setContent {
            TestScreen(state = UiState.Error(error))
        }

        composeTestRule.onNodeWithTag("ErrorText").assertIsDisplayed()
        composeTestRule.onNodeWithText("We couldn't find what you were looking for.").assertIsDisplayed()
    }

    @Test
    fun successState_showsData() {
        composeTestRule.setContent {
            TestScreen(state = UiState.Success("My Test Data"))
        }

        composeTestRule.onNodeWithTag("SuccessData").assertIsDisplayed()
        composeTestRule.onNodeWithText("My Test Data").assertIsDisplayed()
    }

    @Test
    fun successStateWithTransientError_showsDataAndSnackbar() {
        val error = NetworkError.Transient()
        composeTestRule.setContent {
            TestScreen(state = UiState.Success("My Test Data", transientError = error))
        }

        composeTestRule.onNodeWithTag("SuccessData").assertIsDisplayed()
        composeTestRule.onNodeWithText("My Test Data").assertIsDisplayed()
        composeTestRule.onNodeWithTag("TransientErrorSnackbar").assertIsDisplayed()
        composeTestRule.onNodeWithText("We are having trouble connecting. We'll keep trying in the background.").assertIsDisplayed()
    }
}
