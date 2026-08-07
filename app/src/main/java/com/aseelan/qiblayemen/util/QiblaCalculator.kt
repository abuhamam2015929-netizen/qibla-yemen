package com.aseelan.qiblayemen.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.asin

/**
 * حساب اتجاه القبلة بدقة عالية باستخدام معادلة الدائرة العظمى (Great Circle Bearing).
 * هذه هي الطريقة الفلكية المعتمدة عالمياً لحساب اتجاه القبلة (وليست خط مستقيم على خريطة مسطحة).
 *
 * إحداثيات الكعبة المشرفة الدقيقة:
 * 21.4225° شمالاً، 39.8262° شرقاً
 */
object QiblaCalculator {

    const val KAABA_LAT = 21.4225
    const val KAABA_LON = 39.8262

    /**
     * يُرجع زاوية اتجاه القبلة بالدرجات (0–360) من الشمال الحقيقي (True North)،
     * بالاتجاه الموافق لعقارب الساعة، انطلاقاً من نقطة (lat, lon) المُعطاة.
     */
    fun calculateBearing(lat: Double, lon: Double): Double {
        val lat1 = Math.toRadians(lat)
        val lat2 = Math.toRadians(KAABA_LAT)
        val deltaLon = Math.toRadians(KAABA_LON - lon)

        val x = sin(deltaLon) * cos(lat2)
        val y = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)

        val bearingRad = atan2(x, y)
        var bearingDeg = Math.toDegrees(bearingRad)
        bearingDeg = (bearingDeg + 360) % 360
        return bearingDeg
    }

    /**
     * المسافة بالكيلومترات إلى الكعبة (معادلة Haversine) - تُعرض كمعلومة إضافية للمستخدم.
     */
    fun distanceToKaabaKm(lat: Double, lon: Double): Double {
        val earthRadiusKm = 6371.0
        val lat1 = Math.toRadians(lat)
        val lat2 = Math.toRadians(KAABA_LAT)
        val dLat = Math.toRadians(KAABA_LAT - lat)
        val dLon = Math.toRadians(KAABA_LON - lon)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * asin(sqrt(a))
        return earthRadiusKm * c
    }

    /**
     * أقرب موقع من قائمة نقاط (المحافظات/المديريات) لإحداثيات معينة - Haversine بسيط.
     * تُستخدم لاختيار "أقرب نقطة مطابقة" عند الوضع اليدوي إن لزم لاحقاً.
     */
    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * asin(sqrt(a))
        return earthRadiusKm * c
    }
}
