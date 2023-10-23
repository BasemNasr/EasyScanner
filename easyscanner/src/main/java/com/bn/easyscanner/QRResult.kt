package io.github.g00fy2.quickie

import com.bn.easyscanner.content.QRContent


public sealed class QRResult {

  /**
   * MLKit successfully detected a QR code.
   *
   * @property content the wrapped MLKit response.
   */
  public data class QRSuccess internal constructor(val content: QRContent) : QRResult()

  /**
   * Activity got cancelled by the user.
   */
  public object QRUserCanceled : QRResult()

  /**
   * Camera permission was not granted.
   */
  public object QRMissingPermission : QRResult()

  /**
   * Error while setting up CameraX or while MLKit analysis.
   *
   * @property exception the cause why the Activity was finished.
   */
  public data class QRError internal constructor(val exception: Exception) : QRResult()
}