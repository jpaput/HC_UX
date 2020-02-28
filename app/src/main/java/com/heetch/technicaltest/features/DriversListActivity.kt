package com.heetch.technicaltest.features

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.heetch.technicaltest.R
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


private val INTERVAL: Long = 5000
private val NOW: Long = 0

class DriversListActivity : AppCompatActivity() {

    companion object {
        const val LOG_TAG = "DriversListActivity"
    }

    private val permissions =
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    private val compositeDisposable = CompositeDisposable()
    private lateinit var locationManager: LocationManager

    private var isRunning = false
    val driverService = DriverService()
    lateinit var driverAdapter: DriverAdapter

    lateinit var getDriverSubscription: Disposable

    private val getDriverObservable = Observable
        .interval(NOW, INTERVAL, java.util.concurrent.TimeUnit.MILLISECONDS)
        .timeInterval()
        .flatMap() {
            getUserLocation()
                .map {
                    driverService.refreshDriver(it)
                        .subscribe(
                            { data ->
                                if(data.size >0){
                                    empty_view.visibility = View.GONE
                                }
                                Log.d(LOG_TAG, "Fresh data incoming !")
                                driverAdapter.set(data)
                                driverAdapter.notifyDataSetChanged()
                            },
                            { error ->
                                Snackbar.make(
                                    coordinator,
                                    error.toString(),
                                    Snackbar.LENGTH_LONG)
                                    .setAction(getString(R.string.close), object : View.OnClickListener {
                                        override fun onClick(view: View?) {

                                        }
                                    })
                                    .setActionTextColor(resources.getColor(R.color.colorPrimaryDark))
                                    .show()
                            })
                }
        }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers)

        driverAdapter = DriverAdapter(this)

        drivers_listview.layoutManager = LinearLayoutManager(this)
        drivers_listview.adapter = driverAdapter

        locationManager = LocationManager(this)
        subscribeToFabClick()
    }

    private fun subscribeToFabClick(): Disposable {

        return drivers_fab.clicks()
            .flatMap {
                checkPermissions()
                    .flatMap {
                        changeRunState()
                    }
            }
            .subscribe()

    }

    private fun changeRunState(): Observable<Boolean> {

        if (isRunning) {
            Log.d(LOG_TAG, "Unsuscribe GetDriver !")

            progress_circular.visibility = View.GONE

            drivers_fab.setImageDrawable(
                baseContext.resources.getDrawable(R.drawable.ic_play_arrow_black_24dp)
            )

            compositeDisposable.remove(getDriverSubscription)

        } else {
            Log.d(LOG_TAG, "Suscribe GetDriver !")

            progress_circular.visibility = View.VISIBLE

            drivers_fab.setImageDrawable(
                baseContext.resources.getDrawable(R.drawable.ic_pause_black_24dp)
            )
            getDriverSubscription = getDriverObservable.subscribe()
            compositeDisposable.add(getDriverSubscription)
        }

        isRunning = !isRunning

        return checkRunState()
    }


    private fun checkRunState(): Observable<Boolean> {

        Log.d(LOG_TAG, "check run state : " + isRunning)
        return Observable.just(isRunning)
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


