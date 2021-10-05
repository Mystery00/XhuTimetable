/*
 *                     GNU GENERAL PUBLIC LICENSE
 *                        Version 3, 29 June 2007
 *
 *  Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */

package vip.mystery0.xhu.timetable.model.event

data class UIEvent(val eventType: EventType)

enum class EventType {
    CHANGE_MAIN_USER,
    MAIN_USER_LOGOUT,
    MULTI_MODE_CHANGED,
}