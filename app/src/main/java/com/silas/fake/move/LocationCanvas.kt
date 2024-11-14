import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.silas.fake.move.LocationData
import com.silas.fake.move.LocationUtils
import com.silas.fake.move.LocationViewModel
import kotlin.math.pow
import kotlin.math.roundToInt

interface LatLng {
    val latitude: Double
    val longitude: Double
}

data class SimpleLatLng(override val latitude: Double, override val longitude: Double) : LatLng

@Composable
fun AutoCenteredAndConstrainedPath(
    centerLatLng: LatLng,
    initialScale: Float,
    viewModel: LocationViewModel
) {

    val latLngList = viewModel.itemList
    val scale by remember { mutableFloatStateOf(initialScale) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var trackLast by remember { mutableStateOf(true) }

    if (latLngList.isEmpty()) {
        Text("No location to draw...")
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    trackLast = false
                    zoomScale *= zoom
                    val newOffsetX = (centroid.x - (centroid.x - offsetX) * zoom)
                    val newOffsetY = (centroid.y - (centroid.y - offsetY) * zoom)
                    offsetX = newOffsetX + pan.x
                    offsetY = newOffsetY + pan.y
                    println("XXXX.zoomScale=$zoomScale")
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (trackLast) {
                val internalX: Float
                val internalY: Float
                val last = latLngList.last()
                internalX = canvasWidth / 2 - (lonToX(
                    last.longitude,
                    scale,
                    centerLatLng.longitude,
                    canvasWidth
                ) * zoomScale + offsetX)
                internalY = canvasHeight / 2 - (latToY(
                    last.latitude,
                    scale,
                    centerLatLng.latitude,
                    canvasHeight
                ) * zoomScale + offsetY)
                offsetX += internalX
                offsetY += internalY
            }
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
                var lastX = startX
                var lastY = startY
                for (i in 1 until latLngList.size) {
                    val pointX = longitudeToX(latLngList[i].longitude)
                    val pointY = latitudeToY(latLngList[i].latitude)
                    path.lineTo(pointX, pointY)
                    lastX = pointX
                    lastY = pointY
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
                val theSize = 6.dp.toPx()
                drawCircle(
                    color = Color.Red,
                    radius = theSize,
                    center = Offset(startX, startY)
                )
                drawCircle(
                    color = Color.Green,
                    radius = theSize,
                    center = Offset(lastX, lastY)
                )
            }

        }
        var time: Long? = null
        val first = latLngList.first()
        val last = latLngList.last()
        if (first is LocationData && last is LocationData) {
            time = last.time - first.time
        }
        ShowInfo(latLngList.size, LocationUtils.totalDistance(latLngList), time)
        Button(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp),
            onClick = {
                trackLast = true
            }
        ) {
            Text("Track Last")
        }
    }

}


@Composable
fun ShowInfo(dotCount: Int, distance: Double, time: Long?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        if (time != null) {
            Text("UsedTime: ${time.toTime()}")
        }
        Text(text = "DotCount: $dotCount")
        Text(text = "Distance: ${distance.round(2)} m")
        Row(
            modifier = Modifier.defaultMinSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "From: ")
            Canvas(
                modifier = Modifier.size(20.dp),
                onDraw = {
                    drawCircle(Color.Red, 8.dp.toPx())
                }
            )
        }
        Row(
            modifier = Modifier.defaultMinSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "To: ")
            Canvas(
                modifier = Modifier.size(20.dp),
                onDraw = {
                    drawCircle(Color.Green, 8.dp.toPx())
                }
            )
        }
    }
}

private fun Long.toTime(): String {
    val sb = StringBuilder()
    val hours = this / 3600000
    val minutes = (this - hours * 3600000) / 60000
    val seconds = (this - hours * 3600000 - minutes * 60000) / 1000
    if (hours < 10) sb.append("0")
    sb.append(hours).append(":")
    if (minutes < 10) sb.append("0")
    sb.append(minutes).append(":")
    if (seconds < 10) sb.append("0")
    sb.append(seconds)
    return sb.toString()
}

private fun Double.round(i: Int): String {
    val factor = 10.0.pow(i.toDouble())
    return ((this * factor).roundToInt() / factor).toString()
}

fun lonToX(longitude: Double, scale: Float, centerLongitude: Double, canvasWidth: Float): Float {
    val xOffset = (longitude - centerLongitude) * 10000 * scale
    return (canvasWidth / 2) + xOffset.toFloat()
}

fun latToY(latitude: Double, scale: Float, centerLatitude: Double, canvasHeight: Float): Float {
    val yOffset = -(latitude - centerLatitude) * 10000 * scale
    return (canvasHeight / 2) + yOffset.toFloat()
}

