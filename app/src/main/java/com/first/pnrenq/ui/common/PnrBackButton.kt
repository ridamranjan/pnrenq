
package com.first.pnrenq.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import com.first.pnrenq.R

@Composable
fun PnrBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(
                R.drawable.ic_pnr_back
            ),
            contentDescription = "Navigate back",
            tint = LocalContentColor.current,
            modifier = Modifier.size(24.dp)
        )
    }
}
