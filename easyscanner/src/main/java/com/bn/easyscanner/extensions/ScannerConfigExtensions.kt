package com.bn.easyscanner.extensions

import com.bn.easyscanner.config.ParcelableScannerConfig
import com.bn.easyscanner.config.ScannerConfig

internal fun ScannerConfig.toParcelableConfig() =
    ParcelableScannerConfig(
        formats = formats,
        stringRes = stringRes,
        drawableRes = drawableRes,
        hapticFeedback = hapticFeedback,
        showTorchToggle = showTorchToggle,
        horizontalFrameRatio = horizontalFrameRatio,
        useFrontCamera = useFrontCamera,
        showCloseButton = showCloseButton,
        showTextAction = showTextAction,
        text = text
    )