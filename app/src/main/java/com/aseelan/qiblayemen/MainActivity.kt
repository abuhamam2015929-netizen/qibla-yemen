package com.aseelan.qiblayemen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.aseelan.qiblayemen.ui.screens.ManualLocationScreen
import com.aseelan.qiblayemen.ui.screens.QiblaScreen
import com.aseelan.qiblayemen.ui.theme.QiblaYemenTheme
import com.aseelan.qiblayemen.viewmodel.LocationStatus
import com.aseelan.qiblayemen.viewmodel.QiblaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: QiblaViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // بغض النظر عن نتيجة الإذن، نعيد محاولة التسلسل: GPS إن سُمح، وإلا ينتقل تلقائياً لليدوي
        viewModel.startLocationSequence()
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QiblaYemenTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by viewModel.uiState.collectAsState()

                    LaunchedEffect(Unit) {
                        if (hasLocationPermission()) {
                            viewModel.startLocationSequence()
                        } else {
                            requestLocationPermission()
                        }
                    }

                    when (state.locationStatus) {
                        is LocationStatus.ManualPicker -> {
                            ManualLocationScreen(
                                governorates = state.governorates,
                                selectedGovernorate = state.selectedGovernorate,
                                placesInGovernorate = state.placesInGovernorate,
                                onSelectGovernorate = { gov -> viewModel.selectGovernorate(gov) },
                                onSelectPlace = { place -> viewModel.selectPlace(place) },
                                onBackToGovernorates = { viewModel.backToGovernorateList() },
                                onBack = { viewModel.startLocationSequence() }
                            )
                        }
                        else -> {
                            QiblaScreen(
                                state = state,
                                onRequestPermission = { requestLocationPermission() },
                                onRetryGps = { viewModel.retryGps() },
                                onOpenManualPicker = { viewModel.openManualPicker() },
                                onChangeLocation = { viewModel.changeLocation() }
                            )
                        }
                    }
                }
            }
        }
    }
}
