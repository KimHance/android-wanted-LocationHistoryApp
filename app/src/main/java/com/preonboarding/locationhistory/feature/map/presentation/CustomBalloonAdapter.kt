package com.preonboarding.locationhistory.feature.map.presentation

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.preonboarding.locationhistory.R
import com.preonboarding.locationhistory.feature.presentation.ADDRESS
import com.preonboarding.locationhistory.feature.presentation.MAX_RESULT
import com.preonboarding.locationhistory.feature.presentation.MainViewModel
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem

class CustomBalloonAdapter(
    inflater: LayoutInflater,
    private val viewModel: MainViewModel,
    private val context: Context
) : CalloutBalloonAdapter {

    val callOutBalloon: View = inflater.inflate(R.layout.item_balloon, null)
    val name = callOutBalloon.findViewById<TextView>(R.id.tv_name)
    val address = callOutBalloon.findViewById<TextView>(R.id.tv_address)

    override fun getCalloutBalloon(marker: MapPOIItem): View {
        val latitude = marker.mapPoint.mapPointGeoCoord.latitude
        val longitude = marker.mapPoint.mapPointGeoCoord.longitude
        address.text = getDetailAddress(latitude, longitude)
        return callOutBalloon
    }

    override fun getPressedCalloutBalloon(p0: MapPOIItem?): View {
        return callOutBalloon
    }

    private fun getDetailAddress(uLatitude: Double, uLongitude: Double): String {
        val geocoder = Geocoder(context)
        val convertAddress = geocoder
            .getFromLocation(uLatitude, uLongitude, MAX_RESULT)

        if (convertAddress.isEmpty()) {
            return context.getString(R.string.no_detail_location)
        } else {
            return convertAddress.get(ADDRESS).getAddressLine(ADDRESS).toString()
        }
    }
}