package io.github.vinceglb.filekit.coil

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath

internal actual val PlatformFile.underlyingFile: Any
    get() = absolutePath()
