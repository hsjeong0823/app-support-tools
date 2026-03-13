package com.hsjeong.supporttools.ui.logviewer

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.hsjeong.supporttools.ui.SupportToolsViewModel
import com.hsjeong.supporttools.ui.common.ButtonType
import com.hsjeong.supporttools.ui.common.CommonToolBarH48
import com.hsjeong.supporttools.ui.common.CtaButton
import com.hsjeong.supporttools.ui.common.CtaButtonStyle
import com.hsjeong.supporttools.ui.common.noRippleClickable
import com.hsjeong.supporttools.utils.LogcatOverlayUtil
import kotlin.text.ifEmpty

@Composable
fun LogViewerScreen(state: LogViewerState,
                           onUiEventListener: ((LogViewerUiEvent) -> Unit)? = null) {
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
            LogViewerToolBarView(onUiEventListener = onUiEventListener)
        },
        content = {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.c_de000000))
                .padding(it)) {
                LogViewerContentView(state = state, onUiEventListener = onUiEventListener)
            }
        },
        bottomBar = {
            LogViewerBottomBarView(state = state, onUiEventListener = onUiEventListener)
        }
    )
}

@Composable
fun LogViewerToolBarView(onUiEventListener: ((LogViewerUiEvent) -> Unit)? = null) {
    CommonToolBarH48(
        leftContent = {
            Image(
                painter = painterResource(R.drawable.arrow_left_b_48),
                contentDescription = stringResource(R.string.back),
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .noRippleClickable(onClick = { onUiEventListener?.invoke(LogViewerUiEvent.Close) })
            )
        },
        centerContent = {
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = stringResource(R.string.log_viewer_screen_title)
            )
        },
        rightContent = {
            Image(
                painter = painterResource(android.R.drawable.ic_menu_delete),
                contentDescription = stringResource(R.string.back),
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .noRippleClickable(onClick = { onUiEventListener?.invoke(LogViewerUiEvent.Clear) })
            )
        }
    )
}

@Composable
fun LogViewerBottomBarView(state: LogViewerState, onUiEventListener: ((LogViewerUiEvent) -> Unit)? = null) {
    Box(modifier = Modifier
        .background(color = colorResource(R.color.c_ffffff))
        .padding(10.dp)) {
        CtaButton(
            buttonType = ButtonType.FILLGREEN,
            ctaButtonStyle = CtaButtonStyle.TYPE_MEDIUM,
            text = stringResource(R.string.share),
            enabled = true,
            onClick = { onUiEventListener?.invoke(LogViewerUiEvent.ShareLog) }
        )
    }
}

@Composable
fun LogViewerContentView(state: LogViewerState, onUiEventListener: ((LogViewerUiEvent) -> Unit)? = null) {
    Column {
        val listState = rememberLazyListState()
        TextField(
            value = state.searchText,
            onValueChange = { onUiEventListener?.invoke(LogViewerUiEvent.SearchLogData(it)) },
            label = { Text(state.searchText.ifEmpty { "Search Log" }) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.searchedLogData) { logData ->
                Text(
                    text = logData.log,
                    color = Color(LogcatOverlayUtil.logColor(logData.level)),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = colorResource(R.color.c_94ffffff)
                )
            }
        }

        LaunchedEffect(state.logData.size) {
            if (state.logData.isNotEmpty()) {
                listState.scrollToItem(state.logData.lastIndex)
            }
        }
    }
}

@Preview
@Composable
fun LogViewerScreenPreview() {
    LogViewerScreen(state = LogViewerState())
}