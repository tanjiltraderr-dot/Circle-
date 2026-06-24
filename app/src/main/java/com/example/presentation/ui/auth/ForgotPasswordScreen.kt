package com.example.presentation.ui.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.repository.AuthRepository
import com.example.presentation.components.CircleAppLogo
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    EMAIL, OTP, NEW_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    authRepository: AuthRepository,
    onNavigateLogin: () -> Unit
) {
    var currentStep by remember { mutableStateOf(ForgotPasswordStep.EMAIL) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            key(currentStep.name) {
                CircleAppLogo(modifier = Modifier.size(80.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                when (currentStep) {
                    ForgotPasswordStep.EMAIL -> "Reset Password"
                    ForgotPasswordStep.OTP -> "Enter Code"
                    ForgotPasswordStep.NEW_PASSWORD -> "New Password"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                when (currentStep) {
                    ForgotPasswordStep.EMAIL -> "Enter your email address to receive a 6-digit verification code."
                    ForgotPasswordStep.OTP -> "A 6-digit code has been sent to your email."
                    ForgotPasswordStep.NEW_PASSWORD -> "Enter your new password below."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            when (currentStep) {
                ForgotPasswordStep.EMAIL -> {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email address", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.1f),
                            focusedBorderColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                        )
                    )
                }
                ForgotPasswordStep.OTP -> {
                    BasicTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it.filter { char -> char.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                repeat(6) { index ->
                                    val char = when {
                                        index >= otp.length -> ""
                                        else -> otp[index].toString()
                                    }
                                    val isFocused = otp.length == index
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .border(
                                                width = if (isFocused) 2.dp else 1.dp,
                                                color = if (isFocused) Color.Black else Color.Black.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = char,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
                ForgotPasswordStep.NEW_PASSWORD -> {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = { Text("New Password", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.1f),
                            focusedBorderColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Confirm Password", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.1f),
                            focusedBorderColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else if (successMessage != null) {
                Text(
                    text = successMessage!!,
                    color = Color(0xFF10B981),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Button(
                onClick = {
                    scope.launch {
                        errorMessage = null
                        successMessage = null
                        isLoading = true
                        
                        when (currentStep) {
                            ForgotPasswordStep.EMAIL -> {
                                if (email.isBlank()) {
                                    errorMessage = "Please enter your email"
                                } else {
                                    val result = authRepository.sendPasswordResetEmail(email)
                                    if (result.isSuccess) {
                                        currentStep = ForgotPasswordStep.OTP
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to send reset email"
                                    }
                                }
                            }
                            ForgotPasswordStep.OTP -> {
                                if (otp.isBlank()) {
                                    errorMessage = "Please enter the OTP"
                                } else {
                                    val result = authRepository.verifyPasswordResetOtp(email, otp)
                                    if (result.isSuccess) {
                                        currentStep = ForgotPasswordStep.NEW_PASSWORD
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Invalid OTP"
                                    }
                                }
                            }
                            ForgotPasswordStep.NEW_PASSWORD -> {
                                if (newPassword != confirmPassword || newPassword.isBlank()) {
                                    errorMessage = "Passwords do not match or are empty"
                                } else {
                                    val result = authRepository.updatePassword(newPassword)
                                    if (result.isSuccess) {
                                        successMessage = "Password updated successfully"
                                        kotlinx.coroutines.delay(1500)
                                        // Then log in user or back to login, the API logs them in automatically on verify otp usually, but let's go back to login just in case
                                        onNavigateLogin()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to update password"
                                    }
                                }
                            }
                        }
                        
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = when (currentStep) {
                            ForgotPasswordStep.EMAIL -> "Send Code"
                            ForgotPasswordStep.OTP -> "Verify Code"
                            ForgotPasswordStep.NEW_PASSWORD -> "Update Password"
                        },
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append("Remembered your password? ")
                    }
                    withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold)) {
                        append("Log In")
                    }
                },
                modifier = Modifier.clickable { onNavigateLogin() },
                fontSize = 14.sp
            )
        }
    }
}
