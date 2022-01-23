package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.inject
import retrofit2.Retrofit
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.*
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.interceptor.FileDownloadProgressInterceptor
import vip.mystery0.xhu.timetable.config.interceptor.FileDownloadProgressState
import vip.mystery0.xhu.timetable.externalPictureDir
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.model.response.BackgroundResponse
import vip.mystery0.xhu.timetable.ui.activity.formatFileSize
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha1
import vip.mystery0.xhu.timetable.utils.sha256
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class BackgroundViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "BackgroundViewModel"
    }

    private val eventBus: EventBus by inject()

    private val serverApi: ServerApi by inject()
    private val fileApi: FileApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(FileDownloadProgressInterceptor { progress ->
                val text =
                    "下载进度：${progress.received.formatFileSize()}/${progress.total.formatFileSize()} ${progress.progress.toInt()}%"
                _progressState.value = DownloadProgressState(progress, text = text)
            })
            .retryOnConnectionFailure(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://example.com/")
            .client(client)
            .build()
            .create(FileApi::class.java)
    }

    private var backgroundList: ArrayList<BackgroundResponse> = ArrayList()

    private val _backgroundListState = MutableStateFlow(BackgroundListState())
    val backgroundListState: StateFlow<BackgroundListState> = _backgroundListState

    private val _progressState =
        MutableStateFlow(DownloadProgressState(FileDownloadProgressState()))
    val progressState: StateFlow<DownloadProgressState> = _progressState

    init {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load background list failed", throwable)
            _backgroundListState.value = BackgroundListState(
                loading = false,
                errorMessage = throwable.message ?: throwable.javaClass.simpleName,
            )
        }) {
            _progressState.value =
                DownloadProgressState(FileDownloadProgressState(), finished = true)
            _backgroundListState.value = BackgroundListState(loading = true)
            val list = SessionManager.mainUser().withAutoLogin {
                serverApi.selectAllBackground(it).checkLogin()
            }.first
            backgroundList.clear()
            backgroundList.addAll(list)
            _backgroundListState.value = BackgroundListState(backgroundList = generateList())
        }
    }

    private suspend fun generateList(): List<Background> {
        val backgroundFile = getConfig { backgroundImage }
        val resultList =
            ArrayList(backgroundList.map {
                Background(
                    it.backgroundId,
                    it.resourceId,
                    thumbnailUrl = it.thumbnailUrl
                )
            })
        resultList.add(
            0,
            Background(0, 0L, imageResId = R.mipmap.main_bg)
        )
        if (backgroundFile == null) {
            resultList[0].checked = true
        } else {
            val fileName = backgroundFile.nameWithoutExtension
            resultList.forEach {
                val hashKey = it.getHashKey()
                it.checked = fileName == hashKey
            }
        }
        return resultList
    }

    fun setBackground(backgroundId: Long) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "set background failed", throwable)
            _backgroundListState.value = _backgroundListState.value.replaceMessage(
                throwable.message ?: throwable.javaClass.simpleName
            )
            _progressState.value =
                DownloadProgressState(
                    FileDownloadProgressState(),
                    finished = true,
                )
        }) {
            val nowList = _backgroundListState.value.backgroundList
            _backgroundListState.value = _backgroundListState.value.loadWithList(true)
            val current = nowList.find { it.checked }!!
            val selected = nowList.find { it.backgroundId == backgroundId }!!
            if (current.backgroundId == backgroundId) {
                _backgroundListState.value = _backgroundListState.value.loadWithList(false)
                return@launch
            }
            if (backgroundId == 0L) {
                setConfig { backgroundImage = null }
            } else {
                _progressState.value =
                    DownloadProgressState(FileDownloadProgressState(), text = "正在获取下载地址")
                val url = SessionManager.mainUser().withAutoLogin {
                    serverApi.getBackgroundUrl(it, selected.resourceId).checkLogin()
                }.first.url
                val file = runOnCpu {
                    val dir = File(externalPictureDir, "background")
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val extension = url.substringAfterLast(".")
                    val name =
                        "${
                            selected.backgroundId.toString().sha1()
                        }-${selected.thumbnailUrl.md5()}+${selected.resourceId}"
                    File(dir, "${name.sha256()}.${extension}")
                }
                runOnIo {
                    val response = fileApi.downloadFile(url)
                    response.byteStream().use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                setConfig { backgroundImage = file }
                _progressState.value =
                    DownloadProgressState(
                        FileDownloadProgressState(),
                        finished = true,
                    )
            }
            _backgroundListState.value =
                BackgroundListState(backgroundList = generateList(), errorMessage = "背景图设置成功")
            eventBus.post(UIEvent(EventType.CHANGE_MAIN_BACKGROUND))
        }
    }
}

data class BackgroundListState(
    val loading: Boolean = false,
    val backgroundList: List<Background> = emptyList(),
    val errorMessage: String = "",
) {
    fun loadWithList(loading: Boolean) = BackgroundListState(
        loading = loading,
        backgroundList = backgroundList,
        errorMessage = "",
    )

    fun replaceMessage(message: String) = BackgroundListState(
        loading = false,
        backgroundList = backgroundList,
        errorMessage = message,
    )
}

data class Background(
    var backgroundId: Long,
    var resourceId: Long,
    var thumbnailUrl: String = "",
    var imageResId: Int = 0,
    var checked: Boolean = false,
) {
    fun getHashKey(): String {
        val name = "${backgroundId.toString().sha1()}-${thumbnailUrl.md5()}+${resourceId}"
        return name.sha256()
    }
}

data class DownloadProgressState(
    val progress: FileDownloadProgressState,
    val text: String = "",
    val finished: Boolean = false,
)