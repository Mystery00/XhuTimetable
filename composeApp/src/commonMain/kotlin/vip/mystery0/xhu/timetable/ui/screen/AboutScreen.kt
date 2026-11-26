package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.painterResource
import vip.mystery0.xhu.timetable.base.appName
import vip.mystery0.xhu.timetable.base.appVersionCode
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.ic_app_icon
import kotlin.time.Clock

@Composable
fun AboutScreen() {
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "开源依赖库") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        },
    ) { innerPadding ->
        val libraries by produceLibraries {
            Res.readBytes("files/aboutlibraries.json").decodeToString()
        }
        LibrariesContainer(
            libraries,
            Modifier.fillMaxSize(),
            header = {
                item {
                    AppInfo()
                }
            },
            contentPadding = innerPadding,
        )
    }
}

@Composable
private fun AppInfo() {
    val scope = rememberCoroutineScope()
    val hits = remember { LongArray(5) }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_app_icon),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )
        Text(text = appName(), color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = "${appVersionName()}(${appVersionCode()})",
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    val now = Clock.System.now().toEpochMilliseconds()
                    hits.copyInto(hits, 0, 1, hits.size)
                    hits[hits.lastIndex] = now
                    if (hits[0] > 0L && hits[0] >= now - 3000L) {
                        hits.fill(0L)
                        scope.safeLaunch {
                            setConfigStore { debugMode = true }
                        }
                        showToast("开发者模式已启用！")
                    }
                }
                .padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}