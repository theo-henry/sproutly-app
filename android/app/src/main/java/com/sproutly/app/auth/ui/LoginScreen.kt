package com.sproutly.app.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sproutly.app.auth.AuthViewModel
import com.sproutly.app.core.design.*

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val form by viewModel.form.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = BgDeep) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(LeafDeep),
                contentAlignment = Alignment.Center,
            ) { Text("🌱", style = MaterialTheme.typography.displaySmall) }

            Text("Sproutly", color = TextPrimary, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)
            Text(
                if (isSignUp) "Create your plant-based hub." else "Welcome back.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = darkFieldColors(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = darkFieldColors(),
            )

            form.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            MintPillButton(
                label = if (form.isSubmitting) "…" else if (isSignUp) "Create account" else "Sign in",
                onClick = {
                    if (isSignUp) viewModel.signUp(email, password) else viewModel.signIn(email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !form.isSubmitting && email.isNotBlank() && password.isNotBlank(),
            )
            GhostButton(
                label = "Try the demo",
                onClick = { viewModel.signInDemo() },
                modifier = Modifier.fillMaxWidth(),
            )

            TextButton(onClick = { isSignUp = !isSignUp }) {
                Text(
                    if (isSignUp) "Have an account? Sign in" else "New here? Create an account",
                    color = LeafMint,
                )
            }
        }
    }
}

@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LeafMint,
    unfocusedBorderColor = Divider,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = LeafMint,
    unfocusedLabelColor = TextMuted,
    cursorColor = LeafMint,
)
