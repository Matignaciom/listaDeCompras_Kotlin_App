package com.iplacex.listacompras

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductPage(navController: NavController) {
    var productName by remember { mutableStateOf(TextFieldValue("")) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Aquí se registra el lanzador para seleccionar una imagen
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón de volver
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(stringResource(R.string.back))
        }

        // Selector de imagen
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = stringResource(R.string.selected_image_description),
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        launcher.launch("image/*")
                    }
            )
        } ?: run {
            Image(
                painter = painterResource(id = R.drawable.placeholder_image),
                contentDescription = stringResource(R.string.image_placeholder_description),
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        launcher.launch("image/*")
                    }
            )
            Text(stringResource(R.string.select_image))
        }

        TextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text(stringResource(R.string.item_name)) },
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = {
                if (productName.text.isBlank()) {
                    // Mostrar un mensaje de error si el nombre está vacío
                    return@Button
                }

                // Agregar el producto a la base de datos
                val newItem = ShoppingItem(name = productName.text, imageUrl = imageUri?.toString())
                scope.launch {
                    val database = DatabaseBuilder.getInstance(context)
                    database.shoppingItemDao().insertItem(newItem)
                }
                navController.navigate("main") // Regresar a la página principal
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(stringResource(R.string.create))
        }
    }
}