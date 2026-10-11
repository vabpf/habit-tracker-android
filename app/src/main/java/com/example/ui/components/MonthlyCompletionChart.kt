package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MonthlyCompletionChart(
    data: List<Pair<String, Int>>, // List of ("Jan", 25), ("Feb", 28), etc.
    habitColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Completions / Month",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(habitColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = habitColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (data.isNotEmpty()) {
                val maxVal = (data.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val points = mutableListOf<Offset>()

                    val stepX = if (data.size > 1) width / (data.size - 1) else width

                    data.forEachIndexed { i, pair ->
                        val x = i * stepX
                        val normalized = pair.second.toFloat() / maxVal
                        val y = height - (normalized * (height - 20.dp.toPx())) - 10.dp.toPx()
                        points.add(Offset(x, y))
                    }

                    // Draw subtle grid lines
                    for (i in 0..3) {
                        val gridY = height * (i / 3f)
                        drawLine(
                            color = Color(0xFF232631),
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Build smooth cubic bezier curve
                    val strokePath = Path()
                    val fillPath = Path()

                    if (points.isNotEmpty()) {
                        strokePath.moveTo(points[0].x, points[0].y)
                        fillPath.moveTo(points[0].x, height)
                        fillPath.lineTo(points[0].x, points[0].y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                            strokePath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p1.x, p1.y
                            )
                            fillPath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p1.x, p1.y
                            )
                        }

                        fillPath.lineTo(points.last().x, height)
                        fillPath.close()

                        // Gradient fill underneath
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    habitColor.copy(alpha = 0.45f),
                                    habitColor.copy(alpha = 0.0f)
                                )
                            )
                        )

                        // Smooth stroke line
                        drawPath(
                            path = strokePath,
                            color = habitColor,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )

                        // Draw point circles
                        points.forEach { pt ->
                            drawCircle(
                                color = habitColor,
                                radius = 3.5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color(0xFF16181F),
                                radius = 2.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // X-axis month labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    data.forEach { (label, _) ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
