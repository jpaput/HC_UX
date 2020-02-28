package com.heetch.technicaltest.features.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.heetch.technicaltest.R
import com.heetch.technicaltest.data.local.DriverModel
import com.heetch.technicaltest.util.RxPicasso
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.driver_itemview.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


private val AVATAR_BASE_URL = "http://hiring.heetch.com/"

class DriverAdapter(val context: Context)
    : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()


    var driversData: ArrayList<DriverModel> = arrayListOf();

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.driver_itemview, parent, false))
    }

    override fun getItemCount(): Int {
        return driversData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val driver = driversData.get(position)

        holder.driverNameTextview.text = driver.completeName
        holder.driverDistanceTextview.text = driver.getFormatedDistance()

        compositeDisposable.add(
            RxPicasso()
            .loadAvatarImage(AVATAR_BASE_URL + driver.image)
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
        )

        compositeDisposable.add(
            RxPicasso()
            .loadImage(driver.staticMap)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(5, TimeUnit.SECONDS)
            .subscribe(
                { data ->
                    holder.mapViewImage.setImageBitmap(data)
                },
                { error ->
                    println("Error: $error")
                }
            )
        )
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        compositeDisposable.dispose()
    }

    fun set(freshData : List<DriverModel>) {

        val diffResult = DiffUtil.calculateDiff(DriverDiffUtilCallBack(driversData, freshData))
        driversData.clear()
        driversData.addAll(freshData)
        diffResult.dispatchUpdatesTo(this)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val driverNameTextview = itemView.driver_name_tv
        val driverPicture = itemView.driver_picture
        val mapViewImage = itemView.driver_map
        val driverDistanceTextview = itemView.distance_tv
    }

    class DriverDiffUtilCallBack(val oldList: ArrayList<DriverModel>?, val newList: List<DriverModel>?) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return newList?.get(newItemPosition)?.equals(oldList?.get(oldItemPosition))!!
        }

        override fun getOldListSize(): Int {
            return oldList?.size ?: 0
        }

        override fun getNewListSize(): Int {
            return newList?.size ?: 0
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return newList?.get(newItemPosition)?.equals(oldList?.get(oldItemPosition))!!
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }
}
