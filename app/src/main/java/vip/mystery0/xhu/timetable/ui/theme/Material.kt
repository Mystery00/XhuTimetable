package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

object MaterialIcons {
    object TwoTone {
        val ArrowForwardIos: ImageVector =
            materialIcon(name = "TwoTone.ArrowForwardIos") {
                materialPath {
                    moveTo(6.23f, 20.23f)
                    lineToRelative(1.77f, 1.77f)
                    lineToRelative(10.0f, -10.0f)
                    lineToRelative(-10.0f, -10.0f)
                    lineToRelative(-1.77f, 1.77f)
                    lineToRelative(8.23f, 8.23f)
                    close()
                }
            }
        val ArrowDropUp: ImageVector =
            materialIcon(name = "TwoTone.ArrowDropUp") {
                materialPath {
                    moveTo(7.0f, 14.0f)
                    lineToRelative(5.0f, -5.0f)
                    lineToRelative(5.0f, 5.0f)
                    horizontalLineTo(7.0f)
                    close()
                }
            }
        val Clear: ImageVector =
            materialIcon(name = "TwoTone.Clear") {
                materialPath {
                    moveTo(19.0f, 6.41f)
                    lineTo(17.59f, 5.0f)
                    lineTo(12.0f, 10.59f)
                    lineTo(6.41f, 5.0f)
                    lineTo(5.0f, 6.41f)
                    lineTo(10.59f, 12.0f)
                    lineTo(5.0f, 17.59f)
                    lineTo(6.41f, 19.0f)
                    lineTo(12.0f, 13.41f)
                    lineTo(17.59f, 19.0f)
                    lineTo(19.0f, 17.59f)
                    lineTo(13.41f, 12.0f)
                    lineTo(19.0f, 6.41f)
                    close()
                }
            }
        val AccountCircle: ImageVector =
            materialIcon(name = "TwoTone.AccountCircle") {
                materialPath(fillAlpha = 0.3f, strokeAlpha = 0.3f) {
                    moveTo(12.0f, 4.0f)
                    curveToRelative(-4.41f, 0.0f, -8.0f, 3.59f, -8.0f, 8.0f)
                    curveToRelative(0.0f, 1.82f, 0.62f, 3.49f, 1.64f, 4.83f)
                    curveToRelative(1.43f, -1.74f, 4.9f, -2.33f, 6.36f, -2.33f)
                    reflectiveCurveToRelative(4.93f, 0.59f, 6.36f, 2.33f)
                    curveTo(19.38f, 15.49f, 20.0f, 13.82f, 20.0f, 12.0f)
                    curveToRelative(0.0f, -4.41f, -3.59f, -8.0f, -8.0f, -8.0f)
                    close()
                    moveTo(12.0f, 13.0f)
                    curveToRelative(-1.94f, 0.0f, -3.5f, -1.56f, -3.5f, -3.5f)
                    reflectiveCurveTo(10.06f, 6.0f, 12.0f, 6.0f)
                    reflectiveCurveToRelative(3.5f, 1.56f, 3.5f, 3.5f)
                    reflectiveCurveTo(13.94f, 13.0f, 12.0f, 13.0f)
                    close()
                }
                materialPath {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                    reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(7.07f, 18.28f)
                    curveToRelative(0.43f, -0.9f, 3.05f, -1.78f, 4.93f, -1.78f)
                    reflectiveCurveToRelative(4.51f, 0.88f, 4.93f, 1.78f)
                    curveTo(15.57f, 19.36f, 13.86f, 20.0f, 12.0f, 20.0f)
                    reflectiveCurveToRelative(-3.57f, -0.64f, -4.93f, -1.72f)
                    close()
                    moveTo(18.36f, 16.83f)
                    curveToRelative(-1.43f, -1.74f, -4.9f, -2.33f, -6.36f, -2.33f)
                    reflectiveCurveToRelative(-4.93f, 0.59f, -6.36f, 2.33f)
                    curveTo(4.62f, 15.49f, 4.0f, 13.82f, 4.0f, 12.0f)
                    curveToRelative(0.0f, -4.41f, 3.59f, -8.0f, 8.0f, -8.0f)
                    reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                    curveToRelative(0.0f, 1.82f, -0.62f, 3.49f, -1.64f, 4.83f)
                    close()
                    moveTo(12.0f, 6.0f)
                    curveToRelative(-1.94f, 0.0f, -3.5f, 1.56f, -3.5f, 3.5f)
                    reflectiveCurveTo(10.06f, 13.0f, 12.0f, 13.0f)
                    reflectiveCurveToRelative(3.5f, -1.56f, 3.5f, -3.5f)
                    reflectiveCurveTo(13.94f, 6.0f, 12.0f, 6.0f)
                    close()
                    moveTo(12.0f, 11.0f)
                    curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                    reflectiveCurveTo(11.17f, 8.0f, 12.0f, 8.0f)
                    reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                    reflectiveCurveTo(12.83f, 11.0f, 12.0f, 11.0f)
                    close()
                }
            }
        val Lock: ImageVector =
            materialIcon(name = "TwoTone.Lock") {
                materialPath(fillAlpha = 0.3f, strokeAlpha = 0.3f) {
                    moveTo(6.0f, 20.0f)
                    horizontalLineToRelative(12.0f)
                    lineTo(18.0f, 10.0f)
                    lineTo(6.0f, 10.0f)
                    verticalLineToRelative(10.0f)
                    close()
                    moveTo(12.0f, 13.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                    reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                    reflectiveCurveToRelative(-2.0f, -0.9f, -2.0f, -2.0f)
                    reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                    close()
                }
                materialPath {
                    moveTo(18.0f, 8.0f)
                    horizontalLineToRelative(-1.0f)
                    lineTo(17.0f, 6.0f)
                    curveToRelative(0.0f, -2.76f, -2.24f, -5.0f, -5.0f, -5.0f)
                    reflectiveCurveTo(7.0f, 3.24f, 7.0f, 6.0f)
                    verticalLineToRelative(2.0f)
                    lineTo(6.0f, 8.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(10.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(12.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    lineTo(20.0f, 10.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(9.0f, 6.0f)
                    curveToRelative(0.0f, -1.66f, 1.34f, -3.0f, 3.0f, -3.0f)
                    reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
                    verticalLineToRelative(2.0f)
                    lineTo(9.0f, 8.0f)
                    lineTo(9.0f, 6.0f)
                    close()
                    moveTo(18.0f, 20.0f)
                    lineTo(6.0f, 20.0f)
                    lineTo(6.0f, 10.0f)
                    horizontalLineToRelative(12.0f)
                    verticalLineToRelative(10.0f)
                    close()
                    moveTo(12.0f, 17.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                    reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                    reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                    close()
                }
            }
    }

    object Outlined {
        val Mood: ImageVector =
            materialIcon(name = "Outlined.Mood") {
                materialPath {
                    moveTo(11.99f, 2.0f)
                    curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                    curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                    reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                    close()
                    moveTo(12.0f, 20.0f)
                    curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                    reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                    reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                    reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                    close()
                    moveTo(15.5f, 11.0f)
                    curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                    reflectiveCurveTo(16.33f, 8.0f, 15.5f, 8.0f)
                    reflectiveCurveTo(14.0f, 8.67f, 14.0f, 9.5f)
                    reflectiveCurveToRelative(0.67f, 1.5f, 1.5f, 1.5f)
                    close()
                    moveTo(8.5f, 11.0f)
                    curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                    reflectiveCurveTo(9.33f, 8.0f, 8.5f, 8.0f)
                    reflectiveCurveTo(7.0f, 8.67f, 7.0f, 9.5f)
                    reflectiveCurveTo(7.67f, 11.0f, 8.5f, 11.0f)
                    close()
                    moveTo(12.0f, 17.5f)
                    curveToRelative(2.33f, 0.0f, 4.31f, -1.46f, 5.11f, -3.5f)
                    lineTo(6.89f, 14.0f)
                    curveToRelative(0.8f, 2.04f, 2.78f, 3.5f, 5.11f, 3.5f)
                    close()
                }
            }
        val InsertPhoto: ImageVector =
            materialIcon(name = "Outlined.InsertPhoto") {
                materialPath {
                    moveTo(19.0f, 5.0f)
                    verticalLineToRelative(14.0f)
                    lineTo(5.0f, 19.0f)
                    lineTo(5.0f, 5.0f)
                    horizontalLineToRelative(14.0f)
                    moveToRelative(0.0f, -2.0f)
                    lineTo(5.0f, 3.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(14.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(14.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    lineTo(21.0f, 5.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(14.14f, 11.86f)
                    lineToRelative(-3.0f, 3.87f)
                    lineTo(9.0f, 13.14f)
                    lineTo(6.0f, 17.0f)
                    horizontalLineToRelative(12.0f)
                    lineToRelative(-3.86f, -5.14f)
                    close()
                }
            }
    }
}