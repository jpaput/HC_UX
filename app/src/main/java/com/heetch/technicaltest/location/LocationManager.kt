package com.heetch.technicaltest.location

import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.heetch.technicaltest.data.remote.DriverRemoteModel
import com.heetch.technicaltest.util.RxPicasso
import io.reactivex.Observable

class LocationManager(private val context: Context) : MapSnapshotRetriever, AddressRetriever {

    companion object {
        const val API_KEY = "*****"
        const val DEFAULT_ZOOM = 18
        const val ICON_URL = "https%3A%2F%2Fs3-eu-west-1.amazonaws.com%2Fheetch-production%2Fassets%2Fproducts%2Fcar-image-lepro.png"

        fun getDistance(from: Location, to: Location) : Float {
            return from.distanceTo(to)
        }

        fun getDistance(from: Location, to : DriverRemoteModel.Coordinates) : Float {
            var targetLocation =  Location("");//provider name is unnecessary
            targetLocation.setLatitude(to.latitude);//your coords of course
            targetLocation.setLongitude(to.longitude)

            return getDistance(from, targetLocation)
        }

        fun generateSnapshotUrl(latitude: Double, longitude: Double): String {
            return "https://maps.googleapis.com/maps/api/staticmap?" +
                    "center=$latitude,$longitude" +
                    "&zoom=$DEFAULT_ZOOM" +
                    "&size=800x400" +
                    "&markers=anchor:center%7Cicon:$ICON_URL%7C$latitude,$longitude" +
                    "&key=$API_KEY"
        }

        fun generateSnapshotUrl(coordinates: DriverRemoteModel.Coordinates): String {
            return generateSnapshotUrl(coordinates.latitude, coordinates.longitude)
        }

    }

    override fun retrieveSnapshot(latitude: Double, longitude: Double): Observable<Bitmap> {
        val url = generateSnapshotUrl(latitude, longitude)
        return RxPicasso().loadImage(url)
    }


    override fun geocode(latitude: Double, longitude: Double): Observable<Address> {
        return Observable.fromCallable {
            Geocoder(context).getFromLocation(latitude, longitude, 1)
        }.flatMap {
            if (it.isNotEmpty()) {
                Observable.just(it[0])
            } else {
                Observable.empty()
            }
        }
    }



}
