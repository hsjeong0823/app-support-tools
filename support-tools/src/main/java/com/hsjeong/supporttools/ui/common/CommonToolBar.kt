package com.hsjeong.supporttools.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hsjeong.supporttools.R

/**
 * Copyright ⓒ 2025 Starbucks Coffee Company. All Rights Reserved.| Confidential
 *
 * @ Description : 공통 ToolBar
 * @ Class : CommonToolBar
 * @ Created by : jeonghwasoo
 * @ Created Date : 2025. 07. 18.
 */

@Composable
fun CommonToolBarH48(
    leftContent: (@Composable () -> Unit)? = null,
    centerContent:(@Composable () -> Unit)? = null,
    rightContent: (@Composable () -> Unit)? = null
) {
    val sideWidth = if (leftContent == null && rightContent == null) 0.dp else 48.dp    // 좌우 View가 없을 경우 영역 제거를 위해 0dp로 설정
    val sidePadding = 4.dp
    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        color = colorResource(R.color.c_ffffff)
    ) {
        // 좌우 영역
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sidePadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(sideWidth),
                contentAlignment = Alignment.CenterStart
            ) {
                leftContent?.invoke()
            }

            Spacer(modifier = Modifier.weight(1f)) // 가운데 컨텐츠 영역의 공간을 띄우기 위한 용도

            Box(
                modifier = Modifier
                    .width(sideWidth),
                contentAlignment = Alignment.CenterEnd
            ) {
                rightContent?.invoke()
            }
        }

        // 가운데 타이틀 중앙 고정
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sideWidth + sidePadding), // 좌우 여백 확보
            contentAlignment = Alignment.Center
        ) {
            centerContent?.invoke()
        }
    }
}

@Preview
@Composable
fun CommonToolBarPreview() {
    Box(modifier = Modifier.fillMaxSize().background(colorResource(R.color.c_ffffff))) {
        CommonToolBarH48(leftContent = { Text(text = "left") }, centerContent = { Text(text = "center") }, rightContent = { Text(text = "right") })
    }
}
