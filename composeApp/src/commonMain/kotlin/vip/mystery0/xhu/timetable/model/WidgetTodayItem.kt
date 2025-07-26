package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color

data class WidgetTodayItem(
    //标题
    val title: String,
    //显示时间
    val showTime: List<String>,
    //地点
    val location: String,
    //颜色
    val color: Color,
    //开始时间，用来最终排序
    val startTime: Long,
)