package com.hsjeong.supporttools.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * Ripple click 시 효과 제거를 위한 확장 함수
 */
@Composable
fun Modifier.noRippleClickable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit
): Modifier = this.then(
    Modifier.clickable(
        indication = null, // ripple 제거
        interactionSource = interactionSource,
        onClick = onClick
    )
)

/**
 * Text의 fontSize를 dp로 지정하기 위해 dp를 sp로 변환하는 함수
 */
@Composable
fun fontSizeDpToSp(dp: Dp): TextUnit {
    val density = LocalDensity.current
    return with(density) {
        dp.toSp()
    }
}