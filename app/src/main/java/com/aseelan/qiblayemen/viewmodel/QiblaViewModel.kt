package com.aseelan.qiblayemen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aseelan.qiblayemen.data.model.LocationMode
import com.aseelan.qiblayemen.data.model.PlaceInfo
import com.aseelan.qiblayemen.data.repository.LocationRepository
import com.aseelan.qiblayemen.location.LocationProvider
import com.aseelan.qiblayemen.location.LocationResult
import com.aseelan.qiblayemen.sensor.QiblaSensorManager
import com.aseelan.qiblayemen.util.QiblaCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** حالة تحديد الموقع الحالية المعروضة على الشاشة الرئيسية. */
sealed class LocationStatus {
    object Searching : LocationStatus()
    object PermissionDenied : LocationStatus()
    object NeedsManualFallback : LocationStatus() // فشل GPS/انتهت المهلة -> يعرض خيار الاختيار اليدوي
    object ManualPicker : LocationStatus()         // شاشة اختيار المحافظة/المديرية مفتوحة
    data class Ready(
        val mode: LocationMode,
        val lat: Double,
        val lon: Double,
        val accuracyMeters: Float?,
        val locationLabel: String?
    ) : LocationStatus()
}

data class QiblaUiState(
    val locationStatus: LocationStatus = LocationStatus.Searching,
    val qiblaBearing: Double? = null,
    val distanceKm: Double? = null,
    val compassAzimuth: Float = 0f,
    val compassAccuracy: Int = 0,
    val hasCompassSensors: Boolean = true,
    val governorates: List<String> = emptyList(),
    val selectedGovernorate: String? = null,
    val placesInGovernorate: List<PlaceInfo> = emptyList()
)

class QiblaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocationRepository(application)
    private val locationProvider = LocationProvider(application)
    private val sensorManager = QiblaSensorManager(application)

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(hasCompassSensors = sensorManager.hasRequiredSensors()) }
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        startCompassListening()
    }

    /** يبدأ التسلسل: محاولة GPS أولاً، وعند الفشل/انتهاء المهلة ينتقل تلقائياً لوضع الاختيار اليدوي. */
    fun startLocationSequence() {
        _uiState.update { it.copy(locationStatus = LocationStatus.Searching) }
        viewModelScope.launch {
            when (val result = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    applyLocation(
                        mode = LocationMode.GPS,
                        lat = result.lat,
                        lon = result.lon,
                        accuracy = result.accuracyMeters,
                        label = null
                    )
                }
                LocationResult.PermissionDenied -> {
                    _uiState.update { it.copy(locationStatus = LocationStatus.PermissionDenied) }
                }
                LocationResult.Unavailable -> {
                    _uiState.update { it.copy(locationStatus = LocationStatus.NeedsManualFallback) }
                }
            }
        }
    }

    fun retryGps() = startLocationSequence()

    fun openManualPicker() {
        _uiState.update { it.copy(locationStatus = LocationStatus.ManualPicker) }
        viewModelScope.launch {
            val governorates = repository.getGovernorates()
            _uiState.update { it.copy(governorates = governorates) }
        }
    }

    fun selectGovernorate(governorate: String) {
        _uiState.update { it.copy(selectedGovernorate = governorate) }
        viewModelScope.launch {
            val places = repository.getPlacesForGovernorate(governorate)
            _uiState.update { it.copy(placesInGovernorate = places) }
        }
    }

    /** يعيد المستخدم من قائمة المديريات إلى قائمة المحافظات دون مغادرة وضع الاختيار اليدوي. */
    fun backToGovernorateList() {
        _uiState.update { it.copy(selectedGovernorate = null, placesInGovernorate = emptyList()) }
    }

    fun selectPlace(place: PlaceInfo) {
        applyLocation(
            mode = LocationMode.MANUAL,
            lat = place.lat,
            lon = place.lon,
            accuracy = null,
            label = "${place.governorate} - ${place.district}"
        )
    }

    fun changeLocation() {
        _uiState.update {
            it.copy(
                locationStatus = LocationStatus.Searching,
                selectedGovernorate = null,
                placesInGovernorate = emptyList()
            )
        }
        startLocationSequence()
    }

    private fun applyLocation(mode: LocationMode, lat: Double, lon: Double, accuracy: Float?, label: String?) {
        val bearing = QiblaCalculator.calculateBearing(lat, lon)
        val distance = QiblaCalculator.distanceToKaabaKm(lat, lon)
        _uiState.update {
            it.copy(
                locationStatus = LocationStatus.Ready(mode, lat, lon, accuracy, label),
                qiblaBearing = bearing,
                distanceKm = distance
            )
        }
    }

    private fun startCompassListening() {
        viewModelScope.launch {
            sensorManager.compassFlow().collect { reading ->
                _uiState.update {
                    it.copy(compassAzimuth = reading.azimuthDegrees, compassAccuracy = reading.accuracy)
                }
            }
        }
    }
}
