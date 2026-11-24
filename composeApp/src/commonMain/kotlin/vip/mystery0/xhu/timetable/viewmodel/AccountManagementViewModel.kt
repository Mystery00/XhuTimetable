package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.customImageDir
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.CustomAccountTitle
import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.repository.UserRepo
import vip.mystery0.xhu.timetable.ui.theme.ProfileImages
import kotlin.time.Clock

class AccountManagementViewModel : ComposeViewModel() {
    private val _loggedUserList = MutableStateFlow<List<UserItem>>(emptyList())
    val loggedUserList: StateFlow<List<UserItem>> = _loggedUserList

    private val _customAccountTitle = MutableStateFlow(GlobalConfigStore.customAccountTitle)
    val customAccountTitle: StateFlow<CustomAccountTitle> = _customAccountTitle

    val customAvatar: StateFlow<String> = ProfileImages.customAvatarFlow

    fun init() {
        loadLoggedUserList()
    }

    fun loadLoggedUserList() {
        viewModelScope.safeLaunch {
            val mainUserId = UserStore.getMainUserId()
            val userList = UserStore.loggedUserList()
            _loggedUserList.value =
                userList.map {
                    UserItem(
                        it.studentId,
                        it.info.name,
                        it.info.gender,
                        it.studentId == mainUserId,
                        it.info.xhuGrade,
                        it.info.college,
                        it.info.majorName,
                        it.info.majorDirection,
                    )
                }.sortedBy { it.studentId }
        }
    }

    fun changeMainUser(studentId: String) {
        viewModelScope.safeLaunch {
            UserStore.setMainUser(studentId)
            EventBus.post(EventType.CHANGE_MAIN_USER)
            loadLoggedUserList()
        }
    }

    fun reloadUserInfo(studentId: String) {
        viewModelScope.safeLaunch {
            val loginUser = UserStore.getUserByStudentId(studentId) ?: return@safeLaunch
            val userInfo = UserRepo.reloadUserInfo(loginUser.token)
            //再获取一次数据，避免更新用户信息的请求时token变了
            val latestUser = UserStore.getUserByStudentId(studentId) ?: loginUser
            val user = latestUser.copy(info = userInfo)
            UserStore.updateUser(user)
            loadLoggedUserList()
        }
    }

    fun logoutUser(studentId: String) {
        viewModelScope.safeLaunch {
            val result = UserStore.logout(studentId)
            if (result) {
                EventBus.post(EventType.MAIN_USER_LOGOUT)
            }
            loadLoggedUserList()
        }
    }

    fun updateAccountTitleTemplate(customAccountTitle: CustomAccountTitle) {
        viewModelScope.safeLaunch {
            setConfigStore { this.customAccountTitle = customAccountTitle }
            _customAccountTitle.value = customAccountTitle
            EventBus.post(EventType.CHANGE_CUSTOM_ACCOUNT_TITLE)
        }
    }

    fun selectBuiltinAvatar(key: String) {
        viewModelScope.safeLaunch {
            clearOldCustomAvatarFile()
            val value = "${ProfileImages.BUILTIN_PREFIX}$key"
            setConfigStore { customAvatar = value }
            ProfileImages.updateCustomAvatar(value)
        }
    }

    fun saveCustomAvatar(bytes: ByteArray) {
        viewModelScope.safeLaunch {
            val savedFile = withContext(Dispatchers.IO) {
                if (!customImageDir.exists()) {
                    customImageDir.createDirectories(true)
                }
                val file = PlatformFile(
                    customImageDir,
                    "avatar-${Clock.System.now().toEpochMilliseconds()}.jpg",
                )
                file.write(bytes)
                file
            }
            clearOldCustomAvatarFile()
            val value = "${ProfileImages.FILE_PREFIX}${savedFile.absolutePath()}"
            setConfigStore { customAvatar = value }
            ProfileImages.updateCustomAvatar(value)
        }
    }

    fun resetCustomAvatar() {
        viewModelScope.safeLaunch {
            clearOldCustomAvatarFile()
            setConfigStore { customAvatar = "" }
            ProfileImages.updateCustomAvatar("")
        }
    }

    private suspend fun clearOldCustomAvatarFile() {
        val current = getConfigStore { customAvatar }
        if (!current.startsWith(ProfileImages.FILE_PREFIX)) return
        val path = current.removePrefix(ProfileImages.FILE_PREFIX)
        if (path.isBlank()) return
        withContext(Dispatchers.IO) {
            runCatching {
                val oldFile = PlatformFile(path)
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            }.onFailure {
                logger.w("delete old custom avatar failed", it)
            }
        }
    }
}

data class UserItem(
    //学号
    val studentId: String,
    //用户姓名
    val userName: String,
    //性别
    val gender: Gender,
    //是否为主用户
    val main: Boolean,
    val xhuGrade: Int,
    val college: String,
    val majorName: String,
    val majorDirection: String,
)