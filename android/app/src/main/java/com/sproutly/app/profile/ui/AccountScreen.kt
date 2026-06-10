package com.sproutly.app.profile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sproutly.app.core.design.*
import com.sproutly.app.core.result.UiState
import com.sproutly.app.profile.ProfileViewModel
import com.sproutly.app.profile.model.DietPreference
import com.sproutly.app.profile.model.DietTags
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val saved by viewModel.savedMessage.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    viewModel.uploadAvatar(stream.readBytes())
                }
            }
        }
    }

    LaunchedEffect(saved) {
        saved?.let {
            snackbar.showSnackbar(it)
            viewModel.clearSavedMessage()
        }
    }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDeep,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when (val s = state) {
            UiState.Loading -> CenteredLoader(padding)
            is UiState.Error -> CenteredMessage(padding, "Error: ${s.message}")
            UiState.Empty -> CenteredMessage(padding, "No profile")
            is UiState.Success -> {
                val profile = s.data
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SproutlyCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val avatarUrl = viewModel.avatarUrl(profile.avatarPath)
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(LeafDeep),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (avatarUrl != null) {
                                    AsyncImage(model = avatarUrl, contentDescription = "Avatar", modifier = Modifier.fillMaxSize())
                                } else {
                                    Text("🌱", style = MaterialTheme.typography.headlineMedium)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(profile.displayName ?: "Plant explorer", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                Text(profile.email ?: "", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                            }
                            GhostButton(label = "Change", onClick = {
                                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            })
                        }
                    }

                    SproutlyCard {
                        SectionLabel("Display")
                        Spacer(Modifier.height(8.dp))
                        Field("Display name", profile.displayName ?: "") { v ->
                            viewModel.update { it.copy(displayName = v) }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f)) {
                                Field("City", profile.city ?: "") { v -> viewModel.update { it.copy(city = v) } }
                            }
                            Box(Modifier.weight(1f)) {
                                Field("Country", profile.country ?: "") { v -> viewModel.update { it.copy(country = v) } }
                            }
                        }
                    }

                    SproutlyCard {
                        SectionLabel("Diet preference")
                        Spacer(Modifier.height(10.dp))
                        FlowRowChips(
                            options = DietPreference.entries.map { it.value to it.label },
                            selectedValues = listOfNotNull(profile.dietPreference),
                            onToggle = { value ->
                                viewModel.update { it.copy(dietPreference = value) }
                            }
                        )
                    }

                    SproutlyCard {
                        SectionLabel("Tags")
                        Spacer(Modifier.height(10.dp))
                        FlowRowChips(
                            options = DietTags.ALL.map { it to it },
                            selectedValues = profile.dietTags,
                            onToggle = { value ->
                                viewModel.update {
                                    val next = if (value in it.dietTags) it.dietTags - value else it.dietTags + value
                                    it.copy(dietTags = next)
                                }
                            }
                        )
                    }

                    MintPillButton("Save profile", onClick = viewModel::save, modifier = Modifier.fillMaxWidth())
                    GhostButton("Sign out", onClick = onSignOut, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LeafMint,
            unfocusedBorderColor = Divider,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = LeafMint,
            unfocusedLabelColor = TextMuted,
            cursorColor = LeafMint,
        )
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowChips(
    options: List<Pair<String, String>>,
    selectedValues: List<String>,
    onToggle: (String) -> Unit,
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            Chip(label = label, selected = value in selectedValues, onClick = { onToggle(value) })
        }
    }
}

@Composable
private fun CenteredLoader(padding: PaddingValues) {
    Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = LeafMint)
    }
}

@Composable
private fun CenteredMessage(padding: PaddingValues, message: String) {
    Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = TextMuted)
    }
}
