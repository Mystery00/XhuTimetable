package multiplatform.network.cmptoast

enum class ToastDuration {
    Short,
    Long,
}

fun showToast(
    message: String,
    duration: ToastDuration = ToastDuration.Short,
) {
    // 鸿蒙宿主由 ArkTS 原生弹窗与 Toast 负责提示，Kotlin 业务层无需直接调用 Compose Toast。
}
