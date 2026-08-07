package com.aseelan.qiblayemen.data.local

import android.content.Context
import org.json.JSONArray

/**
 * يقرأ ملف assets/yemen_locations.json (الذي يمكن تحديثه لاحقاً عبر GitHub فقط)
 * ويعبّئ قاعدة بيانات Room المحلية عند أول تشغيل للتطبيق، بحيث يعمل البحث
 * عن المحافظات/المديريات بالكامل دون إنترنت.
 */
object DataSeeder {

    private const val ASSET_FILE = "yemen_locations.json"

    suspend fun seedIfNeeded(context: Context, dao: LocationDao) {
        if (dao.count() > 0) return

        val jsonText = context.assets.open(ASSET_FILE).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val array = JSONArray(jsonText)
        val entities = ArrayList<LocationEntity>(array.length())

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            entities.add(
                LocationEntity(
                    id = obj.getInt("id"),
                    governorate = obj.getString("governorate"),
                    district = obj.getString("district"),
                    place = obj.getString("place"),
                    lat = obj.getDouble("lat"),
                    lon = obj.getDouble("lon"),
                    qiblaBearing = obj.getDouble("qibla_bearing")
                )
            )
        }
        dao.insertAll(entities)
    }
}
