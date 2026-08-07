package com.aseelan.qiblayemen.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * تمثل مديرية أو مركز سكني داخل قاعدة البيانات المحلية (تعمل بدون إنترنت).
 * qiblaBearing محسوبة مسبقاً بمعادلة الدائرة العظمى (Great Circle) وقت بناء البيانات،
 * وتُستخدم كقيمة احتياطية إضافة إلى الحساب الحي داخل التطبيق.
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: Int,
    val governorate: String,
    val district: String,
    val place: String,
    val lat: Double,
    val lon: Double,
    val qiblaBearing: Double
)
