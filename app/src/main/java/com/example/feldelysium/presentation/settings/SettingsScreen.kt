package com.example.feldelysium.presentation.settings

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feldelysium.FeldApp
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.navigation.FeldScreenHeader
import com.example.feldelysium.designsystem.components.container.FeldToastHost
import com.example.feldelysium.designsystem.components.container.rememberFeldToastState
import com.example.feldelysium.domain.model.settings.AppSettings
import com.example.feldelysium.presentation.settings.dialogs.SettingsDialogHost
import com.example.feldelysium.presentation.settings.sections.AppearanceSection
import com.example.feldelysium.presentation.settings.sections.ComingSoonSection
import com.example.feldelysium.presentation.settings.sections.LanguageSection
import com.example.feldelysium.presentation.settings.sections.PlaybackSection
import com.example.feldelysium.presentation.settings.sections.RecordingSection
import com.example.feldelysium.presentation.settings.sections.SpeakersSection
import com.example.feldelysium.presentation.settings.speakerpack.SpeakerPackViewModel
import com.example.feldelysium.presentation.settings.speakerpack.text
import com.example.feldelysium.designsystem.theme.fontDir


@SuppressLint("ContextCastToActivity")
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current as ComponentActivity
    val container = (activity.application as FeldApp).container

    val vm: SettingsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = SettingsViewModel.factory(container, activity.fontDir()),
    )
    val packVm: SpeakerPackViewModel = viewModel(factory = SpeakerPackViewModel.factory(container))

    val settings by vm.state.collectAsStateWithLifecycle()
    val toast = rememberFeldToastState()

    var dialog by rememberSaveable { mutableStateOf<SettingsDialog?>(null) }
    var leaving by remember { mutableStateOf(false) }

    BackHandler(enabled = !leaving) { leaving = true }

    LaunchedEffect(leaving) {
        if (leaving) {
            withFrameNanos { }
            withFrameNanos { }
            onNavigateBack()
        }
    }

    LaunchedEffect(vm) {
        vm.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowMessage -> toast.show(activity.getString(effect.messageRes))
            }
        }
    }
    LaunchedEffect(packVm) {
        packVm.messages.collect { toast.show(it.text(activity)) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri -> uri?.let(packVm::export) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(packVm::prepareImport) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SettingsContent(
            settings = settings,
            freezeVideo = leaving,
            onEvent = vm::dispatch,
            onOpenDialog = { dialog = it },
            onExportPack = { exportLauncher.launch("speakers.feldpack") },
            onImportPack = { importLauncher.launch(arrayOf("*/*")) },
            onBack = { leaving = true },
        )

        FeldToastHost(
            toast,
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }

    SettingsDialogHost(
        dialog = dialog,
        settings = settings,
        packVm = packVm,
        onEvent = vm::dispatch,
        onImportFont = vm::importFont,
        onDismiss = { dialog = null },
    )
}


@Composable
internal fun SettingsContent(
    settings: AppSettings,
    freezeVideo: Boolean,
    onEvent: (SettingsEvent) -> Unit,
    onOpenDialog: (SettingsDialog) -> Unit,
    onExportPack: () -> Unit,
    onImportPack: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().statusBarsPadding()) {
        FeldScreenHeader(
            title = stringResource(R.string.settings_title),
            eyebrow = stringResource(R.string.settings_eyebrow),
            subtitle = stringResource(R.string.settings_subtitle),
            onNav = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .testTag("settings_scroll"),
        ) {
            PlaybackSection(settings, onEvent)
            AppearanceSection(settings, freezeVideo, onEvent, onOpenDialog)
            RecordingSection(settings, onEvent)
            SpeakersSection(settings, onEvent, onOpenDialog, onExportPack, onImportPack)
            LanguageSection(onOpenDialog)
            ComingSoonSection()
        }
    }
}
