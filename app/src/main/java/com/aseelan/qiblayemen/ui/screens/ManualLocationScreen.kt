package com.aseelan.qiblayemen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aseelan.qiblayemen.data.model.PlaceInfo
import com.aseelan.qiblayemen.ui.theme.DeepGreen
import com.aseelan.qiblayemen.ui.theme.Gold
import com.aseelan.qiblayemen.ui.theme.Ivory
import com.aseelan.qiblayemen.ui.theme.MidGreen

@Composable
fun ManualLocationScreen(
    governorates: List<String>,
    selectedGovernorate: String?,
    placesInGovernorate: List<PlaceInfo>,
    onSelectGovernorate: (String) -> Unit,
    onSelectPlace: (PlaceInfo) -> Unit,
    onBackToGovernorates: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        TopBar(
            title = if (selectedGovernorate == null) "اختر المحافظة" else "اختر المديرية",
            onBack = if (selectedGovernorate == null) onBack else onBackToGovernorates
        )

        Text(
            text = "اختر موقعك من القائمة المحلية لحساب اتجاه القبلة بدقة دون الحاجة لإنترنت أو GPS",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            color = DeepGreen.copy(alpha = 0.75f),
            fontSize = 14.sp
        )

        if (selectedGovernorate.isNullOrEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(governorates) { gov ->
                    GovernorateCard(name = gov, onClick = { onSelectGovernorate(gov) })
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(placesInGovernorate) { place ->
                    PlaceCard(place = place, onClick = { onSelectPlace(place) })
                }
            }
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepGreen)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Gold)
        }
        Text(
            text = title,
            color = Ivory,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun GovernorateCard(name: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MidGreen),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, color = Ivory, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Gold)
        }
    }
}

@Composable
private fun PlaceCard(place: PlaceInfo, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = place.district, color = DeepGreen, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (place.place != place.district) {
                Text(
                    text = place.place,
                    color = DeepGreen.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
