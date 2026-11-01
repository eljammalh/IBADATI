package com.ibadati.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/** Book cover images: resized copies kept in the app's private storage. */
object Images {
    private const val MAX_SIDE = 700

    /** Copies the picked image into [dest] as a JPEG no larger than MAX_SIDE pixels. */
    fun saveResized(context: Context, uri: Uri, dest: File): Boolean = try {
        val bmp = decode(context, uri) ?: throw IllegalStateException("decode")
        val scale = minOf(1f, MAX_SIDE.toFloat() / maxOf(bmp.width, bmp.height))
        val out = if (scale < 1f) {
            Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt().coerceAtLeast(1), (bmp.height * scale).toInt().coerceAtLeast(1), true)
        } else bmp
        FileOutputStream(dest).use { out.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        true
    } catch (e: Exception) {
        false
    }

    private fun decode(context: Context, uri: Uri): Bitmap? {
        if (Build.VERSION.SDK_INT >= 28) {
            val src = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                val big = maxOf(info.size.width, info.size.height)
                if (big > MAX_SIDE * 2) decoder.setTargetSampleSize(big / (MAX_SIDE * 2) + 1)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }
}

/** Loads a cover image off the main thread; null while loading or when missing. */
@Composable
fun rememberCover(file: File?): ImageBitmap? {
    val bmp by produceState<ImageBitmap?>(null, file?.path, file?.lastModified()) {
        value = if (file == null) null else withContext(Dispatchers.IO) {
            try { BitmapFactory.decodeFile(file.path)?.asImageBitmap() } catch (e: Exception) { null }
        }
    }
    return bmp
}
