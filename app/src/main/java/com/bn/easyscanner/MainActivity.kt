package com.bn.easyscanner

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bn.easyscanner.config.BarcodeFormat
import com.bn.easyscanner.config.ScannerConfig
import com.bn.easyscanner.content.QRContent
import com.bn.easyscanner.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import io.github.g00fy2.quickie.QRResult

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private var selectedBarcodeFormat = BarcodeFormat.FORMAT_ALL_FORMATS

  private val scanQrCode = registerForActivityResult(ScanQRCode(), ::showSnackbar)
  private val scanCustomCode = registerForActivityResult(ScanCustomCode(), ::showSnackbar)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setBarcodeFormatDropdown()

    binding.qrScannerButton.setOnClickListener {
      scanCustomCode.launch(
        ScannerConfig.build {
          setShowTextAction(true,"Enter Code")
        }
      )
    }

    binding.customScannerButton.setOnClickListener {
      scanCustomCode.launch(
        ScannerConfig.build {
          setBarcodeFormats(listOf(selectedBarcodeFormat)) // set interested barcode formats
          setOverlayStringRes(R.string.scan_barcode) // string resource used for the scanner overlay
          setOverlayDrawableRes(R.drawable.ic_scan_barcode) // drawable resource used for the scanner overlay
          setHapticSuccessFeedback(false) // enable (default) or disable haptic feedback when a barcode was detected
          setShowTorchToggle(true) // show or hide (default) torch/flashlight toggle button
          setShowCloseButton(true) // show or hide (default) close button
          setHorizontalFrameRatio(2.2f) // set the horizontal overlay ratio (default is 1 / square frame)
          setUseFrontCamera(false) // use the front camera
        }
      )
    }

    if (intent.extras?.getBoolean(OPEN_SCANNER) == true) scanQrCode.launch(null)
  }

  private fun showSnackbar(result: QRResult) {
    val text = when (result) {
      is QRResult.QRSuccess -> {
        result.content.rawValue
        // decoding with default UTF-8 charset when rawValue is null will not result in meaningful output, demo purpose
          ?: result.content.rawBytes?.let { String(it) }.orEmpty()
      }
      QRResult.QRUserCanceled -> "User canceled"
      QRResult.QRMissingPermission -> "Missing permission"
      is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
    }

    Snackbar.make(binding.root, text, Snackbar.LENGTH_INDEFINITE).apply {
      view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.run {
        maxLines = 5
        setTextIsSelectable(true)
      }
      if (result is QRResult.QRSuccess) {
        val content = result.content
        if (content is QRContent.Url) {
          setAction(R.string.open_action) { openUrl(content.url) }
          return@apply
        }
      }
      setAction(R.string.ok_action) { }
    }.show()
  }

  private fun openUrl(url: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (ignored: ActivityNotFoundException) {
      // no Activity found to run the given Intent
    }
  }

  private fun setBarcodeFormatDropdown() {
    ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, BarcodeFormat.entries.map { it.name }).let {
      binding.barcodeFormatsAutoCompleteTextView.setAdapter(it)
      binding.barcodeFormatsAutoCompleteTextView.setText(it.getItem(it.getPosition(selectedBarcodeFormat.name)), false)
    }
    binding.barcodeFormatsAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
      selectedBarcodeFormat = BarcodeFormat.entries[position]
    }
  }

  companion object {
    const val OPEN_SCANNER = "open_scanner"
  }
}