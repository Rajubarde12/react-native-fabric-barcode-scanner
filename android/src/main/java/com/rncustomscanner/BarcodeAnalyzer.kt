package com.rncustomscanner

import android.annotation.SuppressLint
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private var onBarcodesDetected: (List<Barcode>) -> Unit,
) : ImageAnalysis.Analyzer {

    private var scanner: BarcodeScanner = createScanner(intArrayOf())

    fun updateFormats(formats: IntArray) {
        scanner.close()
        scanner = createScanner(formats)
    }

    private fun createScanner(formats: IntArray): BarcodeScanner {
        val builder = BarcodeScannerOptions.Builder()
        if (formats.isNotEmpty()) {
            builder.setBarcodeFormats(formats.first(), *formats.drop(1).toIntArray())
        }
        return BarcodeScanning.getClient(builder.build())
    }

    @SuppressLint("UnsafeOptInUsageError")
    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    onBarcodesDetected(barcodes)
                }
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    fun close() {
        scanner.close()
    }
}
