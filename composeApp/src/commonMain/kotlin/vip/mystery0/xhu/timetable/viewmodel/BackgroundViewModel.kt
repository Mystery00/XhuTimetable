package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.write
import io.ktor.client.HttpClient
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.checkEqual
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.customImageDir
import vip.mystery0.xhu.timetable.config.externalPictureDir
import vip.mystery0.xhu.timetable.config.ktor.FileDownloadProgressState
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.response.BackgroundResponse
import vip.mystery0.xhu.timetable.module.HTTP_CLIENT_FILE
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.module.downloadFileTo
import vip.mystery0.xhu.timetable.repository.BackgroundRepo
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.utils.formatFileSize
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha1
import vip.mystery0.xhu.timetable.utils.sha256
import kotlin.time.Clock

class BackgroundViewModel : ComposeViewModel() {
    private val fileClient: HttpClient by inject(named(HTTP_CLIENT_FILE))

    private var backgroundList: ArrayList<BackgroundResponse> = ArrayList()

    private val _backgroundListState = MutableStateFlow(BackgroundListState())
    val backgroundListState: StateFlow<BackgroundListState> = _backgroundListState

    private val _progressState =
        MutableStateFlow(DownloadProgressState(FileDownloadProgressState()))
    val progressState: StateFlow<DownloadProgressState> = _progressState

