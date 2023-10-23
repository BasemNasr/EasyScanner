package com.bn.easyscanner

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.bn.easyscanner.QRScannerActivity.Companion.EXTRA_CONFIG
import com.bn.easyscanner.QRScannerActivity.Companion.RESULT_ERROR
import com.bn.easyscanner.QRScannerActivity.Companion.RESULT_MISSING_PERMISSION
import com.bn.easyscanner.config.ScannerConfig
import com.bn.easyscanner.extensions.getRootException
import com.bn.easyscanner.extensions.toParcelableConfig
import com.bn.easyscanner.extensions.toQuickieContentType
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.QRResult.QRError
import io.github.g00fy2.quickie.QRResult.QRMissingPermission
import io.github.g00fy2.quickie.QRResult.QRSuccess
import io.github.g00fy2.quickie.QRResult.QRUserCanceled

public class ScanCustomCode : ActivityResultContract<ScannerConfig, QRResult>() {

    override fun createIntent(context: Context, input: ScannerConfig): Intent {
        return Intent(context, QRScannerActivity::class.java).apply {
            putExtra(EXTRA_CONFIG, input.toParcelableConfig())
        }
    }

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