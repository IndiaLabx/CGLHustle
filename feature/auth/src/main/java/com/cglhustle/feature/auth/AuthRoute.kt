package com.cglhustle.feature.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cglhustle.feature.auth.viewmodel.AuthViewModel
import com.cglhustle.feature.auth.state.AuthUiState
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

// Custom Colors
private val DarkCharcoal = Color(0xFF121212)
private val IndigoGlow = Color(0xFF4F46E5)
private val CyanBloom = Color(0xFF06B6D4)
private val TranslucentSurface = Color(0x1AFFFFFF)
private val MutedGray = Color(0xFFA1A1AA)
private val SoftRed = Color(0xFFEF4444)
private val SuccessGreen = Color(0xFF10B981)

@Composable
fun AuthRoute(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Assuming session is verified globally, but if we need a direct success trigger:
    // (In a real app, MainActivity/NavGraph observing sessionStatus handles this).

    AuthScreen(
        uiState = uiState,
        onTabSwitched = viewModel::onTabSwitched,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onNameChanged = viewModel::onNameChanged,
        onAgeCheckedChanged = viewModel::onAgeCheckedChanged,
        onPrivacyCheckedChanged = viewModel::onPrivacyCheckedChanged,
        onSubmit = viewModel::submitEmailForm,
        onGoogleSignIn = viewModel::signInWithGoogle,
        onGuestSignIn = viewModel::signInWithGuest,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onTabSwitched: (Boolean) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onAgeCheckedChanged: (Boolean) -> Unit,
    onPrivacyCheckedChanged: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignIn: (String) -> Unit,
    onGuestSignIn: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = DarkCharcoal
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCharcoal)
        ) {
            AnimatedBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                AuthHeader()

                Spacer(modifier = Modifier.height(32.dp))

                AuthCard(
                    uiState = uiState,
                    onTabSwitched = onTabSwitched,
                    onEmailChanged = onEmailChanged,
                    onPasswordChanged = onPasswordChanged,
                    onConfirmPasswordChanged = onConfirmPasswordChanged,
                    onNameChanged = onNameChanged,
                    onAgeCheckedChanged = onAgeCheckedChanged,
                    onPrivacyCheckedChanged = onPrivacyCheckedChanged,
                    onSubmit = onSubmit,
                    onGoogleSignIn = onGoogleSignIn,
                    onGuestSignIn = onGuestSignIn
                )

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val driftY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "driftY"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(IndigoGlow.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.2f + driftY),
                radius = size.width * 0.6f
            ),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CyanBloom.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.8f - driftY),
                radius = size.width * 0.7f
            ),
        )
    }
}

@Composable
fun AuthHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(IndigoGlow.copy(alpha = 0.2f), CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Psychology,
                contentDescription = "CGL Hustle Logo",
                tint = IndigoGlow,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CGL Hustle",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your intelligent study ecosystem",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedGray
        )
    }
}

@Composable
fun AuthCard(
    uiState: AuthUiState,
    onTabSwitched: (Boolean) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onAgeCheckedChanged: (Boolean) -> Unit,
    onPrivacyCheckedChanged: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignIn: (String) -> Unit,
    onGuestSignIn: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = IndigoGlow
            ),
        shape = RoundedCornerShape(32.dp),
        color = TranslucentSurface
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthTabs(
                isSignUp = uiState.isSignUpMode,
                onTabSelected = onTabSwitched
            )

            Spacer(modifier = Modifier.height(24.dp))

            GoogleAuthButton(
                isLoading = uiState.isGoogleLoading,
                onClick = onGoogleSignIn
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MutedGray.copy(alpha = 0.3f))
                Text(
                    text = "Or continue with email",
                    color = MutedGray,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelSmall
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MutedGray.copy(alpha = 0.3f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            EmailAuthForm(
                uiState = uiState,
                onEmailChanged = onEmailChanged,
                onPasswordChanged = onPasswordChanged,
                onConfirmPasswordChanged = onConfirmPasswordChanged,
                onNameChanged = onNameChanged,
                onAgeCheckedChanged = onAgeCheckedChanged,
                onPrivacyCheckedChanged = onPrivacyCheckedChanged,
                onSubmit = onSubmit
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!uiState.isSignUpMode) {
                Text(
                    text = "Continue as Guest (Click Here)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MutedGray,
                    modifier = Modifier.clickable { onGuestSignIn() }
                )

                if (uiState.isGuestLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = IndigoGlow)
                }
            }
        }
    }
}

