package com.hsjeong.supporttools.ui.environmentconfig

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.hsjeong.supporttools.R
import com.hsjeong.supporttools.ui.common.ButtonType
import com.hsjeong.supporttools.ui.common.CommonToolBarH48
import com.hsjeong.supporttools.ui.common.CtaButton
import com.hsjeong.supporttools.ui.common.CtaButtonStyle
import com.hsjeong.supporttools.ui.common.noRippleClickable

@Composable
fun EnvironmentConfigViewerScreen(
    json: String?,
    onCloseClick: () -> Unit,
    onCopyClick: () -> Unit,
) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowInsetsControllerCompat(window, view).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = { EnvironmentConfigViewerToolbar(onCloseClick) },
        bottomBar = { EnvironmentConfigViewerBottomBar(json, onCopyClick) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.c_ffffff))
                .padding(paddingValues),
        ) {
            if (json.isNullOrEmpty()) {
                Text(
                    text = stringResource(R.string.environment_config_unavailable),
                    color = colorResource(R.color.c_000000),
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                SelectionContainer {
                    Text(
                        text = json,
                        color = colorResource(R.color.c_000000),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState())
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentConfigViewerToolbar(onCloseClick: () -> Unit) {
    CommonToolBarH48(
        leftContent = {
            Image(
                painter = painterResource(R.drawable.arrow_left_b_48),
                contentDescription = stringResource(R.string.back),
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .noRippleClickable(onClick = onCloseClick),
            )
        },
        centerContent = {
            Text(
                text = stringResource(R.string.environment_config_viewer_screen_title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
private fun EnvironmentConfigViewerBottomBar(json: String?, onCopyClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(colorResource(R.color.c_ffffff))
            .padding(10.dp),
    ) {
        CtaButton(
            buttonType = ButtonType.FILLGREEN,
            ctaButtonStyle = CtaButtonStyle.TYPE_MEDIUM,
            text = stringResource(R.string.environment_config_copy),
            enabled = !json.isNullOrEmpty(),
            onClick = onCopyClick,
        )
    }
}

@Preview
@Composable
private fun EnvironmentConfigViewerScreenPreview() {
    EnvironmentConfigViewerScreen(
        json = """{"schemaVersion":1,"services":[]}""",
        onCloseClick = {},
        onCopyClick = {},
    )
}
