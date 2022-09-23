package com.preonboarding.locationhistory.data.entity

import android.content.res.Resources
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.preonboarding.locationhistory.R
import kotlinx.parcelize.Parcelize
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import java.text.SimpleDateFormat

@Parcelize
@Entity
data class History(
    val time: String,

    val date: String,
    val latitude: Double,
    val longitude: Double,
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

fun Long.toFormatTime(): String = SimpleDateFormat("yyyy.MM.dd HH:mm").format(this)

fun Long.toFormatDate(): String = SimpleDateFormat("yyyy.MM.dd").format(this)

fun List<History>.toMapItem(): List<MapPOIItem> {
    return this.map {
        val position = MapPoint.mapPointWithGeoCoord(it.latitude, it.longitude)
        MapPOIItem().apply {
            tag = it.id
            itemName = ""
            mapPoint = position
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.RedPin
        }
    }
}