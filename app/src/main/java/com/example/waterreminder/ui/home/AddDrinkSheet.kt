package com.example.waterreminder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.data.DrinkType
import com.example.waterreminder.data.UnitSystem
import com.example.waterreminder.ui.components.SectionLabel
import com.example.waterreminder.ui.components.TextInputDialog
import com.example.waterreminder.util.Units
import kotlinx.coroutines.launch

private val VOLUME_PRESETS_ML = listOf(100, 150, 200, 250, 300, 400, 500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDrinkSheet(
    units: UnitSystem,
    onDismiss: () -> Unit,
    onAdd: (type: DrinkType, customName: String?, ml: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var type by remember { mutableStateOf(DrinkType.WATER) }
    var customName by remember { mutableStateOf("") }
    var volumeMl by remember { mutableIntStateOf(200) }
    var isCustomVolume by remember { mutableStateOf(false) }
    var dialog by remember { mutableIntStateOf(0) } // 0 = none, 1 = beverage name, 2 = volume

    val canAdd = volumeMl > 0 && (type != DrinkType.CUSTOM || customName.isNotBlank())

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Add drink",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            SectionLabel("Beverage")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Tile(
                        label = if (customName.isBlank()) "Add custom" else customName,
                        selected = type == DrinkType.CUSTOM,
                        onClick = { dialog = 1 }
                    ) { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(32.dp)) }
                }
                items(DrinkType.selectable) { drink ->
                    Tile(
                        label = drink.label,
                        selected = type == drink,
                        onClick = { type = drink }
                    ) { Text(drink.emoji, fontSize = 28.sp) }
                }
            }

            SectionLabel("Volume")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Tile(
                        label = if (isCustomVolume) Units.formatVolume(volumeMl, units) else "Add custom",
                        selected = isCustomVolume,
                        onClick = { dialog = 2 }
                    ) { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(32.dp)) }
                }
                items(VOLUME_PRESETS_ML) { ml ->
                    Tile(
                        label = Units.formatVolume(ml, units),
                        selected = !isCustomVolume && volumeMl == ml,
                        onClick = {
                            isCustomVolume = false
                            volumeMl = ml
                        }
                    ) { Icon(Icons.Filled.LocalDrink, contentDescription = null, modifier = Modifier.size(32.dp)) }
                }
            }

            Button(
                onClick = {
                    onAdd(type, if (type == DrinkType.CUSTOM) customName else null, volumeMl)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onDismiss()
                    }
                },
                enabled = canAdd,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                    .height(56.dp)
            ) {
                Text("ADD", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.LocalDrink, contentDescription = null)
            }
        }
    }

    when (dialog) {
        1 -> TextInputDialog(
            title = "Beverage name",
            initial = customName,
            keyboardType = KeyboardType.Text,
            onConfirm = { text ->
                if (text.isNotBlank()) {
                    customName = text.trim()
                    type = DrinkType.CUSTOM
                }
                dialog = 0
            },
            onDismiss = { dialog = 0 }
        )
        2 -> TextInputDialog(
            title = "Volume (${Units.volumeUnit(units)})",
            initial = if (isCustomVolume) Units.volumeValue(volumeMl, units).toString() else "",
            keyboardType = KeyboardType.Number,
            onConfirm = { text ->
                text.toIntOrNull()?.takeIf { it > 0 }?.let { value ->
                    volumeMl = Units.toMl(value, units).coerceIn(10, 5000)
                    isCustomVolume = true
                }
                dialog = 0
            },
            onDismiss = { dialog = 0 }
        )
    }
}

@Composable
private fun Tile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val container = if (selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
    val content = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .width(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            icon()
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
