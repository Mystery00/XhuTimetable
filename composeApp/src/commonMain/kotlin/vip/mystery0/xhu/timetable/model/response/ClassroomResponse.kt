package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ClassroomResponse(
    //场地编号
    val roomNo: String,
    //场地名称
    val roomName: String,
    //校区
    val campus: String,
    //场地类型
    val roomType: String,
    //座位数
    val seatCount: String,
    //考试座位数
    val examSeatCount: String,
    //楼号
    val buildingNo: String,
    //楼层号
    val floorNo: String,
    //场地借用类型
    val roomBorrowType: String,
    //场地备注信息
    val roomRemark: String,
    //使用部门
    val usedDepartment: String,
    //场地二级类别
    val roomSecondType: String,
    //托管部门
    val entrustedDepartment: String,
)
