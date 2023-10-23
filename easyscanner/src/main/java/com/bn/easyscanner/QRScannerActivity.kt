package com.bn.easyscanner

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bn.easyscanner.config.ParcelableScannerConfig
import com.bn.easyscanner.databinding.QuickieScannerActivityBinding
import com.bn.easyscanner.extensions.toParcelableContentType
import com.bn.easyscanner.utils.MlKitErrorHandler
import com.google.mlkit.vision.barcode.common.Barcode

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class QRScannerActivity : AppCompatActivity() {

  private lateinit var binding: QuickieScannerActivityBinding
  private lateinit var analysisExecutor: ExecutorService
  private var barcodeFormats = intArrayOf(Barcode.FORMAT_QR_CODE)
  private var hapticFeedback = true
  private var showTorchToggle = false
  private var showCloseButton = false
  private var useFrontCamera = false
  private var showTextAction = false
  private var text = ""
  internal var errorDialog: Dialog? = null
    set(value) {
      field = value
      value?.show()
      value?.setOnKeyListener { dialog, keyCode, _ ->
        if (keyCode == KeyEvent.KEYCODE_BACK) {
          finish()
          dialog.dismiss()
          true
        } else {
          false
        }
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val appThemeLayoutInflater = applicationInfo.theme.let { appThemeRes ->
      if (appThemeRes != 0) layoutInflater.cloneInContext(ContextThemeWrapper(this, appThemeRes)) else layoutInflater
    }
    binding = QuickieScannerActivityBinding.inflate(appThemeLayoutInflater)
    setContentView(binding.root)

    setupEdgeToEdgeUI()
    applyScannerConfig()

    analysisExecutor = Executors.newSingleThreadExecutor()

    requestCameraPermissionIfMissing { granted ->
      if (granted) {
        startCamera()
      } else {
        setResult(RESULT_MISSING_PERMISSION, null)
        finish()
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    analysisExecutor.shutdown()
  }

  private fun startCamera() {
    val cameraProviderFuture = try {
      ProcessCameraProvider.getInstance(this)
    } catch (e: Exception) {
      onFailure(e)
      return
    }

    cameraProviderFuture.addListener({
      val cameraProvider = try {
        cameraProviderFuture.get()
      } catch (e: Exception) {
        onFailure(e)
        return@addListener
      }

      val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
      val imageAnalysis = ImageAnalysis.Builder()
        .setTargetResolution(Size(1280, 720))
        .build()
        .also {
          it.setAnalyzer(
            analysisExecutor,
            QRCodeAnalyzer(
              barcodeFormats = barcodeFormats,
              onSuccess = { barcode ->
                it.clearAnalyzer()
                onSuccess(barcode)
              },
              onFailure = { exception -> onFailure(exception) },
              onPassCompleted = { failureOccurred -> onPassCompleted(failureOccurred) }
            )
          )
        }

      cameraProvider.unbindAll()

      val cameraSelector =
        if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

      try {
        val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        binding.overlayView.visibility = View.VISIBLE
        binding.overlayView.setCloseVisibilityAndOnClick(showCloseButton) { finish() }
        if (showTextAction) {
          binding.layoutTextAction.visibility = View.VISIBLE
          binding.tvTextAction.text = text
          binding.tvTextAction.setOnClickListener {
            setResult(
              Activity.RESULT_OK,
              Intent().apply {
                putExtra(TEXT_ACTION_CLICKED, true)
              }
            )
            finish()
          }
        }
        if (showTorchToggle && camera.cameraInfo.hasFlashUnit()) {
          binding.overlayView.setTorchVisibilityAndOnClick(true) { camera.cameraControl.enableTorch(it) }
          camera.cameraInfo.torchState.observe(this) { binding.overlayView.setTorchState(it == TorchState.ON) }
        } else {
          binding.overlayView.setTorchVisibilityAndOnClick(false)
        }
      } catch (e: Exception) {
        binding.overlayView.visibility = View.INVISIBLE
        onFailure(e)
      }
    }, ContextCompat.getMainExecutor(this))
  }

  private fun onSuccess(result: Barcode) {
    binding.overlayView.isHighlighted = true
    if (hapticFeedback) {
      @Suppress("DEPRECATION")
      val flags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
      binding.overlayView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, flags)
    }
    setResult(
      Activity.RESULT_OK,
      Intent().apply {
        putExtra(EXTRA_RESULT_BYTES, result.rawBytes)
        putExtra(EXTRA_RESULT_VALUE, result.rawValue)
        putExtra(EXTRA_RESULT_TYPE, result.valueType)
        putExtra(EXTRA_RESULT_PARCELABLE, result.toParcelableContentType())
      }
    )
    finish()
  }

  private fun onFailure(exception: Exception) {
    setResult(RESULT_ERROR, Intent().putExtra(EXTRA_RESULT_EXCEPTION, exception))
    if (!MlKitErrorHandler.isResolvableError(this, exception)) finish()
  }

  private fun onPassCompleted(failureOccurred: Boolean) {
    if (!isFinishing) binding.overlayView.isLoading = failureOccurred
  }

  private fun setupEdgeToEdgeUI() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(binding.overlayView) { v, insets ->
      insets.getInsets(WindowInsetsCompat.Type.systemBars()).let { v.setPadding(it.left, it.top, it.right, it.bottom) }
      WindowInsetsCompat.CONSUMED
    }
  }

  private fun applyScannerConfig() {
    intent?.let { IntentCompat.getParcelableExtra(it, EXTRA_CONFIG, ParcelableScannerConfig::class.java) }?.let {
      barcodeFormats = it.formats
      binding.overlayView.setCustomText(it.stringRes)
      binding.overlayView.setCustomIcon(it.drawableRes)
      binding.overlayView.setHorizontalFrameRatio(it.horizontalFrameRatio)
      hapticFeedback = it.hapticFeedback
      showTorchToggle = it.showTorchToggle
      useFrontCamera = it.useFrontCamera
      showCloseButton = it.showCloseButton
      showTextAction = it.showTextAction
      text = it.text
    }
  }

  private fun requestCameraPermissionIfMissing(onResult: ((Boolean) -> Unit)) {
    if (ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED) {
      onResult(true)
    } else {
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { onResult(it) }.launch(CAMERA)
    }
  }

  companion object {
    const val EXTRA_CONFIG = "easy-scanner-config"
    const val EXTRA_RESULT_BYTES = "easy-scanner-bytes"
    const val EXTRA_RESULT_VALUE = "easy-scanner-value"
    const val EXTRA_RESULT_TYPE = "easy-scanner-type"
    const val EXTRA_RESULT_PARCELABLE = "easy-scanner-parcelable"
    const val EXTRA_RESULT_EXCEPTION = "easy-scanner-exception"
    const val TEXT_ACTION_CLICKED = "text_action_clicked"
    const val RESULT_MISSING_PERMISSION = RESULT_FIRST_USER + 1
    const val RESULT_ERROR = RESULT_FIRST_USER + 2
  }
}