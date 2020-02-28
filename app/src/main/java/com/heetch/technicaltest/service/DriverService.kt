package com.heetch.technicaltest.service

import android.location.Location
import com.heetch.technicaltest.data.local.DriverModel
import com.heetch.technicaltest.network.CoordinatesBody
import com.heetch.technicaltest.data.remote.DriverRemoteModel
import com.heetch.technicaltest.location.LocationManager
import com.heetch.technicaltest.network.NetworkManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class DriverService {

    val networkManager = NetworkManager()

    fun refreshDriver(location : Location) : Observable<List<DriverModel>>{
        return networkManager.getRepository()
            .getCoordinates(CoordinatesBody(location.latitude, location.longitude))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(20, TimeUnit.SECONDS)
            .flatMap { it -> digestData(location, it)}

    }

    private fun digestData(location : Location, it: List<DriverRemoteModel>) : Observable<List<DriverModel>> {
        return Observable.just(
            it.map {
                DriverModel(
                    it.id,
                    it.getFullName(),
                    it.image,
                    it.coordinates,
                    LocationManager.getDistance(location, it.coordinates),
                    LocationManager.generateSnapshotUrl(it.coordinates)
                )
            }.toList().sorted()
        )
    }
}