package com.aseelan.qiblayemen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aseelan.qiblayemen.data.model.LocationMode
import com.aseelan.qiblayemen.ui.components.CompassDial
import com.aseelan.qiblayemen.ui.theme.DeepGreen
import com.aseelan.qiblayemen.ui.theme.Gold
import com.aseelan.qiblayemen.ui.theme.Ivory
import com.aseelan.qiblayemen.ui.theme.MidGreen
import com.aseelan.qiblayemen.viewmodel.LocationStatus
import com.aseelan.qiblayemen.viewmodel.QiblaUiState
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(
    state: QiblaUiState,
    onRequestPermission: () -> Unit,
    onRetryGps: () -> Unit,
    onOpenManualPicker: () -> Unit,
    onChangeLocation: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            AppHeader()
            Spacer(Modifier.height(8.dp))

            when (val status = state.locationStatus) {
                is LocationStatus.Searching -> SearchingContent()
                is LocationStatus.PermissionDenied -> PermissionDeniedContent(
                    onGrant = onRequestPermission,
                    onManual = onOpenManualPicker
                )
                is LocationStatus.NeedsManualFallback -> FallbackContent(
                    onRetry = onRetryGps,
                    onManual = onOpenManualPicker
                )
                is LocationStatus.ManualPicker -> {
                    // تُعرض شاشة الاختيار اليدوي بشكل منفصل من MainActivity
                }
                is LocationStatus.Ready -> ReadyContent(
                    state = state,
                    mode = status.mode,
                    locationLabel = status.locationLabel,
                    accuracyMeters = status.accuracyMeters,
                    onChangeLocation = onChangeLocation
                )
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "قبلة اليمن",
            color = DeepGreen,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "دليلك نحو الكعبة",
            color = DeepGreen.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SearchingContent() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Gold, strokeWidth = 3.dp)
        Spacer(Modifier.height(20.dp))
        Text("جاري تحديد موقعك…", color = DeepGreen, fontSize = 16.sp)
        Text(
            "نحاول تحديد اتجاه القبلة عبر GPS مباشرة",
            color = DeepGreen.copy(alpha = 0.6f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionDeniedContent(onGrant: () -> Unit, onManual: () -> Unit) {
    InfoCard(
        title = "التطبيق يحتاج إذن الموقع",
        description = "لحساب اتجاه القبلة تلقائياً بدقة، امنح التطبيق إذن الوصول للموقع، أو استخدم الاختيار اليدوي من قاعدة بيانات المديريات.",
    ) {
        PrimaryButton(text = "منح الإذن", icon = Icons.Default.MyLocation, onClick = onGrant)
        Spacer(Modifier.height(10.dp))
        SecondaryButton(text = "الاختيار اليدوي", icon = Icons.Default.EditLocation, onClick = onManual)
    }
}

@Composable
private fun FallbackContent(onRetry: () -> Unit, onManual: () -> Unit) {
    InfoCard(
        title = "تعذّر تحديد الموقع تلقائياً",
        description = "لا يتوفر GPS أو إشارة كافية الآن (مثال: داخل مبنى مغلق). يمكنك اختيار محافظتك ومديريتك يدوياً للحصول على اتجاه القبلة بنفس الدقة.",
    ) {
        PrimaryButton(text = "الاختيار اليدوي", icon = Icons.Default.EditLocation, onClick = onManual)
        Spacer(Modifier.height(10.dp))
        SecondaryButton(text = "إعادة محاولة GPS", icon = Icons.Default.Refresh, onClick = onRetry)
    }
}

@Composable
private fun ReadyContent(
    state: QiblaUiState,
    mode: LocationMode,
    locationLabel: String?,
    accuracyMeters: Float?,
    onChangeLocation: () -> Unit
) {
    val bearing = state.qiblaBearing ?: 0.0

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(6.dp))

        if (!state.hasCompassSensors) {
            Text(
                "⚠️ جهازك لا يحتوي حساس بوصلة، لن يعمل المؤشر الحي",
                color = Color(0xFFB23A2F),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        } else if (state.compassAccuracy <= 1) {
            Text(
                "حرّك جهازك على شكل ٨ لمعايرة البوصلة",
                color = DeepGreen.copy(alpha = 0.55f),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        CompassDial(
            qiblaBearing = bearing,
            deviceAzimuth = state.compassAzimuth,
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Text(
            text = "${bearing.roundToInt()}°",
            color = DeepGreen,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "اتجاه القبلة من الشمال الحقيقي",
            color = DeepGreen.copy(alpha = 0.6f),
            fontSize = 13.sp
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatChip(
                label = if (mode == LocationMode.GPS) "عبر GPS" else "اختيار يدوي",
                modifier = Modifier.weight(1f)
            )
            state.distanceKm?.let {
                StatChip(
                    label = "${it.roundToInt()} كم للكعبة",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (locationLabel != null) {
            Spacer(Modifier.height(8.dp))
            Text(locationLabel, color = DeepGreen.copy(alpha = 0.7f), fontSize = 13.sp)
        }

        Spacer(Modifier.height(16.dp))
        SecondaryButton(text = "تغيير الموقع", icon = Icons.Default.EditLocation, onClick = onChangeLocation)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatChip(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MidGreen, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Ivory, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InfoCard(title: String, description: String, actions: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MidGreen)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(title, color = Ivory, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(description, color = Ivory.copy(alpha = 0.85f), fontSize = 13.5.sp, lineHeight = 20.sp)
            Spacer(Modifier.height(18.dp))
            actions()
        }
    }
}

@Composable
private fun PrimaryButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepGreen),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SecondaryButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ivory),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Medium)
    }
}