    fun init() {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load background list failed", throwable)
            _backgroundListState.value = BackgroundListState(
                loading = false,
                errorMessage = throwable.desc(),
            )
        }) {
            _progressState.value =
                DownloadProgressState(FileDownloadProgressState(), finished = true)
            _backgroundListState.value = BackgroundListState(loading = true)
            val list = BackgroundRepo.getList()

            backgroundList.clear()
            backgroundList.addAll(list)
            val (backgroundList, selectedBackgroundId) = generateList()
            _backgroundListState.value = BackgroundListState(
                backgroundList = backgroundList,
                selectedBackgroundId = selectedBackgroundId,
            )
        }
    }

    private suspend fun generateList(): Pair<List<Background>, Long> {
        var selectedBackgroundId: Long? = null
        var backgroundFile = getConfigStore { backgroundImage }
        val resultList =
            ArrayList(backgroundList.map {
                Background(
                    it.backgroundId,
                    it.resourceId,
                    thumbnailUrl = it.thumbnailUrl,
                    imageUrl = it.imageUrl,
                )
            })
        resultList.add(
            0,
            Background(0, 0L, imageResUri = XhuImages.defaultBackgroundImageUri)
        )
        backgroundFile?.let {
            val parent = runCatching { it.parent() }
                .onFailure { Logger.w("get file parent failed") }
                .getOrNull()
            if (parent != null && parent.checkEqual(customImageDir)) {
                //自定义的背景图，添加到第2位
                resultList.add(
                    1,
                    Background(-1, -1L, thumbnailUrl = it.absolutePath())
                )
                selectedBackgroundId = -1L
            } else if (parent == null) {
                backgroundFile = null
            }
        }
        if (resultList.firstOrNull { b -> b.backgroundId == selectedBackgroundId } == null) {
            when {
                backgroundFile == null -> {
                    selectedBackgroundId = 0L
                }

                else -> {
                    val fileName = backgroundFile.nameWithoutExtension
                    selectedBackgroundId = resultList.first {
                        val hashKey = it.getHashKey()
                        fileName == hashKey
                    }.backgroundId
                }
            }
        }
        return resultList to selectedBackgroundId!!
    }

    fun setCustomBackground(bytes: ByteArray) {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("set custom background failed", throwable)
            _backgroundListState.value = _backgroundListState.value.replaceMessage(
                throwable.desc()
            )
        }) {
            val cacheImageFile = PlatformFile(
                customImageDir,
                "background-${Clock.System.now().toEpochMilliseconds()}.jpg"
            )
            if (!customImageDir.exists()) {
                customImageDir.createDirectories(true)
            }
            cacheImageFile.write(bytes)
            _backgroundListState.value = _backgroundListState.value.loadWithList(true)
            clearOldCustomImage()
            setConfigStore { backgroundImage = cacheImageFile }
            val (backgroundList, selectedBackgroundId) = generateList()
            _backgroundListState.value =
                BackgroundListState(
                    backgroundList = backgroundList,
                    selectedBackgroundId = selectedBackgroundId,
                    errorMessage = "背景图设置成功"
                )
            EventBus.post(EventType.CHANGE_MAIN_BACKGROUND)
        }
    }

    fun setBackground(backgroundId: Long) {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("set background failed", throwable)
            _backgroundListState.value = _backgroundListState.value.replaceMessage(
                throwable.desc()
            )
            _progressState.value =
                DownloadProgressState(
                    FileDownloadProgressState(),
                    finished = true,
                )
        }) {
            val nowList = _backgroundListState.value.backgroundList
            _backgroundListState.value = _backgroundListState.value.loadWithList(true)
            val current =
                nowList.firstOrNull { b -> b.backgroundId == _backgroundListState.value.selectedBackgroundId }
            val selected = nowList.find { it.backgroundId == backgroundId }!!
            if (current?.backgroundId == backgroundId) {
                _backgroundListState.value = _backgroundListState.value.loadWithList(false)
                return@safeLaunch
            }
            clearOldCustomImage()
            when (backgroundId) {
                0L -> setConfigStore { backgroundImage = null }
                -1L -> return@safeLaunch
                else -> {
                    _progressState.value =
                        DownloadProgressState(
                            FileDownloadProgressState(),
                            text = "正在获取下载地址..."
                        )
                    val url = selected.imageUrl
                    val file = withContext(Dispatchers.Default) {
                        val dir = PlatformFile(externalPictureDir, "background")
                        if (!dir.exists()) {
                            dir.createDirectories(true)
                        }
                        val extension = url.substringAfterLast(".")
                        val name = buildString {
                            append(selected.backgroundId.toString().sha1())
                            append("-")
                            append(selected.thumbnailUrl.md5())
                            append("+")
                            append(selected.resourceId)
                        }
                        PlatformFile(dir, "${name.sha256()}.${extension}")
                    }
                    withContext(Dispatchers.IO) {
                        fileClient.downloadFileTo(
                            saveFile = file,
                            builder = {
                                url(url)
                            },
                            progress = { progress ->
                                val text = buildString {
                                    append("下载进度：")
                                    append(progress.received.formatFileSize())
                                    append("/")
                                    append(progress.total.formatFileSize())
                                    append(" ")
                                    append(progress.progress.toInt())
                                    append("%")
                                }
                                _progressState.value = DownloadProgressState(progress, text = text)
                            })
                    }
                    setConfigStore { backgroundImage = file }
                    _progressState.value =
                        DownloadProgressState(
                            FileDownloadProgressState(),
                            finished = true,
                        )
                }
            }
            val (backgroundList, selectedBackgroundId) = generateList()
            _backgroundListState.value =
                BackgroundListState(
                    backgroundList = backgroundList,
                    selectedBackgroundId = selectedBackgroundId,
                    errorMessage = "背景图设置成功"
                )
            EventBus.post(EventType.CHANGE_MAIN_BACKGROUND)
        }
    }

    fun clearErrorMessage() {
        _backgroundListState.value = _backgroundListState.value.copy(errorMessage = "")
    }

    suspend fun clearOldCustomImage() {
        val oldImageFile = getConfigStore { backgroundImage }
        if (oldImageFile == null) {
            return
        }
        val parent = runCatching { oldImageFile.parent() }
            .onFailure { Logger.w("get file parent failed") }
            .getOrNull()
        if (parent != null && parent.checkEqual(customImageDir)) {
            runCatching { oldImageFile.delete() }
                .onFailure { Logger.w("delete old custom image failed") }
        }
    }
}

data class BackgroundListState(
    val loading: Boolean = false,
    val backgroundList: List<Background> = emptyList(),
    val selectedBackgroundId: Long? = null,
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
    val backgroundId: Long,
    val resourceId: Long,
    val thumbnailUrl: String = "",
    val imageUrl: String = "",
    val imageResUri: String? = null,
) {
    fun getHashKey(): String {
        val name = "${backgroundId.toString().sha1()}-${thumbnailUrl.md5()}+${resourceId}"
        return name.sha256()
    }
}

data class DownloadProgressState(
    val progress: FileDownloadProgressState,
    val text: String = "正在下载...",
    val finished: Boolean = false,
)