package com.heetch.technicaltest.features

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.heetch.technicaltest.R
import com.heetch.technicaltest.data.local.DriverModel
import com.heetch.technicaltest.data.remote.DriverRemoteModel
import com.heetch.technicaltest.features.adapter.DriverAdapter
import com.heetch.technicaltest.location.LocationManager
import com.heetch.technicaltest.service.DriverService
import com.jakewharton.rxbinding3.view.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_drivers.*
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import java.io.Console

class DriversListActivity : AppCompatActivity() {

    val driverService = DriverService()
    lateinit var driverAdapter : DriverAdapter
    companion object {
        const val LOG_TAG = "DriversListActivity"
    }
    private val permissions =
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val compositeDisposable = CompositeDisposable()
    private lateinit var locationManager: LocationManager
    private lateinit var getDriverDisposable : Disposable
    private var isGetDriverRunning: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers)
        setSupportActionBar(drivers_toolbar)

        driverAdapter = DriverAdapter(this)

        drivers_listview.layoutManager = LinearLayoutManager(this)
        drivers_listview.adapter = driverAdapter

        locationManager = LocationManager(this)
        compositeDisposable.add(subscribeToFabClick())
    }

    private fun subscribeToFabClick(): Disposable {

        return drivers_fab.clicks()
            .flatMap {
                checkRunState()
                .flatMap {
                    if(it){
                        checkPermissions()
                            .flatMap {
                                getUserLocation() }
                            .doOnNext { runGetDrivers(it) }
                    }else{
                        Observable.just(true)
                    }
                }
            }
            .subscribe()
    }

    private fun checkRunState() : Observable<Boolean>{

        Log.d(LOG_TAG, "check run state : " + isGetDriverRunning)

        if(isGetDriverRunning){
            Log.d(LOG_TAG, "Removing GetDriverDisposable")

            compositeDisposable.remove(getDriverDisposable)
            drivers_fab.setImageDrawable(
                baseContext.resources.getDrawable(R.drawable.ic_play_arrow_black_24dp))
        }else{
            drivers_fab.setImageDrawable(
                baseContext.resources.getDrawable(R.drawable.ic_pause_black_24dp))
        }

        isGetDriverRunning = !isGetDriverRunning

        return Observable.just(isGetDriverRunning)
    }

    private fun runGetDrivers(location : Location){

        getDriverDisposable = Observable
            .interval(0, 5000, java.util.concurrent.TimeUnit.MILLISECONDS)
            .timeInterval()
            .flatMap { driverService.refreshDriver(location) }
            .flatMap { transformList(it, location) }
            .subscribe (
                { data ->
                    driverAdapter.updateData(data)
                    driverAdapter.notifyDataSetChanged()
                },
                { error -> println("Error: $error") }
            )

        isGetDriverRunning = true

        Log.d(LOG_TAG, "GetDriverDisposable is now running")

        compositeDisposable.add(getDriverDisposable)
    }

    private fun transformList(it: List<DriverRemoteModel> , location: Location) : Observable<List<DriverModel>> {
        return Observable.just(
            it.map {
                DriverModel(
                    it.id,
                    it.getFullName(),
                    it.image,
                    it.coordinates,
                    locationManager.getDistance(location, it.coordinates)
                )

            }
            .toList().sorted()
        )
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun checkPermissions(): Observable<Boolean> {
        return RxPermissions(this).request(*permissions)
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation(): Observable<Location> {
        return ReactiveLocationProvider(this).lastKnownLocation
    }

}