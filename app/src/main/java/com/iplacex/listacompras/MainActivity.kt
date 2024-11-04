package com.iplacex.listacompras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iplacex.listacompras.ui.theme.ListaComprasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListaComprasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("main") { MainPage(navController) }
        composable("create_product") { CreateProductPage(navController) }
        composable("edit_product/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()

            // Estado para almacenar el ítem
            var item by remember { mutableStateOf<ShoppingItem?>(null) }
            val context = LocalContext.current

            // Usar LaunchedEffect para realizar la llamada a la base de datos
            LaunchedEffect(itemId) {
                itemId?.let { id ->
                    val database = DatabaseBuilder.getInstance(context)
                    item = database.shoppingItemDao().getItemById(id) // Llamada suspendida
                }
            }

            // Verifica si el ítem fue cargado
            item?.let { loadedItem ->
                EditProductPage(navController, loadedItem)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainPage() {
    ListaComprasTheme {
        MainPage(navController = rememberNavController())
    }
}