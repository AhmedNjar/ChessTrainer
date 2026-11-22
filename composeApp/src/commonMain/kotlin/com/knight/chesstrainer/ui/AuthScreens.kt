package com.knight.chesstrainer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knight.chesstrainer.mvi.TrainerUiState

private val AuthLime = Color(0xFFD9FF72)
private val AuthNight = Color(0xFF070908)
private val AuthMuted = Color(0xFF8A928D)

@Composable
fun SignInScreen(
    state: TrainerUiState,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onSubmit: () -> Unit,
    onSignUp: () -> Unit,
    onBack: () -> Unit,
) = AuthForm(state, false, onEmail, onPassword, {}, {}, onSubmit, onSignUp, onBack)

@Composable
fun SignUpScreen(
    state: TrainerUiState,
    onName: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onConfirmation: (String) -> Unit,
    onSubmit: () -> Unit,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
) = AuthForm(state, true, onEmail, onPassword, onConfirmation, onName, onSubmit, onSignIn, onBack)

@Composable
private fun AuthForm(
    state: TrainerUiState,
    signUp: Boolean,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onConfirmation: (String) -> Unit,
    onName: (String) -> Unit,
    onSubmit: () -> Unit,
    onSwitchMode: () -> Unit,
    onBack: () -> Unit,
) {
    Header(
        if (signUp) "Create your account" else "Welcome back",
        if (signUp) "Sign up" else "Sign in",
        trailing = { Text("Back", Modifier.clickable(onClick = onBack).padding(8.dp), color = AuthLime, fontSize = 11.sp) },
    )
    Text(
        if (signUp) "Create an account to personalize your chess training." else "Sign in to continue your chess training.",
        color = AuthMuted,
        fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 18.dp),
    )
    if (signUp) AuthField("Display name", state.authDisplayName, onName, KeyboardType.Text)
    AuthField("Email", state.authEmail, onEmail, KeyboardType.Email)
    AuthField("Password", state.authPassword, onPassword, KeyboardType.Password, secret = true)
    if (signUp) AuthField("Confirm password", state.authPasswordConfirmation, onConfirmation, KeyboardType.Password, secret = true)

    if (state.authError.isNotBlank()) {
        Text(state.authError, color = Color(0xFFFF796F), fontSize = 10.sp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
    Button(
        onClick = onSubmit,
        enabled = !state.authBusy,
        modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AuthLime, contentColor = AuthNight, disabledContainerColor = AuthLime.copy(alpha = .5f)),
    ) {
        Text(if (state.authBusy) "Please wait…" else if (signUp) "Create account" else "Sign in", fontWeight = FontWeight.Bold)
    }
    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(if (signUp) "Already have an account? " else "New to Chess Trainer? ", color = AuthMuted, fontSize = 10.sp)
        Text(if (signUp) "Sign in" else "Create account", Modifier.clickable(onClick = onSwitchMode), color = AuthLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    secret: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = AuthLime,
            unfocusedLabelColor = AuthMuted,
            focusedBorderColor = AuthLime,
            unfocusedBorderColor = Color(0xFF303833),
            cursorColor = AuthLime,
            focusedContainerColor = Color(0xFF131715),
            unfocusedContainerColor = Color(0xFF131715),
        ),
    )
}
