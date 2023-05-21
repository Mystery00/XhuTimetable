package vip.mystery0.xhu.timetable.config.store

import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.module.moshiAdapter
import java.util.TreeMap

object MenuStore {
    private const val MENU_LIST = "menuList"
    private val kv = MMKV.mmkvWithID("MenuStore", MMKV.SINGLE_PROCESS_MODE)
    private val menuMoshi = moshiAdapter<Menu>()

    suspend fun loadAllMenu(): List<List<Menu>> {
        val list = withContext(Dispatchers.IO) { kv.decodeStringSet(MENU_LIST) } ?: emptySet()
        if (list.isEmpty()) return emptyList()
        val menuGroup = TreeMap<Int, ArrayList<Menu>>()
        list.forEach {
            val json = withContext(Dispatchers.IO) { kv.decodeString(it) } ?: return@forEach
            val menu = menuMoshi.fromJson(json) ?: return@forEach
            val menuList = menuGroup.getOrDefault(menu.group, ArrayList())
            menuList.add(menu)
            menuGroup[menu.group] = menuList
        }
        menuGroup.forEach { (_, l) ->
            l.sortBy { it.sort }
        }
        return menuGroup.values.toList()
    }

    suspend fun updateList(list: List<Menu>) {
        kv.clearAll()
        val menuList = list.map { it.key }.toSortedSet()
        withContext(Dispatchers.IO) {
            kv.encode(MENU_LIST, menuList)
            list.forEach { menu ->
                kv.encode(menu.key, menuMoshi.toJson(menu))
            }
        }
    }
}

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