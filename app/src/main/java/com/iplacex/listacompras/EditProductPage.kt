package com.iplacex.listacompras

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductPage(navController: NavController, item: ShoppingItem) {
    var productName by remember { mutableStateOf(TextFieldValue(item.name)) }
    var imageUri by remember { mutableStateOf(item.imageUrl?.let { Uri.parse(it) }) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Aquí se registra el lanzador para seleccionar una imagen
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(id = R.string.back))
        }

        // Esto mostrara la imagen seleccionada o la imagen por defecto
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = stringResource(id = R.string.selected_image_description),
                modifier = Modifier
                    .size(100.dp)
                    .clickable { launcher.launch("image/*") }
            )
        } ?: run {
            Image(
                painter = painterResource(id = R.drawable.placeholder_image),
                contentDescription = stringResource(id = R.string.image_placeholder_description),
                modifier = Modifier
                    .size(100.dp)
                    .clickable { launcher.launch("image/*") }
            )
            Text(stringResource(id = R.string.click_to_select_image))
        }

        TextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text(stringResource(id = R.string.item_name)) }
        )

        Button(
            onClick = {
                if (productName.text.isNotBlank()) {
                    val updatedItem = ShoppingItem(
                        id = item.id,
                        name = productName.text,
                        imageUrl = imageUri?.toString(),
                        isPurchased = item.isPurchased
                    )
                    scope.launch {
                        try {
                            val database = DatabaseBuilder.getInstance(context)
                            database.shoppingItemDao().updateItem(updatedItem)
                            snackbarHostState.showSnackbar("Item updated successfully")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error updating item: ${e.message}")
                        }
                    }
                    navController.popBackStack()
                }
            }
        ) {
            Text(stringResource(id = R.string.save))
        }

        // Snackbar para mostrar mensajes
        SnackbarHost(hostState = snackbarHostState)
    }
}