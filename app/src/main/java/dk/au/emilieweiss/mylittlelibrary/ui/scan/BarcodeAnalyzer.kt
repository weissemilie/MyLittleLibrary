package dk.au.emilieweiss.mylittlelibrary.ui.scan

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Finder EAN-13 stregkoder, der starter med 978 eller 979 (Bookland, altså et ISBN).
 * Brug STRATEGY_KEEP_ONLY_LATEST på ImageAnalysis, så billeder ikke hober sig op.
 *
 * [isActive] gør at billeder bare droppes, mens scanningen er sat på pause.
 */
class BarcodeAnalyzer(
    private val isActive: () -> Boolean,
    private val onIsbnDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !isActive()) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val isbn = barcodes
                    .mapNotNull { it.rawValue }
                    .firstOrNull { it.length == 13 && (it.startsWith("978") || it.startsWith("979")) }
                if (isbn != null) onIsbnDetected(isbn)
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    fun close() {
        scanner.close()
    }
}
