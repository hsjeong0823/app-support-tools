package com.hsjeong.supporttools.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hsjeong.supporttools.R

// 버튼 State 색상 정보
data class CtaButtonColors(
    val normal: Color,
    val pressed: Color,
    val disabled: Color,
)

// 버튼 타입 정의
enum class ButtonType {
    // 공통
    FILLGREEN,              // 초록 버튼
    FILLWHITE_BORDERGREEN,  // 흰색 버튼, 초록색 테두리
    FILLGREEN_DISABLE,      // 회색 버튼
    FILLWHITE_BORDERGRAY,   // 흰색 버튼, 회색 테두리(58%)
}

enum class CtaButtonStyle(
    val height: Dp,
    val horizontalPadding: Dp,
    val fontSize: TextUnit
) {
    TYPE_MINI(
        height = 24.dp,
        horizontalPadding = 8.dp,
        fontSize = 12.sp
    ),
    TYPE_SMALL(
        height = 32.dp,
        horizontalPadding = 12.dp,
        fontSize = 14.sp
    ),
    TYPE_MEDIUM(
        height = 40.dp,
        horizontalPadding = 16.dp,
        fontSize = 16.sp
    ),
    TYPE_LARGE(
        height = 48.dp,
        horizontalPadding = 12.dp,
        fontSize = 16.sp
    )
}

// 버튼 배경 색
@Composable
private fun ButtonType.backgroundColors(): CtaButtonColors = when (this) {
    ButtonType.FILLGREEN -> CtaButtonColors(
        normal = colorResource(R.color.c_00a862),
        pressed = colorResource(R.color.c_4ac18f),
        disabled = colorResource(R.color.c_1e000000)
    )
    ButtonType.FILLWHITE_BORDERGREEN -> CtaButtonColors(
        normal = colorResource(R.color.c_ffffff),
        pressed = colorResource(R.color.c_1900a862),
        disabled = colorResource(R.color.c_ffffff)
    )
    ButtonType.FILLGREEN_DISABLE -> CtaButtonColors(
        normal = colorResource(R.color.c_1e000000),
        pressed = colorResource(R.color.c_1e000000),
        disabled = colorResource(R.color.c_1e000000)
    )
    ButtonType.FILLWHITE_BORDERGRAY -> CtaButtonColors(
        normal = colorResource(R.color.c_ffffff),
        pressed = colorResource(R.color.c_ffffff),
        disabled = colorResource(R.color.c_ffffff)
    )
}

// 버튼 테두리 색
@Composable
private fun ButtonType.borderColors(): CtaButtonColors = when (this) {
    ButtonType.FILLGREEN -> CtaButtonColors(
        normal = colorResource(R.color.c_00a862),
        pressed = colorResource(R.color.c_00a862),
        disabled = colorResource(R.color.c_transparent)
    )
    ButtonType.FILLWHITE_BORDERGREEN -> CtaButtonColors(
        normal = colorResource(R.color.c_00a862),
        pressed = colorResource(R.color.c_4ac18f),
        disabled = colorResource(R.color.c_1e000000)
    )
    ButtonType.FILLGREEN_DISABLE -> CtaButtonColors(
        normal = colorResource(R.color.c_transparent),
        pressed = colorResource(R.color.c_transparent),
        disabled = colorResource(R.color.c_transparent)
    )
    ButtonType.FILLWHITE_BORDERGRAY -> CtaButtonColors(
        normal = colorResource(R.color.c_94000000),
        pressed = colorResource(R.color.c_94000000),
        disabled = colorResource(R.color.c_94000000)
    )
}

// 버튼 텍스트 색
@Composable
private fun ButtonType.fontColors(): CtaButtonColors = when (this) {
    ButtonType.FILLGREEN -> CtaButtonColors(
        normal = colorResource(R.color.c_ffffff),
        pressed = colorResource(R.color.c_ffffff),
        disabled = colorResource(R.color.c_ffffff)
    )
    ButtonType.FILLWHITE_BORDERGREEN -> CtaButtonColors(
        normal = colorResource(R.color.c_00a862),
        pressed = colorResource(R.color.c_00a862),
        disabled = colorResource(R.color.c_1e000000)
    )
    ButtonType.FILLGREEN_DISABLE -> CtaButtonColors(
        normal = colorResource(R.color.c_ffffff),
        pressed = colorResource(R.color.c_ffffff),
        disabled = colorResource(R.color.c_ffffff)
    )
    ButtonType.FILLWHITE_BORDERGRAY -> CtaButtonColors(
        normal = colorResource(R.color.c_94000000),
        pressed = colorResource(R.color.c_94000000),
        disabled = colorResource(R.color.c_94000000)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CtaButton(
    buttonType: ButtonType,
    ctaButtonStyle: CtaButtonStyle,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 배경색
    val backgroundColor = when {
        !enabled -> buttonType.backgroundColors().disabled   // disable
        isPressed -> buttonType.backgroundColors().pressed   // press
        else -> buttonType.backgroundColors().normal         // normal
    }

    // 테두리 색
    val borderColor = when {
        !enabled -> buttonType.borderColors().disabled    // disable
        isPressed -> buttonType.borderColors().pressed    // press
        else -> buttonType.borderColors().normal          // normal
    }

    // 텍스트 색
    val fontColor = when {
        !enabled -> buttonType.fontColors().disabled     // disable
        isPressed -> buttonType.fontColors().pressed     // press
        else -> buttonType.fontColors().normal           // normal
    }

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(ctaButtonStyle.height / 2),
            border = BorderStroke(0.5.dp,  borderColor),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                disabledContainerColor = backgroundColor
            ),
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = ctaButtonStyle.horizontalPadding),
            modifier = Modifier
                .fillMaxWidth()
                .height(ctaButtonStyle.height)
        ) {
            Text(
                text = text,
                fontSize = ctaButtonStyle.fontSize,
                color = fontColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 리플 효과를 그리지 않는 InteractionSource
//class NoRippleInteractionSource : MutableInteractionSource {
//    override val interactions: Flow<Interaction> = emptyFlow()
//    override suspend fun emit(interaction: Interaction) {}
//    override fun tryEmit(interaction: Interaction) = true
//}

@Preview
@Composable
fun CtaButtonDefaultPreview() {
    CtaButton(
        buttonType = ButtonType.FILLGREEN,
        ctaButtonStyle = CtaButtonStyle.TYPE_LARGE,
        text = stringResource(R.string.confirm),
        enabled = true,
        onClick = {}
    )
}