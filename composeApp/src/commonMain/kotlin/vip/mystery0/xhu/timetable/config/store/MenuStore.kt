package vip.mystery0.xhu.timetable.config.store

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object MenuStore {
    private const val MENU_LIST = "menuList"

    private val menuJson = Json { ignoreUnknownKeys = true }

    suspend fun loadAllMenu(): List<List<Menu>> {
        val list = withContext(Dispatchers.IO) {
            Store.MenuStore.getConfiguration(MENU_LIST, emptySet<String>())
        }
        if (list.isEmpty()) return emptyList()
        val menuGroup = LinkedHashMap<Int, ArrayList<Menu>>()
        list.forEach {
            val json = withContext(Dispatchers.IO) {
                Store.MenuStore.getConfiguration(it, "")
            }
            if (json.isBlank()) return@forEach
            val menu = menuJson.decodeFromString<Menu>(json)
            val menuList = menuGroup.getOrPut(menu.group) { ArrayList() }
            menuList.add(menu)
        }
        menuGroup.forEach { (_, l) ->
            l.sortBy { it.sort }
        }
        return menuGroup.keys.sorted().map { menuGroup[it]!! }
    }

    suspend fun updateList(list: List<Menu>) {
        Store.MenuStore.removeAll()
        val menuList = list.map { it.key }.sorted().toSet()
        withContext(Dispatchers.IO) {
            Store.MenuStore.setConfiguration(MENU_LIST, menuList)
            list.forEach { menu ->
                Store.MenuStore.setConfiguration(menu.key, menuJson.encodeToString(menu))
            }
        }
    }
}

@Serializable
data class Menu(
    //菜单id
    var key: String,
    //标题
    var title: String,
    //菜单顺序
    var sort: Int,
    //分组编号
    var group: Int,
    //当无法处理时显示的提示信息
    var hint: String,
    //跳转链接
    var link: String,
)