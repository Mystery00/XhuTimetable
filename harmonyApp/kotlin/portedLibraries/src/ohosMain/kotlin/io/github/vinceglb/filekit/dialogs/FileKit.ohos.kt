package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow

private var fileSharingHandler: (suspend (List<PlatformFile>) -> Unit)? = null

public fun initializeFileSharing(handler: suspend (List<PlatformFile>) -> Unit) {
    fileSharingHandler = handler
}

internal actual suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>> =
    throw UnsupportedOperationException("鸿蒙预览版暂不支持此操作")

public actual suspend fun FileKit.openDirectoryPicker(
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = throw UnsupportedOperationException("鸿蒙预览版暂不支持此操作")

internal actual suspend fun FileKit.platformOpenFileSaver(
    suggestedName: String,
    defaultExtension: String?,
    allowedExtensions: Set<String>?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = throw UnsupportedOperationException("鸿蒙预览版暂不支持此操作")

public actual fun FileKit.openFileWithDefaultApplication(
    file: PlatformFile,
    openFileSettings: FileKitOpenFileSettings,
): Unit = throw UnsupportedOperationException("鸿蒙预览版暂不支持此操作")

public actual suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType,
    cameraFacing: FileKitCameraFacing,
    destinationFile: PlatformFile,
    openCameraSettings: FileKitOpenCameraSettings,
): PlatformFile? = throw UnsupportedOperationException("鸿蒙预览版暂不支持此操作")

public actual suspend fun FileKit.shareFile(
    file: PlatformFile,
    shareSettings: FileKitShareSettings,
) {
    shareFile(listOf(file), shareSettings)
}

public actual suspend fun FileKit.shareFile(
    files: List<PlatformFile>,
    shareSettings: FileKitShareSettings,
) {
    val handler = fileSharingHandler ?: throw FileKitDialogException("鸿蒙系统分享桥接尚未初始化")
    handler(files.toList())
}
