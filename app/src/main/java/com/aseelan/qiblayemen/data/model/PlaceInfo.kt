package com.aseelan.qiblayemen.data.model

/** تمثيل مبسّط لموقع (مديرية/مركز) يُعرض في واجهة الاختيار اليدوي ونتيجة القبلة. */
data class PlaceInfo(
    val id: Int,
    val governorate: String,
    val district: String,
    val place: String,
    val lat: Double,
    val lon: Double
)

enum class LocationMode {
    GPS,
    MANUAL
}
