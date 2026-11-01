package com.ibadati.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ibadati.app.MainViewModel
import com.ibadati.app.UiState
import com.ibadati.app.data.Book
import com.ibadati.app.logic.Reading

@Composable
fun ReadingScreen(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    var editing by remember { mutableStateOf<Book?>(null) }
    var adding by remember { mutableStateOf(false) }
    var logging by remember { mutableStateOf<Book?>(null) }
    var deleting by remember { mutableStateOf<Book?>(null) }
    var celebrate by remember { mutableStateOf(false) }
    val books = state.records.books
    val reading = books.filter { it.completedDay == null }.reversed()
    val done = books.filter { it.completedDay != null }.sortedByDescending { it.completedDay }

    ScreenColumn {
        DateNavigator(state.selected, state.today, state.settings.showHijri, state.records::hasAnyData, vm::selectDay)

        Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text(L.addBook)
        }

        if (books.isEmpty()) {
            SectionCard {
                Text(L.noBooks, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        if (reading.isNotEmpty()) {
            SectionTitle(L.currentlyReading)
            reading.forEach { b ->
                BookCard(
                    b, state, vm,
                    onLog = { logging = b },
                    onEdit = { editing = b },
                    onDelete = { deleting = b },
                )
            }
        }
        if (done.isNotEmpty()) {
            SectionTitle(L.completedBooks)
            done.forEach { b ->
                BookCard(
                    b, state, vm,
                    onLog = { logging = b },
                    onEdit = { editing = b },
                    onDelete = { deleting = b },
                )
            }
        }
    }

    if (adding || editing != null) {
        BookEditor(
            vm = vm,
            book = editing,
            onDismiss = { adding = false; editing = null },
        ) { title, author, pages, uri, remove ->
            vm.saveBook(editing, title, author, pages, uri, remove)
            adding = false
            editing = null
        }
    }
    logging?.let { b ->
        val current = state.records.reading[b.id]?.get(state.selected) ?: 0
        val readBefore = state.records.pagesRead(b.id) - current
        NumberPickerDialog(
            title = "${L.pagesToday} · ${b.title}",
            initial = current,
            min = 0,
            max = (b.totalPages - readBefore).coerceAtLeast(current).coerceAtLeast(1),
            onDismiss = { logging = null },
        ) { v ->
            vm.setReading(b.id, v) { celebrate = true }
            logging = null
        }
    }
    deleting?.let { b ->
        AppDialog(
            title = L.deleteConfirm(b.title),
            onDismiss = { deleting = null },
            buttons = {
                TextButton(onClick = { deleting = null }) { Text(L.cancel) }
                TextButton(onClick = {
                    vm.deleteBook(b)
                    deleting = null
                }) { Text(L.delete, color = LocalWorshipColors.current.missed, fontWeight = FontWeight.Bold) }
            },
        ) {
            Text(L.deleteBookText, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (celebrate) {
        AppDialog(
            title = L.bookDone,
            onDismiss = { celebrate = false },
            buttons = { TextButton(onClick = { celebrate = false }) { Text(L.alhamdulillah, fontWeight = FontWeight.Bold) } },
        ) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Sparkles(Modifier.fillMaxWidth().height(120.dp))
                Text("📖", fontSize = 44.sp)
            }
            Text(L.bookDoneText, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun BookCard(b: Book, state: UiState, vm: MainViewModel, onLog: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val wc = LocalWorshipColors.current
    val read = state.records.pagesRead(b.id).coerceAtMost(b.totalPages)
    val frac = read.toDouble() / b.totalPages
    val (pace, finish) = Reading.pace(state.records, b, state.today)
    val todayPages = state.records.reading[b.id]?.get(state.selected) ?: 0
    val completed = b.completedDay != null
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
        border = BorderStroke(1.dp, if (completed) wc.gold.copy(alpha = 0.6f) else cs.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row {
                Cover(b, vm, Modifier.width(78.dp).height(112.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(b.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (b.author.isNotBlank()) {
                        Text(b.author, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 1)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(L.pagesOfTotal(read, b.totalPages), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { Bar(frac, color = if (completed) wc.gold else cs.primary) }
                        Spacer(Modifier.width(8.dp))
                        Text(L.pct(frac), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (completed) wc.gold else cs.primary)
                    }
                    if (completed) {
                        Text(L.finishedOn(L.date(b.completedDay!!)), style = MaterialTheme.typography.bodySmall, color = wc.gold, fontWeight = FontWeight.SemiBold)
                    } else if (pace > 0) {
                        Text(
                            L.pacePerDay(pace) + (finish?.let { " · " + L.finishBy(L.shortDate(it)) } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!completed) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(cs.primaryContainer.copy(alpha = 0.6f))
                            .clickable(onClick = onLog)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "${L.pagesToday}: ${L.n(todayPages)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { vm.setBookCompleted(b, !completed) }) {
                    Text(if (completed) L.markReading else L.markCompleted, style = MaterialTheme.typography.labelMedium)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = L.editBook, tint = cs.onSurfaceVariant) }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = L.delete, tint = cs.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun Cover(b: Book, vm: MainViewModel, modifier: Modifier) {
    val img = rememberCover(vm.coverFile(b.cover))
    val wc = LocalWorshipColors.current
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF0F5E4E), Color(0xFF0A4339)))),
        contentAlignment = Alignment.Center,
    ) {
        if (img != null) {
            Image(img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        } else {
            GeometricPattern(Modifier.matchParentSize(), color = wc.gold, alpha = 0.18f, cell = 26.dp)
            Text(
                b.title.take(1),
                fontFamily = Amiri,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE6C27A),
            )
        }
    }
}

@Composable
private fun BookEditor(
    vm: MainViewModel,
    book: Book?,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Uri?, Boolean) -> Unit,
) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    var title by rememberSaveable { mutableStateOf(book?.title ?: "") }
    var author by rememberSaveable { mutableStateOf(book?.author ?: "") }
    var pages by rememberSaveable { mutableStateOf(book?.totalPages?.toString() ?: "") }
    var picked by remember { mutableStateOf<Uri?>(null) }
    var removed by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var tried by remember { mutableStateOf(false) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { picked = uri; removed = false }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { picked = cameraUri; removed = false }
    }
    val pageCount = parseNumber(pages) ?: 0
    val valid = title.isNotBlank() && pageCount > 0
    val context = LocalContext.current
    val preview = remember(picked) {
        picked?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use {
                    val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = 4 }
                    android.graphics.BitmapFactory.decodeStream(it, null, opts)
                }?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }
    val existing = if (!removed && picked == null) rememberCover(vm.coverFile(book?.cover)) else null

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerLow),
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(if (book == null) L.addBook else L.editBook, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(120) },
                    label = { Text(L.bookTitle) },
                    singleLine = true,
                    isError = tried && title.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it.take(80) },
                    label = { Text(L.bookAuthor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = pages,
                    onValueChange = { v -> pages = v.filter { it.isDigit() }.take(5) },
                    label = { Text(L.bookPages) },
                    singleLine = true,
                    isError = tried && pageCount <= 0,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(L.coverPhoto, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .width(70.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cs.surfaceContainerHigh)
                            .border(1.dp, cs.outlineVariant, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        val img = preview ?: existing
                        if (img != null) Image(img, null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
                        else Text("📖", fontSize = 26.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = {
                            gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) { Text(L.fromGallery) }
                        OutlinedButton(onClick = {
                            val uri = vm.newCameraUri()
                            cameraUri = uri
                            camera.launch(uri)
                        }) { Text(L.fromCamera) }
                        if (preview != null || existing != null) {
                            TextButton(onClick = { picked = null; removed = true }) { Text(L.removePhoto) }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(L.cancel) }
                    TextButton(onClick = {
                        tried = true
                        if (valid) onSave(title, author, pageCount, picked, removed)
                    }) {
                        Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(L.save, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
