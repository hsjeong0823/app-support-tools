package com.hsjeong.supporttools.ui.preferenceviewer

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.core.view.WindowInsetsControllerCompat
import com.hsjeong.supporttools.R
import com.hsjeong.supporttools.ui.common.CommonToolBarH48
import com.hsjeong.supporttools.ui.common.noRippleClickable

@Composable
fun PreferenceViewerScreen(state: PreferenceViewerState,
                       onUiEventListener: ((PreferenceViewerUiEvent) -> Unit)? = null) {
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
            PreferenceViewerToolBarView(onCloseClick = { onUiEventListener?.invoke(PreferenceViewerUiEvent.Close) })
        },
        content = {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.c_ffffff))
                .padding(it)) {
                PreferenceViewerContentView(state = state, onUiEventListener = onUiEventListener)
            }
        }
    )
}

@Composable
fun PreferenceViewerToolBarView(onCloseClick: () -> Unit) {
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
                text = stringResource(R.string.preference_viewer_screen_title)
            )
        }
    )
}

@Composable
fun PreferenceViewerContentView(state: PreferenceViewerState, onUiEventListener: ((PreferenceViewerUiEvent) -> Unit)? = null) {
    Column {
        TextField(
            value = state.searchText,
            onValueChange = { onUiEventListener?.invoke(PreferenceViewerUiEvent.SearchPreferenceFiles(it)) },
            label = { Text(state.searchText.ifEmpty { "Search Files" }) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )

        LazyColumn {
            items(state.preferenceSearchedFiles) { fileName ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 10.dp)
                        .noRippleClickable { onUiEventListener?.invoke(PreferenceViewerUiEvent.FileSelect(fileName)) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fileName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = colorResource(R.color.c_94000000)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreferenceViewerScreenPreview() {
    PreferenceViewerScreen(state = PreferenceViewerState(
        preferenceSearchedFiles = listOf(
            "user_data_prefs",
            "network_settings",
            "auth_token_storage",
            "theme_cache"
        ),
        searchText = "user")
    )
}
