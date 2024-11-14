import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

data class LatLng(val latitude: Double, val longitude: Double)

@Composable
fun AutoCenteredAndConstrainedPath(
    centerLatLng: LatLng,
    initialScale: Float,
    latLngList: List<LatLng>
) {
    if (latLngList.isEmpty()) return

    val scale by remember { mutableFloatStateOf(initialScale) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->

                    zoomScale *= zoom
                    val newOffsetX = (centroid.x - (centroid.x - offsetX) * zoom)
                    val newOffsetY = (centroid.y - (centroid.y - offsetY) * zoom)
                    offsetX = newOffsetX + pan.x
                    offsetY = newOffsetY + pan.y
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            fun longitudeToX(longitude: Double): Float {
                val x = lonToX(longitude, scale, centerLatLng.longitude, canvasWidth)
                return x * zoomScale + offsetX
            }

            fun latitudeToY(latitude: Double): Float {
                val y = latToY(latitude, scale, centerLatLng.latitude, canvasHeight)
                return y * zoomScale + offsetY
            }
            if (canvasWidth > 0 && canvasHeight > 0) {

                val path = Path()

                val startX = longitudeToX(latLngList[0].longitude)
                val startY = latitudeToY(latLngList[0].latitude)
                path.moveTo(startX, startY)
                path.lineTo(startX + 1, startY + 1)
                println("startX=$startX, startY=$startY")
                for (i in 1 until latLngList.size) {
                    val pointX = longitudeToX(latLngList[i].longitude)
                    val pointY = latitudeToY(latLngList[i].latitude)
                    path.lineTo(pointX, pointY)
                    println("pointX=$pointX, pointY=$pointY")
                }

                drawPath(
                    path = path,
                    color = Color.Blue,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

        }
    }


}

fun lonToX(longitude: Double, scale: Float, centerLongitude: Double, canvasWidth: Float): Float {
    val xOffset = (longitude - centerLongitude) * 10000 * scale
    return (canvasWidth / 2) + xOffset.toFloat()
}

fun latToY(latitude: Double, scale: Float, centerLatitude: Double, canvasHeight: Float): Float {
    val yOffset = -(latitude - centerLatitude) * 10000 * scale
    return (canvasHeight / 2) + yOffset.toFloat()
}

