package com.hsjeong.supporttools.ui.logviewer

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hsjeong.supporttools.ui.base.BaseActivity
import com.hsjeong.supporttools.ui.logviewer.LogViewerIntent.*
import com.hsjeong.supporttools.utils.PreferencesUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import kotlin.getValue

class LogViewerActivity : BaseActivity() {
    companion object {
        fun startActivityNewTask(context: Context) {
            val intent = Intent(context, LogViewerActivity::class.java).apply {
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }

        fun start(context: Context) {
            val intent = Intent(context, LogViewerActivity::class.java)
            context.startActivity(intent)
        }

        const val FILE_NAME = "support_log.txt"
    }

    private val viewModel: LogViewerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            LogViewerScreen(state = state, onUiEventListener = { event ->
                onUiEvent(event = event)
            })
        }
    }

    private fun init() {
        val searchWord = PreferencesUtil.getLogcatViewerSearchWord(this)
        viewModel.processIntent(LogViewerIntent.LoadLogData(searchWord ?: ""))
    }

    private fun onUiEvent(event: LogViewerUiEvent) {
        when(event) {
            is LogViewerUiEvent.Close -> {
                finish()
            }

            is LogViewerUiEvent.Clear -> {
                viewModel.processIntent(LogViewerIntent.ClearLogData)
            }

            is LogViewerUiEvent.SearchLogData -> {
                PreferencesUtil.setLogcatViewerSearchWord(this, event.searchText)
                viewModel.processIntent(SearchLogData(event.searchText))
            }

            is LogViewerUiEvent.ShareLog -> {
                val logData = viewModel.state.value.logData
                if (logData.isNotEmpty()) {
                    val progressDialog = ProgressDialog(this).apply {
                        setMessage("로그를 저장 중입니다...")
                        setCancelable(false)
                        show()
                    }
                    val log = logData.joinToString(separator = "\n") { itemData -> itemData.log }
                    lifecycleScope.launch(Dispatchers.IO) {
                        saveLog(this@LogViewerActivity, log)
                        withContext(Dispatchers.Main) {
                            progressDialog.dismiss()
                            Toast.makeText(this@LogViewerActivity, "로그가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            shareLogFile(this@LogViewerActivity)
                        }
                    }
                } else {
                    Toast.makeText(this, "저장할 로그가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun saveLog(context: Context, logContent: String) {
        try {
            val logDir = File(context.getExternalFilesDir(null), "logs")
            if (!logDir.exists()) logDir.mkdirs()

            val logFile = File(logDir, FILE_NAME)

            // FileOutputStream의 두 번째 인자를 false로 주면 기존 내용을 삭제하고 새로 씁니다.
            FileOutputStream(logFile, false).use { fos ->
                OutputStreamWriter(fos).use { osw ->
                    osw.write(logContent)
                    osw.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogFile(context: Context): File? {
        val logFile = File(context.getExternalFilesDir(null), "logs/$FILE_NAME")
        return if (logFile.exists()) logFile else null
    }

    fun shareLogFile(context: Context) {
        val logFile = getLogFile(this)

        if (logFile == null || !logFile.exists()) {
            Toast.makeText(context, "저장된 로그 파일이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Content URI 생성
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.supporttools.fileprovider",
            logFile
        )

        // 2. 공유 Intent 생성
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "App Support Debug Logs")
            // 다른 앱이 파일에 접근할 수 있도록 권한 부여
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 3. 공유 창 띄우기
        startActivity(Intent.createChooser(shareIntent, "로그 파일 공유하기"))
    }
}