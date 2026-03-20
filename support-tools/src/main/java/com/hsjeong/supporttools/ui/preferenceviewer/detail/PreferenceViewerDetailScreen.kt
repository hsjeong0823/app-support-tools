package com.hsjeong.supporttools.ui.preferenceviewer.detail

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.hsjeong.supporttools.R
import com.hsjeong.supporttools.ui.common.ButtonType
import com.hsjeong.supporttools.ui.common.CommonToolBarH48
import com.hsjeong.supporttools.ui.common.CtaButton
import com.hsjeong.supporttools.ui.common.CtaButtonStyle
import com.hsjeong.supporttools.ui.common.noRippleClickable
import com.hsjeong.supporttools.utils.PreferenceItem


@Composable
fun PreferenceViewerDetailScreen(state: PreferenceViewerDetailState,
                           onUiEventListener: ((PreferenceViewerDetailUiEvent) -> Unit)? = null) {
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
            PreferenceViewerDetailToolBarView(title = state.fileName, onCloseClick = { onUiEventListener?.invoke(PreferenceViewerDetailUiEvent.Close) })
        },
        content = {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.c_ffffff))
                .padding(it)) {
                PreferenceViewerDetailContentView(state = state, onUiEventListener = onUiEventListener)
            }
        }
    )
}

@Composable
fun PreferenceViewerDetailToolBarView(title: String?, onCloseClick: () -> Unit) {
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
                text = title ?: ""
            )
        }
    )
}

@Composable
fun PreferenceViewerDetailContentView(state: PreferenceViewerDetailState, onUiEventListener: ((PreferenceViewerDetailUiEvent) -> Unit)? = null) {
    var selectedItem by remember { mutableStateOf<PreferenceItem?>(null) }

    Column {
        TextField(
            value = state.searchText,
            onValueChange = { onUiEventListener?.invoke(PreferenceViewerDetailUiEvent.SearchPreferenceItems(it)) },
            label = { Text("Search Data") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            trailingIcon = {
                if (state.searchText.isNotEmpty()) {
                    IconButton(onClick = {
                        onUiEventListener?.invoke(PreferenceViewerDetailUiEvent.SearchPreferenceItems(""))
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

        LazyColumn {
            items(state.preferenceSearchedItems) { item ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .noRippleClickable { selectedItem = item}) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "KEY : "
                        )

                        Text(
                            text = item.key,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "VALUE : "
                        )

                        Text(
                            text = item.value.toString(),
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = colorResource(R.color.c_94000000)
                )
            }
        }
    }

    selectedItem?.let { item ->
        EditPreferenceDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onSave = { newValue ->
                onUiEventListener?.invoke(PreferenceViewerDetailUiEvent.ModifyPreference(state.fileName, item, newValue))
                selectedItem = null
            }
        )
    }
}

@Composable
fun EditPreferenceDialog(
    item: PreferenceItem,
    onDismiss: () -> Unit,
    onSave: (Any) -> Unit
) {
    // 임시 입력 값 상태
    var tempValue by remember { mutableStateOf(item.value.toString()) }
    var tempBoolean by remember { mutableStateOf(item.value as? Boolean ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = item.key) },
        text = {
            Column {
                Text(text = "Type: ${item.type}", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(8.dp))

                if (item.value is Boolean) {
                    // Boolean은 스위치로 처리
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Value: ")
                        Switch(checked = tempBoolean, onCheckedChange = { tempBoolean = it })
                    }
                } else {
                    // 나머지는 텍스트 입력
                    TextField(
                        value = tempValue,
                        onValueChange = { tempValue = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // 오른쪽 정렬
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    // 취소 버튼
                    CtaButton(
                        buttonType = ButtonType.FILLWHITE_BORDERGREEN,
                        ctaButtonStyle = CtaButtonStyle.TYPE_MEDIUM,
                        text = stringResource(R.string.cancel),
                        onClick = onDismiss
                    )
                }

                Spacer(modifier = Modifier.width(8.dp)) // 버튼 사이 간격

                Box(modifier = Modifier.weight(1f)) {
                    // 확인 버튼
                    CtaButton(
                        buttonType = ButtonType.FILLGREEN,
                        ctaButtonStyle = CtaButtonStyle.TYPE_MEDIUM,
                        text = stringResource(R.string.confirm),
                        onClick = {
                            val finalValue: Any = when (item.value) {
                                is Boolean -> tempBoolean
                                is Int -> tempValue.toIntOrNull() ?: item.value
                                is Long -> tempValue.toLongOrNull() ?: item.value
                                is Float -> tempValue.toFloatOrNull() ?: item.value
                                else -> tempValue
                            }
                            onSave(finalValue)
                        }
                    )
                }
            }
        },
        dismissButton = null
    )
}

@Preview
@Composable
fun PreferenceViewerDetailScreenPreview() {
    PreferenceViewerDetailScreen(state = PreferenceViewerDetailState())
}