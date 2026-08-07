package com.aseelan.qiblayemen.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aseelan.qiblayemen.ui.theme.DeepGreen
import com.aseelan.qiblayemen.ui.theme.Gold
import com.aseelan.qiblayemen.ui.theme.GoldLight
import com.aseelan.qiblayemen.ui.theme.Ivory
import com.aseelan.qiblayemen.ui.theme.MidGreen
import kotlin.math.cos
import kotlin.math.sin

/**
 * قرص بوصلة القبلة: يدور مؤشر ذهبي نحو اتجاه الكعبة بناءً على الفرق بين
 * اتجاه القبلة الفعلي (qiblaBearing) واتجاه الجهاز الحالي (deviceAzimuth).
 */
@Composable
fun CompassDial(
    qiblaBearing: Double,
    deviceAzimuth: Float,
    modifier: Modifier = Modifier
) {
    // زاوية دوران القرص بحيث يشير المؤشر الذهبي دوماً نحو القبلة الحقيقية
    val targetRotation = ((qiblaBearing - deviceAzimuth + 360) % 360).toFloat()
    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "compassRotation"
    )
    // زاوية دوران قرص الاتجاهات (N/E/S/W) بحيث الشمال الحقيقي يبقى ثابتاً بالنسبة للمستخدم
    val dialRotation by animateFloatAsState(
        targetValue = -deviceAzimuth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "dialRotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // القرص الخارجي: التدرجات والاتجاهات، يدور مع حركة الجهاز
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .rotate(dialRotation)
        ) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MidGreen, DeepGreen),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Gold,
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
            )
            drawCircle(
                color = GoldLight.copy(alpha = 0.5f),
                radius = radius - 14.dp.toPx(),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )

            // علامات كل 30 درجة + الحروف الأساسية N E S W
            val directions = listOf("N" to 0, "E" to 90, "S" to 180, "W" to 270)
            for (deg in 0 until 360 step 15) {
                val angleRad = Math.toRadians((deg - 90).toDouble())
                val isMajor = deg % 90 == 0
                val outerR = radius - 10.dp.toPx()
                val innerR = if (isMajor) radius - 26.dp.toPx() else radius - 18.dp.toPx()
                val startX = center.x + (outerR * cos(angleRad)).toFloat()
                val startY = center.y + (outerR * sin(angleRad)).toFloat()
                val endX = center.x + (innerR * cos(angleRad)).toFloat()
                val endY = center.y + (innerR * sin(angleRad)).toFloat()
                drawLine(
                    color = if (isMajor) Gold else GoldLight.copy(alpha = 0.6f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                )
            }

            directions.forEach { (label, deg) ->
                val angleRad = Math.toRadians((deg - 90).toDouble())
                val labelR = radius - 42.dp.toPx()
                val x = center.x + (labelR * cos(angleRad)).toFloat()
                val y = center.y + (labelR * sin(angleRad)).toFloat()
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = if (label == "N") android.graphics.Color.parseColor("#B23A2F")
                                else android.graphics.Color.parseColor("#F0D98C")
                        textSize = 15.sp.toPx()
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(label, x, y + 6.dp.toPx(), paint)
                }
            }
        }

        // المؤشر الذهبي: يدور مباشرة نحو اتجاه القبلة الفعلي
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .rotate(animatedRotation),
            contentAlignment = Alignment.TopCenter
        ) {
            QiblaNeedle(modifier = Modifier.padding(top = 18.dp))
        }

        // شارة الكعبة في المنتصف
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(Gold, CircleShape)
                .border(2.dp, Ivory, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🕋", fontSize = 26.sp)
        }
    }
}

@Composable
private fun QiblaNeedle(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 34.dp, height = 90.dp)) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w / 2f, 0f)
            lineTo(w, h * 0.55f)
            lineTo(w / 2f, h * 0.4f)
            lineTo(0f, h * 0.55f)
            close()
        }
        drawPath(path, color = Gold)
        drawPath(
            path,
            color = DeepGreen,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
        )
    }
}
