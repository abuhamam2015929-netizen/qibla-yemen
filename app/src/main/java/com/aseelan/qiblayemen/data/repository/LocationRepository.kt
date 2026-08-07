package com.aseelan.qiblayemen.data.repository

import android.content.Context
import com.aseelan.qiblayemen.data.local.AppDatabase
import com.aseelan.qiblayemen.data.local.DataSeeder
import com.aseelan.qiblayemen.data.model.PlaceInfo

class LocationRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.locationDao()
    private val appContext = context.applicationContext

    suspend fun ensureSeeded() {
        DataSeeder.seedIfNeeded(appContext, dao)
    }

    suspend fun getGovernorates(): List<String> = dao.getGovernorates()

    suspend fun getPlacesForGovernorate(governorate: String): List<PlaceInfo> =
        dao.getDistrictsForGovernorate(governorate).map {
            PlaceInfo(it.id, it.governorate, it.district, it.place, it.lat, it.lon)
        }

    suspend fun getById(id: Int): PlaceInfo? =
        dao.getById(id)?.let { PlaceInfo(it.id, it.governorate, it.district, it.place, it.lat, it.lon) }
}
