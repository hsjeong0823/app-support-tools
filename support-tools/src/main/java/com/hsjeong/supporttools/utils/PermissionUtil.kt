package com.hsjeong.supporttools.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri

class PermissionUtil {
    companion object {
        fun checkOverlayPermission(context: Context): Boolean {
            // Android M(6.0) 미만은 권한 선언만으로 바로 사용 가능
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true
            }

            // 현재 권한이 있는지 체크
            if (!Settings.canDrawOverlays(context)) {
                // 권한이 없으면 설정 화면으로 이동
                Toast.makeText(context, "'다른 앱 위에 그리기' 권한이 필요합니다.", Toast.LENGTH_LONG).show()

                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return false
            }

            return true
        }
    }
}