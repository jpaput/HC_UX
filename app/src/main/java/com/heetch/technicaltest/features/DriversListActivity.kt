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
import com.heetch.technicaltest.features.adapter.DriverAdapter
import com.heetch.technicaltest.location.LocationManager
import com.heetch.technicaltest.network.CoordinatesBody
import com.heetch.technicaltest.network.DriverRemoteModel
import com.heetch.technicaltest.network.NetworkManager
import com.heetch.technicaltest.service.DriverService
import com.jakewharton.rxbinding3.view.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_drivers.*
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import java.util.concurrent.TimeUnit

class DriversListActivity : AppCompatActivity() {

    //TODO to delete

    val driverService = DriverService()

    lateinit var driverAdapter : DriverAdapter

    companion object {
        const val LOG_TAG = "DriversListActivity"
    }

    private val permissions =
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val compositeDisposable = CompositeDisposable()
    private lateinit var locationManager: LocationManager

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
            .doOnNext { Toast.makeText(this, "Play!", Toast.LENGTH_SHORT).show() }
            .flatMap {
                checkPermissions()
                    .flatMap { getUserLocation() }
                    .doOnNext {
                        Log.e(LOG_TAG, "Location : $it")
                        getDrivers(it)
                    }
            }
            .subscribe()
    }

    private fun getDrivers(location : Location){

        Observable
            .interval(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
            .timeInterval()
            .flatMap { driverService.refreshDriver(location) }
            .subscribe (
                { data ->
                    driverAdapter.updateData(data)
                    driverAdapter.notifyDataSetChanged()
                },
                { error -> println("Error: $error") }
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