package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.externalPictureDir
import vip.mystery0.xhu.timetable.repository.SchoolCalendarRepo
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

    private val _area = MutableStateFlow<List<SchoolCalendarData>>(emptyList())
    val area: StateFlow<List<SchoolCalendarData>> = _area

    private val _schoolCalendarData = MutableStateFlow(SchoolCalendarData())
    val schoolCalendarData: StateFlow<SchoolCalendarData> = _schoolCalendarData

    init {
        fun failed(message: String) {
            Log.w(TAG, "load school calendar list failed, $message")
            _loading.value = LoadingState(false, message)
        }

        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "changeArea failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _loading.value = LoadingState(true)
            _area.value = SchoolCalendarRepo.getList()
                .map { SchoolCalendarData(it.area, it.imageUrl) }
            _loading.value = LoadingState(false)
            changeArea(_area.value.first().area)
        }
    }

    fun changeArea(area: String) {
        viewModelScope.launch {
            _area.value.first { it.area == area }.let {
                _schoolCalendarData.value = it.doCache(fileApi)
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
        val file = withContext(Dispatchers.Default) { cacheFile }
        Log.i(
            "SchoolCalendarData",
            "doCache: save school calendar cache to ${file.absolutePath}"
        )
        withContext(Dispatchers.IO) {
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