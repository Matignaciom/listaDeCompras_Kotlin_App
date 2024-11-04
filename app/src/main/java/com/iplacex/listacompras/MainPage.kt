package com.iplacex.listacompras

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import coil.compose.rememberAsyncImagePainter

@Composable
fun MainPage(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var shoppingItems by remember { mutableStateOf(listOf<ShoppingItem>()) }

    // Carga los items desde la base de datos
    LaunchedEffect(Unit) {
        shoppingItems = loadShoppingItems(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (shoppingItems.isEmpty()) {
            EmptyShoppingList(navController)
        } else {
            ShoppingList(
                items = shoppingItems,
                navController = navController,
                context = context,
                scope = scope,
                onItemDeleted = { deletedItem ->
                    // Actualiza la lista localmente
                    shoppingItems = shoppingItems.filterNot { it.id == deletedItem.id }
                },
                onItemPurchased = { purchasedItem ->
                    // Actualiza el estado de compra en la base de datos
                    scope.launch {
                        val database = DatabaseBuilder.getInstance(context)
                        database.shoppingItemDao().updateItem(purchasedItem)
                    }
                    // Actualizar la lista localmente en la UI
                    shoppingItems = shoppingItems.map {
                        if (it.id == purchasedItem.id) purchasedItem else it
                    }
                }
            )
        }

        FloatingActionButton(
            onClick = { navController.navigate("create_product") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("+")
        }
    }
}

@Composable
fun EmptyShoppingList(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(id = R.string.no_items_message))
        Spacer(modifier = Modifier.height(16.dp))
        FloatingActionButton(
            onClick = { navController.navigate("create_product") }
        ) {
            Text("+")
        }
    }
}

@Composable
fun ShoppingList(
    items: List<ShoppingItem>,
    navController: NavController,
    context: Context,
    scope: CoroutineScope,
    onItemDeleted: (ShoppingItem) -> Unit,
    onItemPurchased: (ShoppingItem) -> Unit
) {
    val purchasedItems = items.filter { it.isPurchased }.sortedByDescending { it.id }
    val unpurchasedItems = items.filterNot { it.isPurchased }.sortedBy { it.name }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(unpurchasedItems) { item ->
            ShoppingListItem(
                item = item,
                navController = navController,
                context = context,
                scope = scope,
                onItemDeleted = onItemDeleted,
                onItemPurchased = onItemPurchased
            )
        }

        items(purchasedItems) { item ->
            ShoppingListItem(
                item = item,
                navController = navController,
                context = context,
                scope = scope,
                onItemDeleted = onItemDeleted,
                onItemPurchased = onItemPurchased
            )
        }
    }
}

@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    navController: NavController,
    context: Context,
    scope: CoroutineScope,
    onItemDeleted: (ShoppingItem) -> Unit,
    onItemPurchased: (ShoppingItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { navController.navigate("edit_product/${item.id}") },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            val painter = rememberAsyncImagePainter(item.imageUrl ?: R.drawable.placeholder_image)
            Image(
                painter = painter,
                contentDescription = stringResource(id = R.string.item_name) + ": ${item.name}",
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.name)
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    val updatedItem = item.copy(isPurchased = !item.isPurchased)
                    onItemPurchased(updatedItem)
                }
            ) {
                if (item.isPurchased) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(id = R.string.item_purchased_message),
                        tint = Color(0xFF388E3C)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = stringResource(id = R.string.add_item),
                        tint = Color.Gray
                    )
                }
            }

            IconButton(
                onClick = {
                    scope.launch {
                        val database = DatabaseBuilder.getInstance(context)
                        database.shoppingItemDao().deleteItem(item)
                        onItemDeleted(item)
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.remove_item),
                    tint = Color.Red
                )
            }
        }
    }
}

private suspend fun loadShoppingItems(context: Context): List<ShoppingItem> {
    val database = DatabaseBuilder.getInstance(context)
    return database.shoppingItemDao().getAllItems()
}