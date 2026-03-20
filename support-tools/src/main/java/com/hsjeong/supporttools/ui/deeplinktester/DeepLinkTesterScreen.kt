package com.hsjeong.supporttools.ui.deeplinktester

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
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
import com.hsjeong.supporttools.ui.common.ButtonType
import com.hsjeong.supporttools.ui.common.CommonToolBarH48
import com.hsjeong.supporttools.ui.common.CtaButton
import com.hsjeong.supporttools.ui.common.CtaButtonStyle
import com.hsjeong.supporttools.ui.common.noRippleClickable

@Composable
fun DeepLinkTesterScreen(
    viewModel: DeepLinkTesterViewModel,
    onUiEventListener: ((DeepLinkTesterUiEvent) -> Unit)? = null
) {
    val state = viewModel.state.value
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
            DeepLinkTesterToolBarView(onUiEventListener = onUiEventListener)
        },
        content = { padding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.c_ffffff))
                .padding(padding)) {
                DeepLinkTesterContentView(state = state, onUiEventListener = onUiEventListener)
            }
        }
    )
}

@Composable
fun DeepLinkTesterToolBarView(onUiEventListener: ((DeepLinkTesterUiEvent) -> Unit)? = null) {
    CommonToolBarH48(
        leftContent = {
            Image(
                painter = painterResource(R.drawable.arrow_left_b_48),
                contentDescription = stringResource(R.string.back),
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .noRippleClickable(onClick = { onUiEventListener?.invoke(DeepLinkTesterUiEvent.Close) })
            )
        },
        centerContent = {
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = stringResource(R.string.log_viewer_screen_title)
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeepLinkTesterContentView(state: DeepLinkTesterState, onUiEventListener: ((DeepLinkTesterUiEvent) -> Unit)? = null) {
    Column {
        // [상단] 직접 입력 섹션
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = state.uriText,
                onValueChange = { onUiEventListener?.invoke(DeepLinkTesterUiEvent.DeeplinkInputText(it)) },
                label = { Text("직접 입력 테스트") },
                placeholder = { Text("scheme://host/path") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (state.uriText.isNotEmpty()) {
                        IconButton(onClick = {
                            onUiEventListener?.invoke(DeepLinkTesterUiEvent.DeeplinkInputText(""))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = colorResource(R.color.c_94000000) // 옅은 회색 계열
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier
                .background(color = colorResource(R.color.c_ffffff))
                .padding(10.dp)) {
                CtaButton(
                    buttonType = ButtonType.FILLGREEN,
                    ctaButtonStyle = CtaButtonStyle.TYPE_MEDIUM,
                    text = "입력한 링크 실행",
                    enabled = true,
                    onClick = { onUiEventListener?.invoke(DeepLinkTesterUiEvent.DeeplinkLaunch(state.uriText)) }
                )
            }
        }

        HorizontalDivider(thickness = 8.dp, color = colorResource(R.color.c_f5f5f5))

        // [하단] 규격 리스트 섹션
        Text(
            "딥링크 목록",
            modifier = Modifier.padding(8.dp),
            color = colorResource(R.color.c_000000),
            fontSize = 16.sp
        )

        val interactionSource = remember { MutableInteractionSource() }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.predefinedDeepLinks) { data ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onUiEventListener?.invoke(DeepLinkTesterUiEvent.DeeplinkLaunch(data.uri)) },
                            onLongClick = { onUiEventListener?.invoke(DeepLinkTesterUiEvent.CopyToClipboard(data.uri)) },
                            indication = null,
                            interactionSource = interactionSource
                        )
                        .padding(8.dp)
                ) {
                    Text(text = data.title,
                        color = colorResource(R.color.c_000000),
                        fontSize = 14.sp)
                    Text(
                        text = data.uri,
                        color = colorResource(R.color.c_94000000),
                        fontSize = 10.sp
                    )
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}

@Preview
@Composable
fun DeepLinkTesterScreenPreview() {
    DeepLinkTesterScreen(viewModel = DeepLinkTesterViewModel())
}