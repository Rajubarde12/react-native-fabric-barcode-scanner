package com.rncustomscanner

import com.google.mlkit.vision.barcode.common.Barcode

object BarcodeFormatMapper {
    val supportedFormats: List<String> = listOf(
        "qr",
        "ean13",
        "ean8",
        "upce",
        "code128",
        "code39",
        "code93",
        "pdf417",
        "aztec",
        "datamatrix",
        "itf14",
        "interleaved2of5",
    )

    fun toMlKitFormat(name: String): Int? {
        return when (name) {
            "qr" -> Barcode.FORMAT_QR_CODE
            "ean13" -> Barcode.FORMAT_EAN_13
            "ean8" -> Barcode.FORMAT_EAN_8
            "upce" -> Barcode.FORMAT_UPC_E
            "code128" -> Barcode.FORMAT_CODE_128
            "code39" -> Barcode.FORMAT_CODE_39
            "code93" -> Barcode.FORMAT_CODE_93
            "pdf417" -> Barcode.FORMAT_PDF417
            "aztec" -> Barcode.FORMAT_AZTEC
            "datamatrix" -> Barcode.FORMAT_DATA_MATRIX
            "itf14", "interleaved2of5" -> Barcode.FORMAT_ITF
            else -> null
        }
    }

    fun fromMlKitFormat(@Barcode.BarcodeFormat format: Int): String {
        return when (format) {
            Barcode.FORMAT_QR_CODE -> "qr"
            Barcode.FORMAT_EAN_13 -> "ean13"
            Barcode.FORMAT_EAN_8 -> "ean8"
            Barcode.FORMAT_UPC_E -> "upce"
            Barcode.FORMAT_UPC_A -> "upce"
            Barcode.FORMAT_CODE_128 -> "code128"
            Barcode.FORMAT_CODE_39 -> "code39"
            Barcode.FORMAT_CODE_93 -> "code93"
            Barcode.FORMAT_PDF417 -> "pdf417"
            Barcode.FORMAT_AZTEC -> "aztec"
            Barcode.FORMAT_DATA_MATRIX -> "datamatrix"
            Barcode.FORMAT_ITF -> "itf14"
            else -> "unknown"
        }
    }

    fun toMlKitFormats(names: List<String>): IntArray {
        return names.mapNotNull { toMlKitFormat(it) }.distinct().toIntArray()
    }
}
