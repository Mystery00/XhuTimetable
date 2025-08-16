package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.exists
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
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.externalPictureDir
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.module.HTTP_CLIENT_FILE
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.module.downloadFileTo
import vip.mystery0.xhu.timetable.repository.SchoolCalendarRepo
import vip.mystery0.xhu.timetable.utils.sha256

class SchoolCalendarViewModel : ComposeViewModel() {
    private val fileClient: HttpClient by inject(named(HTTP_CLIENT_FILE))

    private val _loading = MutableStateFlow(LoadingState())
    val loading: StateFlow<LoadingState> = _loading

    private val _area = MutableStateFlow<List<SchoolCalendarData>>(emptyList())
    val area: StateFlow<List<SchoolCalendarData>> = _area

    private val _schoolCalendarData = MutableStateFlow(SchoolCalendarData())
    val schoolCalendarData: StateFlow<SchoolCalendarData> = _schoolCalendarData

    fun init() {
        fun failed(message: String) {
            logger.w("load school calendar list failed, $message")
            _loading.value = LoadingState(false, message)
        }

        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("changeArea failed", throwable)
            failed(throwable.message ?: throwable.desc())
        }) {
            _loading.value = LoadingState(true)
            _area.value = SchoolCalendarRepo.getList()
                .map { SchoolCalendarData(it.area, it.imageUrl) }
            _loading.value = LoadingState(false)
            changeArea(_area.value.first().area)
        }
    }

    fun changeArea(area: String) {
        viewModelScope.safeLaunch {
            _area.value.first { it.area == area }.let {
                _schoolCalendarData.value = it.doCache(fileClient)
            }
        }
    }

    fun clearLoadingErrorMessage() {
        _loading.value = _loading.value.copy(errorMessage = "")
    }
}

data class SchoolCalendarData(
    val area: String = "",
    val imageUrl: String = "",
    var cacheFile: PlatformFile? = null,
) {
    private fun cacheFile(): PlatformFile {
        val dir = PlatformFile(externalPictureDir, "calendar")
        if (!dir.exists()) {
            dir.createDirectories(true)
        }
        val extension = imageUrl.substringBefore("?").substringAfterLast(".")
        return PlatformFile(dir, "${imageUrl.sha256()}.${extension}")
    }

    suspend fun doCache(fileClient: HttpClient): SchoolCalendarData {
        val file = cacheFile()
        cacheFile = file
        Logger.withTag("SchoolCalendarData")
            .i("doCache: save school calendar cache to ${file.absolutePath()}")
        withContext(Dispatchers.IO) {
            fileClient.downloadFileTo(
                saveFile = file,
                builder = {
                    url(imageUrl)
                },
            )
        }
        return this
    }
}