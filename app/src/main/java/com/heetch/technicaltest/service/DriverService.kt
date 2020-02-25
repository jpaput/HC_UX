package com.heetch.technicaltest.service

import android.location.Location
import com.heetch.technicaltest.network.CoordinatesBody
import com.heetch.technicaltest.network.DriverRemoteModel
import com.heetch.technicaltest.network.NetworkManager
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class DriverService {

    val networkManager = NetworkManager()

    fun refreshDriver(location : Location) : Observable<List<DriverRemoteModel>>{
        return networkManager.getRepository()
            .getCoordinates(CoordinatesBody(location.latitude, location.longitude))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(20, TimeUnit.SECONDS)

    }


}