@Composable
fun AuthTabs(isSignUp: Boolean, onTabSelected: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TabItem(
            text = "Sign In",
            isSelected = !isSignUp,
            onClick = { onTabSelected(false) }
        )
        Spacer(modifier = Modifier.width(32.dp))
        TabItem(
            text = "Sign Up",
            isSelected = isSignUp,
            onClick = { onTabSelected(true) }
        )
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (isSelected) IndigoGlow else MutedGray,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(if (isSelected) IndigoGlow else Color.Transparent)
        )
    }
}

@Composable
fun GoogleAuthButton(isLoading: Boolean, onClick: (String) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            if (!isLoading) {
                coroutineScope.launch {
                    val credentialManager = CredentialManager.create(context)
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId("PLACEHOLDER_GOOGLE_WEB_CLIENT_ID") // To be replaced
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    try {
                        val result = credentialManager.getCredential(context = context, request = request)
                        val credential = result.credential
                        if (credential is GoogleIdTokenCredential) {
                            onClick(credential.idToken)
                        }
                    } catch (e: GetCredentialException) {
                        // Handle failure quietly for now
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = DarkCharcoal
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = IndigoGlow)
        } else {
            // Replaced generic text with 'G' to simulate Google without drawable
            Text("G", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Continue with Google", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EmailAuthForm(
    uiState: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onAgeCheckedChanged: (Boolean) -> Unit,
    onPrivacyCheckedChanged: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AnimatedContent(targetState = uiState.isSignUpMode, label = "form_anim") { isSignUp ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (isSignUp) {
                CustomTextField(
                    value = uiState.nameInput,
                    onValueChange = onNameChanged,
                    label = "Full Name",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            }

            CustomTextField(
                value = uiState.emailInput,
                onValueChange = onEmailChanged,
                label = "Email Address",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            CustomTextField(
                value = uiState.passwordInput,
                onValueChange = onPasswordChanged,
                label = "Password",
                keyboardType = KeyboardType.Password,
                imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = it },
                onImeAction = { if (!isSignUp) { focusManager.clearFocus(); onSubmit() } }
            )

            if (isSignUp) {
                CustomTextField(
                    value = uiState.confirmPasswordInput,
                    onValueChange = onConfirmPasswordChanged,
                    label = "Confirm Password",
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = it },
                    onImeAction = { focusManager.clearFocus() }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.isAgeChecked, onCheckedChange = onAgeCheckedChanged)
                    Text("I am 18 years of age or older", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.isPrivacyChecked, onCheckedChange = onPrivacyCheckedChanged)
                    Text(
                        "I agree to the Privacy Policy",
                        color = IndigoGlow,
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cglhustle.app/privacy-policy"))
                            context.startActivity(intent)
                        }
                    )
                }
            } else {
                 Text(
                    "Forgot Password?",
                    color = MutedGray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = SoftRed,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            uiState.successMessage?.let { msg ->
                Text(
                    text = msg,
                    color = SuccessGreen,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoGlow),
                enabled = !uiState.isEmailLoading
            ) {
                if (uiState.isEmailLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = if (isSignUp) "Sign Up" else "Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (Boolean) -> Unit = {},
    onImeAction: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MutedGray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onAny = { onImeAction() }),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        tint = MutedGray
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = IndigoGlow,
            unfocusedBorderColor = TranslucentSurface,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = IndigoGlow,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}
