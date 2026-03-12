package com.hsjeong.supporttools.ui

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.hsjeong.supporttools.R
import com.hsjeong.supporttools.ui.common.ButtonType
import com.hsjeong.supporttools.ui.common.CommonCheckBox
import com.hsjeong.supporttools.ui.common.CommonToolBarH48
import com.hsjeong.supporttools.ui.common.CtaButton
import com.hsjeong.supporttools.ui.common.CtaButtonStyle
import com.hsjeong.supporttools.ui.common.noRippleClickable
import com.hsjeong.supporttools.utils.UrlConfigUtil

@Composable
fun SupportToolsScreen(viewModel: SupportToolsViewModel,
                       onUiEventListener: ((SupportToolsUiEvent) -> Unit)? = null) {
    val view = LocalView.current

    SideEffect {
        val window = (view.context as Activity).window
        val controller = WindowInsetsControllerCompat(window, view)

        // 상태바 아이콘 검정
        controller.isAppearanceLightStatusBars = true

        // 네비게이션바 아이콘 검정
        controller.isAppearanceLightNavigationBars = true
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            SupportToolsToolBarView(onCloseClick = { onUiEventListener?.invoke(SupportToolsUiEvent.Close) })
        },
        content = {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.c_ffffff))
                .padding(it)) {
                SupportToolsContentView(viewModel = viewModel, onUiEventListener = onUiEventListener)
            }
        },
        bottomBar = {
            Box(modifier = Modifier
                .background(color = colorResource(R.color.c_ffffff))
                .shadow(elevation = 10.dp)) {
                SupportToolsBottomBarView(viewModel = viewModel, onApplyClick = { onUiEventListener?.invoke(SupportToolsUiEvent.Apply) })
            }
        }
    )
}

@Composable
fun SupportToolsToolBarView(onCloseClick: () -> Unit) {
    CommonToolBarH48(
        leftContent = {
            Image(
                painter = painterResource(R.drawable.arrow_left_b_48),
                contentDescription = stringResource(R.string.back),
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .noRippleClickable(onClick = onCloseClick)
            )
        },
        centerContent = {
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = stringResource(R.string.setting)
            )
        }
    )
}

@Composable
fun SupportToolsBottomBarView(viewModel: SupportToolsViewModel, onApplyClick: () -> Unit) {
    Box(modifier = Modifier
            .background(color = colorResource(R.color.c_ffffff))
            .padding(10.dp)) {
        CtaButton(
            buttonType = ButtonType.FILLGREEN,
            ctaButtonStyle = CtaButtonStyle.TYPE_LARGE,
            text = stringResource(R.string.confirm),
            enabled = true,
            onClick = { onApplyClick() }
        )
    }
}

@Composable
fun SupportToolsContentView(viewModel: SupportToolsViewModel, onUiEventListener: ((SupportToolsUiEvent) -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        DebugSettingView(viewModel = viewModel, onUiEventListener = onUiEventListener)

        // 서버 URL 변경 설정 View
        if (viewModel.state.value.enableServerChange) {
            Spacer(modifier = Modifier.height(26.dp))
            ServerUrlSettingView(viewModel = viewModel, onUiEventListener = onUiEventListener)
        }

        // Preference viewer 버튼 View
        Spacer(modifier = Modifier.height(26.dp))
        Box(modifier = Modifier
            .background(color = colorResource(R.color.c_ffffff))) {
            CtaButton(
                buttonType = ButtonType.FILLWHITE_BORDERGREEN,
                ctaButtonStyle = CtaButtonStyle.TYPE_MEDIUM,
                text = stringResource(R.string.preference_viewer_button),
                enabled = true,
                onClick = { onUiEventListener?.invoke(SupportToolsUiEvent.MovePreferenceViewer) }
            )
        }
    }
}

@Composable
fun DebugSettingView(viewModel: SupportToolsViewModel, onUiEventListener: ((SupportToolsUiEvent) -> Unit)? = null) {
    val state = viewModel.state.value
    Text(text = "Debug 설정",
        fontSize = 20.sp,
        color = colorResource(R.color.c_000000))

    CommonCheckBox(
        checked = state.showScreenName,
        onCheckedChange = {
            viewModel.processIntent(
                SupportToolsIntent.ToggleScreenName(it)
            )
        },
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Text(text = "화면명 표시 설정",
            fontSize = 14.sp,
            color = colorResource(R.color.c_000000),
            modifier = Modifier.padding(start = 10.dp))
    }

    CommonCheckBox(
        checked = state.showLogcatViewer,
        onCheckedChange = {
            viewModel.processIntent(
                SupportToolsIntent.ToggleLogcatViewer(it)
            )
        },
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Text(text = "로그 뷰어 설정",
            fontSize = 14.sp,
            color = colorResource(R.color.c_000000),
            modifier = Modifier.padding(start = 10.dp))
    }

    CommonCheckBox(
        checked = state.showNetworkLog,
        onCheckedChange = {
            viewModel.processIntent(
                SupportToolsIntent.ToggleNetworkLog(it)
            )
        },
        modifier = Modifier.padding(top = 10.dp)) {
        Text(text = "서버 요청/응답 표시 설정",
            fontSize = 14.sp,
            color = colorResource(R.color.c_000000),
            modifier = Modifier.padding(start = 10.dp))
    }

    CommonCheckBox(
        checked = state.enableServerChange,
        onCheckedChange = {
            viewModel.processIntent(
                SupportToolsIntent.ToggleServerChange(it)
            )
        },
        modifier = Modifier.padding(top = 10.dp)) {
        Text(text = "서버 변경 설정",
            fontSize = 14.sp,
            color = colorResource(R.color.c_000000),
            modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
fun ServerUrlSettingView(viewModel: SupportToolsViewModel, onUiEventListener: ((SupportToolsUiEvent) -> Unit)? = null) {
    val state = viewModel.state.value

    Text(text = "서버 설정",
        fontSize = 20.sp,
        color = colorResource(R.color.c_000000))

    UrlConfigUtil.ServerType.entries.forEach { serverType ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable {
                    viewModel.processIntent(
                        SupportToolsIntent.SelectServer(serverType)
                    )
                }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = state.selectedServer == serverType,
                onClick = null
            )

            Text(
                text = serverType.name,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

    if (!state.selectUrls.isEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "서버 설정 목록",
            fontSize = 10.sp,
            color = colorResource(R.color.c_000000)
        )

        state.selectUrls.forEach {
            Text(it)
        }
    }
}

@Preview
@Composable
fun SupportToolsScreenPreview() {
    SupportToolsScreen(viewModel = SupportToolsViewModel())
}