package vip.mystery0.xhu.timetable.nativeui

import kotlinx.serialization.json.*
import vip.mystery0.xhu.timetable.config.store.Menu

fun profileMenus(groups: List<List<Menu>>, noticeUnread: Boolean, feedbackUnread: Boolean): JsonArray =
    JsonArray(groups.map { menus -> JsonArray(menus.map { menu ->
        val key = menu.key.uppercase()
        val route = when (key) {
            "QUERY_EXAM" -> "exams"
            "QUERY_SCORE" -> "scores"
            "EXP_SCORE" -> "expScores"
            "QUERY_FREE_ROOM" -> "rooms"
            "QUERY_TIMETABLE" -> "timetable"
            "ACCOUNT_MANAGE" -> "accounts"
            "CLASS_SETTING" -> "classSettings"
            "SETTINGS" -> "settings"
            "NOTICE" -> "notices"
            "FEEDBACK" -> "feedback"
            "URGE" -> "urge"
            "SPONSOR" -> "sponsors"
            else -> ""
        }
        val icon = when (key) {
            "QUERY_EXAM" -> "ic_exam"
            "QUERY_SCORE" -> "ic_score"
            "EXP_SCORE" -> "ic_exp_score"
            "QUERY_FREE_ROOM" -> "ic_classroom"
            "QUERY_TIMETABLE" -> "ic_school_timetable"
            "ACCOUNT_MANAGE" -> "ic_account_settings"
            "CLASS_SETTING" -> "ic_class_settings"
            "SETTINGS" -> "ic_settings"
            "NOTICE" -> "ic_notice"
            "FEEDBACK" -> "ic_feedback"
            "SHARE" -> "ic_share"
            "URGE" -> "ic_urge"
            "JOB" -> "ic_job"
            "SERVER_DETECT" -> "ic_server_detect"
            "JOIN_GROUP" -> "ic_join_group"
            else -> "ic_unknown_menu"
        }
        buildJsonObject {
            put("key", key)
            put("title", menu.title)
            put("icon", icon)
            menu.icon?.takeIf { it.isNotBlank() }?.let { put("iconUrl", it) }
            put("route", route)
            put("link", menu.link)
            put("hint", menu.hint)
            put("action", when {
                route.isNotEmpty() -> "route"
                key == "SHARE" -> "share"
                menu.link.isNotBlank() && (key in setOf("SERVER_DETECT", "JOIN_GROUP") || menu.hint.isBlank()) -> "link"
                else -> "hint"
            })
            put("unread", when (key) { "NOTICE" -> noticeUnread; "FEEDBACK" -> feedbackUnread; else -> false })
        }
    }) })
