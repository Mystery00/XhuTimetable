package io.github.vinceglb.filekit.dialogs

public actual class FileKitDialogSettings private constructor() {
    public actual companion object {
        public actual fun createDefault(): FileKitDialogSettings = FileKitDialogSettings()
    }
}

public actual class FileKitOpenFileSettings private constructor() {
    public actual companion object {
        public actual fun createDefault(): FileKitOpenFileSettings = FileKitOpenFileSettings()
    }
}

public actual class FileKitShareSettings private constructor() {
    public actual companion object {
        public actual fun createDefault(): FileKitShareSettings = FileKitShareSettings()
    }
}

public actual class FileKitOpenCameraSettings private constructor() {
    public actual companion object {
        public actual fun createDefault(): FileKitOpenCameraSettings = FileKitOpenCameraSettings()
    }
}
