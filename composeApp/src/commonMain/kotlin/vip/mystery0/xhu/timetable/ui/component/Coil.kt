package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.runtime.Composable
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest

@Composable
fun loadCoilModelWithoutCache(data: Any?) =
    ImageRequest.Builder(LocalPlatformContext.current)
        .data(data)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .build()