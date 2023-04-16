package vip.mystery0.xhu.timetable.model.response

data class AggregationMainPageResponse(
    //课程列表
    val courseList: List<Course>,
    //实践课程列表
    val practicalCourseList: List<PracticalCourse>,
    //实验课程列表
    val experimentCourseList: List<ExperimentCourse>,
    //自定义课程列表
    val customCourseList: List<CustomCourseResponse>,
    //自定义事项列表
    val customThingList: List<CustomThingResponse>,
)
