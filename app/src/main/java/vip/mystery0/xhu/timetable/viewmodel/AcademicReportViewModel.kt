package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.model.AcademicReport
import vip.mystery0.xhu.timetable.model.request.AcademicReportRequest
import java.time.Instant

class AcademicReportViewModel : ComposeViewModel() {
    private val serverApi: ServerApi by inject()

    private val _listState = MutableStateFlow<List<AcademicReport>>(emptyList())
    val listState: StateFlow<List<AcademicReport>> = _listState

    init {
        loadList("")
    }

    fun loadList(keywords: String) {
        viewModelScope.launch {
            val user = SessionManager.mainUser()
            val response = user.withAutoLogin {
                serverApi.getAcademicReportList(it, AcademicReportRequest(keywords)).checkLogin()
            }
            _listState.value = response.first
                .map {
                    AcademicReport(
                        it.title,
                        Instant.ofEpochMilli(it.reportTime).atZone(chinaZone).toLocalDateTime(),
                        it.location,
                        it.speaker,
                        it.organizer,
                        it.detailUrl,
                        it.articleContentHtml,
                    )
                }
        }
    }

}