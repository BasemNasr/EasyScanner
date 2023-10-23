package com.bn.easyscanner.utils

import com.bn.easyscanner.QRScannerActivity


internal object MlKitErrorHandler {

  @Suppress("UNUSED_PARAMETER", "FunctionOnlyReturningConstant")
  fun isResolvableError(activity: QRScannerActivity, exception: Exception) = false // always false when bundled
}