package com.hsjeong.supporttools.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hsjeong.supporttools.R

/**
 * 공통 체크박스 뷰
 *
 * @param checked : check on/off
 * @param onCheckedChange : 체크 변경 리스너
 * @param enabled : enable on/off
 * @param modifier : Modifier
 * @param content : 체크박스 옆에 들어간 Composable
 */
@Composable
fun CommonCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = null
            )
    ) {
        val imageRes = when {
            !enabled -> R.drawable.checkbox_dim_new
            checked -> R.drawable.checkbox_on_new
            else -> R.drawable.checkbox_off_new
        }

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        content()
    }
}

@Preview
@Composable
fun CommonCheckBoxDefaultPreview() {
    CommonCheckBox(checked = false, onCheckedChange = {}, modifier = Modifier) {
        Text(text = "TEST")
    }
}