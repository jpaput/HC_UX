package com.heetch.technicaltest.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heetch.technicaltest.data.local.DriverModel
import com.heetch.technicaltest.data.remote.DriverRemoteModel
import com.heetch.technicaltest.features.DriversListActivity
import com.heetch.technicaltest.location.LocationManager
import com.heetch.technicaltest.network.CoordinatesBody
import com.heetch.technicaltest.network.NetworkManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class DriverViewModel : ViewModel() {

    val networkManager = NetworkManager()

    val driverListLive = MutableLiveData<List<DriverModel>>()

    val empty = MutableLiveData<Boolean>().apply { value = false }

    val dataLoading = MutableLiveData<Boolean>().apply { value = false }

    val userMessage = MutableLiveData<String>()


    fun fetchDriverList(location: Location) {
        networkManager.getRepository()
            .getCoordinates(CoordinatesBody(location.latitude, location.longitude))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(5, TimeUnit.SECONDS)
            .flatMap { it -> digestData(location, it)}
            .subscribe({ data ->
                dataLoading.value = false
                driverListLive.value = data
                empty.value = false

            },
            { error ->
                    empty.value = true
                    userMessage.value = error.message

            })

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