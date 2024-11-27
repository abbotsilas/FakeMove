import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.silas.fake.move.DRAW_SCALE_FACTOR
import com.silas.fake.move.LocationData
import com.silas.fake.move.LocationUtils
import com.silas.fake.move.LocationViewModel
import com.silas.fake.move.round
import com.silas.fake.move.toTime

interface LatLng {
    val latitude: Double
    val longitude: Double
}

data class OriginalInfo(val baseList: List<LocationData>, val progress: Double)

data class SimpleLatLng(override val latitude: Double, override val longitude: Double) : LatLng

private class CanvasViewModel(val centerLatLng: LatLng, val scale: Float, tract: Boolean = false) : ViewModel() {
    var zoomScale by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)
    var trackLast by mutableStateOf(tract)
}


@Composable
fun AutoCenteredAndConstrainedPath(
    centerLatLng: LatLng,
    initialScale: Float,
    viewModel: LocationViewModel,
    baseList: List<LocationData>? = null
) {
    val locationList = viewModel.locationList
    if (locationList.isEmpty()) {
        Text("No location to draw...")
        return
    }
    val canvasModel = remember { CanvasViewModel(centerLatLng, initialScale, viewModel.traceLast) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    canvasModel.trackLast = false
                    canvasModel.zoomScale *= zoom
                    val newOffsetX = (centroid.x - (centroid.x - canvasModel.offsetX) * zoom)
                    val newOffsetY = (centroid.y - (centroid.y - canvasModel.offsetY) * zoom)
                    canvasModel.offsetX = newOffsetX + pan.x
                    canvasModel.offsetY = newOffsetY + pan.y
                    println("XXXX.zoomScale=${canvasModel.zoomScale}")
                }
            }
    ) {

        ShowInfo(baseList?.let {
            val progress = viewModel.matchedProgress
            if (progress.isNaN()) {
                return
            } else {
                OriginalInfo(it, progress)
            }
        }, locationList)
        if (baseList != null) {
            CanvasInfo(canvasModel, baseList, false, Color.LightGray, Color.LightGray, Color.LightGray)
        }
        CanvasInfo(canvasModel, locationList, true, Color.Red, Color.Green)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.Center)
                    .border(1.dp, Color.LightGray, CircleShape)
                    .size(40.dp),
                onClick = {
                    canvasModel.trackLast = true
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Custom Icon",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }

}

@Composable
private fun CanvasInfo(
    canvasModel: CanvasViewModel,
    latLngList: List<LocationData>,
    shouldTrack: Boolean = false,
    startColor: Color = Color.Red,
    endColor: Color = Color.Green,
    lineColor: Color = Color.Blue
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (shouldTrack && canvasModel.trackLast) {
            val internalX: Float
            val internalY: Float
            val last = latLngList.last()
            internalX = canvasWidth / 2 - (lonToX(
                last.longitude,
                canvasModel.scale,
                canvasModel.centerLatLng.longitude,
                canvasWidth
            ) * canvasModel.zoomScale + canvasModel.offsetX)
            internalY = canvasHeight / 2 - (latToY(
                last.latitude,
                canvasModel.scale,
                canvasModel.centerLatLng.latitude,
                canvasHeight
            ) * canvasModel.zoomScale + canvasModel.offsetY)
            canvasModel.offsetX += internalX
            canvasModel.offsetY += internalY
        }
        fun longitudeToX(longitude: Double): Float {
            val x = lonToX(longitude, canvasModel.scale, canvasModel.centerLatLng.longitude, canvasWidth)
            return x * canvasModel.zoomScale + canvasModel.offsetX
        }

        fun latitudeToY(latitude: Double): Float {
            val y = latToY(latitude, canvasModel.scale, canvasModel.centerLatLng.latitude, canvasHeight)
            return y * canvasModel.zoomScale + canvasModel.offsetY
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
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            val theSize = 6.dp.toPx()
            drawCircle(
                color = startColor,
                radius = theSize,
                center = Offset(startX, startY)
            )
            drawCircle(
                color = endColor,
                radius = theSize,
                center = Offset(lastX, lastY)
            )
        }

    }
}


@Composable
fun ShowInfo(originalInfo: OriginalInfo?, locationList: List<LocationData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        val time = locationList.last().time - locationList.first().time
        Text("SpentTime: ${time.toTime()}")
        val currentDistance = LocationUtils.totalDistance(locationList)
        var distanceText = ""
        if (originalInfo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Progress: ${(originalInfo.progress * 100).round(2)}%")
                LinearProgressIndicator(
                    modifier = Modifier
                        .height(14.dp)
                        .weight(1f)
                        .padding(4.dp),
                    progress = { originalInfo.progress.toFloat() }
                )
            }
        }
        Text(text = "Distance: ${currentDistance.round(2)} m")
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

fun lonToX(longitude: Double, scale: Float, centerLongitude: Double, canvasWidth: Float): Float {
    val xOffset = (longitude - centerLongitude) * DRAW_SCALE_FACTOR * scale
    return (canvasWidth / 2) + xOffset.toFloat()
}

fun latToY(latitude: Double, scale: Float, centerLatitude: Double, canvasHeight: Float): Float {
    val yOffset = -(latitude - centerLatitude) * DRAW_SCALE_FACTOR * scale
    return (canvasHeight / 2) + yOffset.toFloat()
}

