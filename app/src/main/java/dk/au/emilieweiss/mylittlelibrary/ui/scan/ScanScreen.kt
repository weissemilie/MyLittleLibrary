package dk.au.emilieweiss.mylittlelibrary.ui.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.au.emilieweiss.mylittlelibrary.ui.components.BookCover
import java.util.concurrent.Executors

private fun Context.hasCameraPermission() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

@Composable
fun ScanScreen(viewModel: ScanViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsState()

    var hasPermission by remember { mutableStateOf(context.hasCameraPermission()) }
    var hasAsked by rememberSaveable { mutableStateOf(false) }
    var showManualDialog by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        hasAsked = true
    }

    // Spørg første gang skærmen vises
    LaunchedEffect(Unit) {
        if (!hasPermission && !hasAsked) launcher.launch(Manifest.permission.CAMERA)
    }
    // Tjek igen når brugeren kommer tilbage fra appens indstillinger
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) hasPermission = context.hasCameraPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Scan bog",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.background
                )
                TextButton(onClick = { showManualDialog = true }) {
                    Text("Skriv ISBN", color = MaterialTheme.colorScheme.tertiary)
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (hasPermission) {
                    CameraPreview(viewModel = viewModel, lifecycleOwner = lifecycleOwner)
                } else {
                    PermissionRationale(
                        askedBefore = hasAsked,
                        onAskAgain = { launcher.launch(Manifest.permission.CAMERA) },
                        onOpenSettings = {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            )
                        }
                    )
                }
            }
            Text(
                "Ret kameraet mod stregkoden bag på bogen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.background,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
        }

        if (state !is ScanUiState.Scanning) {
            ResultCard(
                state = state,
                onSave = viewModel::save,
                onScanAgain = viewModel::scanAgain,
                onRetry = viewModel::retry,
                onTitleChange = viewModel::onTitleChange,
                onAuthorChange = viewModel::onAuthorChange,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showManualDialog) {
        ManualIsbnDialog(
            onDismiss = { showManualDialog = false },
            onLookup = { isbn ->
                showManualDialog = false
                viewModel.onManualIsbn(isbn)
            }
        )
    }
}

@Composable
private fun CameraPreview(viewModel: ScanViewModel, lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context).apply {
            // COMPATIBLE bruger TextureView, så de afrundede hjørner virker
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val analyzer = BarcodeAnalyzer(
            isActive = { viewModel.isScanning },
            onIsbnDetected = viewModel::onBarcodeDetected
        )
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null

        providerFuture.addListener({
            val cameraProvider = providerFuture.get()
            provider = cameraProvider

            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                        )
                        .build()
                )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor, analyzer) }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            provider?.unbindAll()
            executor.execute { analyzer.close() }
            executor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        // Scanningsramme
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(150.dp)
                .border(BorderStroke(3.dp, MaterialTheme.colorScheme.tertiary), RoundedCornerShape(24.dp))
        )
    }
}

@Composable
private fun PermissionRationale(askedBefore: Boolean, onAskAgain: () -> Unit, onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Vi har brug for dit kamera",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.background,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Kameraet bruges kun til at læse stregkoden på bøger, så de kan slås op og lægges i dit bibliotek. " +
                "Du kan også skrive ISBN i hånden.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.background,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAskAgain) { Text("Spørg igen") }
        if (askedBefore) {
            // Efter to afslag viser Android ikke dialogen mere, så giv også vej til indstillingerne
            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.padding(top = 8.dp)) {
                Text("Åbn indstillinger", color = MaterialTheme.colorScheme.background)
            }
        }
    }
}

@Composable
private fun ResultCard(
    state: ScanUiState,
    onSave: () -> Unit,
    onScanAgain: () -> Unit,
    onRetry: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state) {
                is ScanUiState.Scanning -> Unit
                is ScanUiState.LookingUp -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                    Text("Slår ISBN ${state.isbn} op…")
                }
                is ScanUiState.Found -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        BookCover(book = state.book, modifier = Modifier.width(80.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(state.book.title, style = MaterialTheme.typography.titleLarge)
                            if (state.book.author.isNotBlank()) {
                                Text(state.book.author, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    ActionButtons(
                        saveEnabled = !state.isSaving,
                        isSaving = state.isSaving,
                        onSave = onSave,
                        onScanAgain = onScanAgain
                    )
                }
                is ScanUiState.NotFound -> {
                    Text("Vi kunne ikke finde ISBN ${state.isbn}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Udfyld titel og forfatter selv, så gemmer vi bogen i dit bibliotek.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = onTitleChange,
                        label = { Text("Titel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.author,
                        onValueChange = onAuthorChange,
                        label = { Text("Forfatter") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ActionButtons(
                        saveEnabled = state.title.isNotBlank() && !state.isSaving,
                        isSaving = state.isSaving,
                        onSave = onSave,
                        onScanAgain = onScanAgain
                    )
                }
                is ScanUiState.AlreadyInLibrary -> {
                    Text("Den er allerede i dit bibliotek", style = MaterialTheme.typography.titleMedium)
                    Text("\"${state.book.title}\" ligger allerede blandt dine bøger.")
                    Button(onClick = onScanAgain, modifier = Modifier.fillMaxWidth()) { Text("Scan igen") }
                }
                is ScanUiState.Saved -> Text(
                    "✓ \"${state.title}\" er gemt i dit bibliotek",
                    style = MaterialTheme.typography.titleMedium
                )
                is ScanUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { onRetry(state.isbn) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Prøv igen")
                    }
                    TextButton(onClick = onScanAgain, modifier = Modifier.fillMaxWidth()) { Text("Scan igen") }
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(saveEnabled: Boolean, isSaving: Boolean, onSave: () -> Unit, onScanAgain: () -> Unit) {
    Button(onClick = onSave, enabled = saveEnabled, modifier = Modifier.fillMaxWidth()) {
        if (isSaving) {
            CircularProgressIndicator(
                Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Gem i bibliotek")
        }
    }
    TextButton(onClick = onScanAgain, modifier = Modifier.fillMaxWidth()) { Text("Scan igen") }
}

@Composable
private fun ManualIsbnDialog(onDismiss: () -> Unit, onLookup: (String) -> Unit) {
    var isbn by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Skriv ISBN") },
        text = {
            OutlinedTextField(
                value = isbn,
                onValueChange = { isbn = it },
                label = { Text("ISBN") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(onClick = { onLookup(isbn) }, enabled = isbn.isNotBlank()) { Text("Slå op") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } }
    )
}
