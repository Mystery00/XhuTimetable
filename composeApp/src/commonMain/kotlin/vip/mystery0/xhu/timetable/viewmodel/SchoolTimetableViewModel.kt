package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.SelectDataLoader
import vip.mystery0.xhu.timetable.base.Selectable
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.UserStore.mainUser
import vip.mystery0.xhu.timetable.model.request.SchoolTimetableRequest
import vip.mystery0.xhu.timetable.model.response.SchoolTimetableResponse
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.CourseRepo
import vip.mystery0.xhu.timetable.repository.CustomCourseRepo

class SchoolTimetableViewModel(
    val campusSelect: CampusSelectDataLoader = CampusSelectDataLoader(),
    val collegeSelect: CollegeSelectDataLoader = CollegeSelectDataLoader(),
    val majorSelect: MajorSelectDataLoader = MajorSelectDataLoader(),
    private val errorHandlerFactory: ((Throwable) -> Unit) -> ((Throwable) -> Boolean) = ::networkErrorHandler,
) :
    PagingComposeViewModel<SchoolTimetableRequest, SchoolTimetableResponse>(
        {
            CourseRepo.getAllCourseListStream(it)
        }
    ) {
    private var majorLoadJob: Job? = null

    val init: Boolean
        get() = pageRequestFlow.value == null

    private val _saveLoadingState = MutableStateFlow(LoadingState())
    val saveLoadingState: StateFlow<LoadingState> = _saveLoadingState

    fun init() {
        viewModelScope.safeLaunch(onException = errorHandlerFactory { throwable ->
            logger.w("init school timetable selectors failed", throwable)
            toastMessage(throwable.desc())
        }) {
            campusSelect.init()
            collegeSelect.init()
            majorSelect.init()
        }
    }

    fun loadSchoolTimetable(courseName: String = "", teacherName: String = "") {
        fun failed(message: String) {
            logger.w("loadSchoolTimetable failed: $message")
            toastMessage(message)
        }

        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load school timetable list failed", throwable)
            failed(throwable.desc())
        }) {
            val campus = campusSelect.getSelected()
            val college = collegeSelect.getSelected()
            val major = majorSelect.getSelected()
            val courseName = courseName.ifBlank { null }
            val teacherName = teacherName.ifBlank { null }
            loadData(
                SchoolTimetableRequest(
                    campus?.value,
                    college?.value,
                    major?.value,
                    courseName,
                    teacherName,
                )
            )
        }
    }

    fun saveAsCustomCourse(
        schoolTimetable: SchoolTimetableResponse,
    ) {
        fun failed(message: String) {
            logger.w("saveAsCustomCourse failed: $message")
            toastMessage(message)
            _saveLoadingState.value = LoadingState(actionSuccess = false)
        }
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("save as custom course failed", throwable)
            failed(throwable.desc())
        }) {
            _saveLoadingState.value = LoadingState(loading = true, actionSuccess = false)

            val user = mainUser()
            schoolTimetable.customCourseList.forEach {
                CustomCourseRepo.createCustomCourse(user, it)
            }
            _saveLoadingState.value = LoadingState(actionSuccess = true)
            toastMessage("课程保存成功，请前往自定义课程查看")
        }
    }

    fun selectCampus(campus: String) {
        viewModelScope.safeLaunch {
            campusSelect.setSelected(campus)
        }
    }

    fun selectCollege(college: String) {
        majorLoadJob?.cancel()
        majorLoadJob = viewModelScope.safeLaunch(onException = errorHandlerFactory { throwable ->
            logger.w("load major list failed", throwable)
            toastMessage(throwable.desc())
        }) {
            collegeSelect.setSelected(college)
            majorSelect.updateCollegeId(college)
        }
    }

    fun selectMajor(major: String) {
        viewModelScope.safeLaunch {
            majorSelect.setSelected(major)
        }
    }

    data class LoadingState(
        val init: Boolean = false,
        val loading: Boolean = false,
        val actionSuccess: Boolean = true,
    )
}

@Immutable
data class CampusSelect(
    val value: String,
    override val title: String,
    override val selected: Boolean,
) : Selectable

class CampusSelectDataLoader(
    private val loader: suspend () -> Map<String, String> = CourseRepo::loadCampusList,
) : SelectDataLoader<CampusSelect, String>() {
    override suspend fun initSelect(): List<CampusSelect> {
        val map = loader()
        return map.map { (key, value) ->
            CampusSelect(value, key, false)
        }
    }

    override fun valueId(value: CampusSelect): String = value.value

    override fun updateSelect(
        t: CampusSelect,
        selected: Boolean
    ): CampusSelect = t.copy(selected = selected)
}

@Immutable
data class CollegeSelect(
    val value: String,
    override val title: String,
    override val selected: Boolean,
) : Selectable

class CollegeSelectDataLoader(
    private val loader: suspend () -> Map<String, String> = CourseRepo::loadCollegeList,
) : SelectDataLoader<CollegeSelect, String>() {
    override suspend fun initSelect(): List<CollegeSelect> {
        val map = loader()
        return map.map { (key, value) ->
            CollegeSelect(value, key, false)
        }
    }

    override fun valueId(value: CollegeSelect): String = value.value

    override fun updateSelect(
        t: CollegeSelect,
        selected: Boolean
    ): CollegeSelect = t.copy(selected = selected)
}

@Immutable
data class MajorSelect(
    val value: String,
    override val title: String,
    override val selected: Boolean,
) : Selectable

class MajorSelectDataLoader(
    private val loader: suspend (String) -> Map<String, String> = CourseRepo::loadMajorList,
) : SelectDataLoader<MajorSelect, String>() {
    private var collegeId: String? = null

    override suspend fun initSelect(): List<MajorSelect> {
        val currentCollegeId = collegeId
        if (currentCollegeId.isNullOrBlank()) return emptyList()
        val map = loader(currentCollegeId)
        return map.map { (key, value) ->
            MajorSelect(value, key, false)
        }
    }

    override fun valueId(value: MajorSelect): String = value.value

    override fun updateSelect(
        t: MajorSelect,
        selected: Boolean
    ): MajorSelect = t.copy(selected = selected)

    suspend fun updateCollegeId(collegeId: String) {
        this.collegeId = collegeId
        select.value = emptyList()
        init()
    }
}