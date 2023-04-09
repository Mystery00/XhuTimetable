package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.externalPictureDir
import vip.mystery0.xhu.timetable.repository.getSchoolCalendarList
import vip.mystery0.xhu.timetable.repository.getSchoolCalendarUrl
import vip.mystery0.xhu.timetable.utils.sha256
import java.io.File
import java.io.FileOutputStream

class SchoolCalendarViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "SchoolCalendarViewModel"
    }

    private val fileApi: FileApi by inject()

    private val _loading = MutableStateFlow(LoadingState())
    val loading: StateFlow<LoadingState> = _loading

    private val _area = MutableStateFlow<List<Area>>(emptyList())
    val area: StateFlow<List<Area>> = _area

    private val _schoolCalendarData = MutableStateFlow(SchoolCalendarData())
    val schoolCalendarData: StateFlow<SchoolCalendarData> = _schoolCalendarData

    init {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "changeArea failed", throwable)
            _loading.value = LoadingState(
                loading = false,
                errorMessage = throwable.message ?: throwable.javaClass.simpleName
            )
        }) {
            _loading.value = LoadingState(true)
            val mainUser = UserStore.getMainUser()
            if (mainUser == null) {
                _loading.value = LoadingState(loading = false, errorMessage = "用户未登录")
                return@launch
            }
            getSchoolCalendarList(mainUser).map {
                Area(it.area, it.resourceId)
            }.let {
                _area.value = it
                _loading.value = LoadingState(loading = false)
            }
            changeArea(_area.value[0].area)
        }
    }

    fun changeArea(area: String) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "changeArea failed", throwable)
            _loading.value = LoadingState(
                loading = false,
                errorMessage = throwable.message ?: throwable.javaClass.simpleName
            )
        }) {
            _loading.value = LoadingState(true)
            val mainUser = UserStore.getMainUser()
            if (mainUser == null) {
                _loading.value = LoadingState(loading = false, errorMessage = "用户未登录")
                return@launch
            }
            val pair = _area.value.firstOrNull { it.area == area }
            if (pair == null) {
                _loading.value = LoadingState(loading = false, errorMessage = "无效的校区")
                return@launch
            }
            if (pair.url.isNotBlank()) {
                _schoolCalendarData.value =
                    SchoolCalendarData(area = area, imageUrl = pair.url).doCache(fileApi)
            } else {
                getSchoolCalendarUrl(mainUser, pair.resourceId).let {
                    _schoolCalendarData.value =
                        SchoolCalendarData(area = area, imageUrl = it).doCache(fileApi)
                }
            }
            _loading.value = LoadingState(false)
        }
    }
}

data class Area(
    val area: String,
    val resourceId: Long,
    var url: String = "",
)

data class SchoolCalendarData(
    val area: String = "",
    val imageUrl: String = "",
) {
    val cacheFile: File
        get() {
            val dir = File(externalPictureDir, "calendar")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val extension = imageUrl.substringBefore("?").substringAfterLast(".")
            return File(dir, "${imageUrl.sha256()}.${extension}")
        }

    suspend fun doCache(fileApi: FileApi): SchoolCalendarData {
        val file = runOnCpu {
            cacheFile
        }
        Log.i("SchoolCalendarData", "doCache: save school calendar cache to ${file.absolutePath}")
        runOnIo {
            val response = fileApi.downloadFile(imageUrl)
            response.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return this
    }
}