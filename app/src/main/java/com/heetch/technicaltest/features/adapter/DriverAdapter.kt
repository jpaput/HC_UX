package com.heetch.technicaltest.features.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.heetch.technicaltest.R
import com.heetch.technicaltest.network.DriverRemoteModel
import com.heetch.technicaltest.util.RxPicasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.driver_itemview.view.*
import java.util.concurrent.TimeUnit

class DriverAdapter(val context: Context)
    : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    var driversData: MutableList<DriverRemoteModel> = arrayListOf();

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.driver_itemview, parent, false))
    }

    override fun getItemCount(): Int {
        return driversData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val driver = driversData.get(position)

        holder.driverNameTextview.text = driver.getFullName()

        RxPicasso()
            .loadImage("http://hiring.heetch.com/" + driver.image)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(5, TimeUnit.SECONDS)
            .subscribe(
                { data ->
                    holder.driverPicture.setImageBitmap(data)
                },
                { error ->
                    println("Error: $error")
                }
            )
    }

    public fun updateData(data : List<DriverRemoteModel>){
        driversData.addAll(data)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val driverNameTextview = itemView.driver_name_tv
        val driverPicture = itemView.driver_picture
    }
}
