package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAppChrome

/**
 * Fixed-size circular icon button with exact dimensions.
 *
 * Unlike Material3 [androidx.compose.material3.IconButton] — which enforces
 * a 48dp minimum touch target and paints outside a smaller requested size
 * (collapsing the gaps in button clusters) — this renders exactly [size],
 * so [androidx.compose.foundation.layout.Arrangement.spacedBy] gaps are
 * exact.
 */
@Composable
fun CircleIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    containerColor: Color = LocalAppChrome.current.container,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    testTag: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(role = Role.Button, onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
