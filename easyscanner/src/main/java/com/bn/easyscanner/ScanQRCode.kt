package com.bn.easyscanner

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import io.github.g00fy2.quickie.QRResult.QRError
import io.github.g00fy2.quickie.QRResult.QRMissingPermission
import io.github.g00fy2.quickie.QRResult.QRSuccess
import io.github.g00fy2.quickie.QRResult.QRUserCanceled
import com.bn.easyscanner.QRScannerActivity.Companion.RESULT_ERROR
import com.bn.easyscanner.QRScannerActivity.Companion.RESULT_MISSING_PERMISSION
import com.bn.easyscanner.extensions.getRootException
import com.bn.easyscanner.extensions.toQuickieContentType
import io.github.g00fy2.quickie.QRResult

public class ScanQRCode : ActivityResultContract<Nothing?, QRResult>() {

  override fun createIntent(context: Context, input: Nothing?): Intent =
    Intent(context, QRScannerActivity::class.java)

  override fun parseResult(resultCode: Int, intent: Intent?): QRResult {
    return when (resultCode) {
      RESULT_OK -> QRSuccess(intent.toQuickieContentType())
      RESULT_CANCELED -> QRUserCanceled
      RESULT_MISSING_PERMISSION -> QRMissingPermission
      RESULT_ERROR -> QRError(intent.getRootException())
      else -> QRError(IllegalStateException("Unknown activity result code $resultCode"))
    }
  }
}