package com.aseelan.qiblayemen.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

sealed class LocationResult {
    data class Success(val lat: Double, val lon: Double, val accuracyMeters: Float) : LocationResult()
    object PermissionDenied : LocationResult()
    object Unavailable : LocationResult() // GPS/الشبكة غير متاحة أو انتهت المهلة
}

/**
 * يحاول الحصول على الموقع الحالي عبر GPS خلال مهلة قصيرة (Timeout).
 * إن فشل أو انتهت المهلة (مثال: داخل غرفة فندق مغلقة، أو GPS ضعيف) تُرجع Unavailable
 * ليتحول التطبيق تلقائياً إلى وضع الاختيار اليدوي من قاعدة البيانات المحلية.
 */
class LocationProvider(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun isLocationEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(timeoutMillis: Long = 7000L): LocationResult {
        if (!hasPermission()) return LocationResult.PermissionDenied
        if (!isLocationEnabled()) return LocationResult.Unavailable

        return try {
            withTimeout(timeoutMillis) {
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setDurationMillis(timeoutMillis)
                    .build()

                val location = fusedClient.getCurrentLocation(request, null).await()
                if (location != null) {
                    LocationResult.Success(location.latitude, location.longitude, location.accuracy)
                } else {
                    LocationResult.Unavailable
                }
            }
        } catch (e: TimeoutCancellationException) {
            LocationResult.Unavailable
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LocationResult.Unavailable
        }
    }
}
