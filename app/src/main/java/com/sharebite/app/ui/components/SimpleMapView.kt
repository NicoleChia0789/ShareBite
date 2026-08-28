package com.sharebite.app.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun SimpleMapView(
    latitude: Double,
    longitude: Double,
    markerTitle: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            Configuration.getInstance().userAgentValue = ctx.packageName

            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)

                // 关掉内建的 +/- 按钮，改用手指双指缩放（更顺、不会跟外层滑动冲突）
                setBuiltInZoomControls(false)
                setMultiTouchControls(true)

                val point = GeoPoint(latitude, longitude)
                controller.setZoom(15.0)
                controller.setCenter(point)

                val marker = Marker(this)
                marker.position = point
                marker.title = markerTitle
                overlays.add(marker)

                // 关键修复：手指碰到地图时，告诉外层的滑动页面"先别抢这个手势"
                // 这样在地图上滑动/缩放才会顺，不会被外层页面的滚动打断
                setOnTouchListener { view, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            view.parent.requestDisallowInterceptTouchEvent(true)
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            view.parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    false // 回传 false，让地图自己原本的手势处理（拖曳、缩放）继续正常运作
                }
            }
        }
    )
}