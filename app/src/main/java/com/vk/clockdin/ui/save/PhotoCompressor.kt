// Location: app/src/main/java/com/vk/clockdin/ui/save/PhotoCompressor.kt
package com.vk.clockdin.ui.save

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

/**
 * Handles photo capture setup and downscaling/JPEG compression (~85%, max 1600px)
 * into app-private filesDir/photos/ off the main thread.
 */
object PhotoCompressor {

    private const val MAX_DIMENSION = 1600
    private const val JPEG_QUALITY = 85

    /**
     * Creates a temporary file in cacheDir/camera/ and returns a content URI via FileProvider
     * for Camera capture intent.
     */
    fun createCameraDestinationUri(context: Context): Pair<Uri, File> {
        val cameraDir = File(context.cacheDir, "camera").also { it.mkdirs() }
        val tempFile = File(cameraDir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        return Pair(uri, tempFile)
    }

    /**
     * Reads, rotates (respecting EXIF), downscales (max 1600px edge), and compresses
     * an image from a content Uri into context.filesDir/photos/.
     *
     * @return Absolute file path of the compressed photo, or null on failure.
     */
    suspend fun processAndSavePhoto(
        context: Context,
        sourceUri: Uri
    ): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Decode bounds to compute inSampleSize
            var inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return@withContext null

            var inSampleSize = 1
            val maxEdge = max(srcWidth, srcHeight)
            while ((maxEdge / inSampleSize) > MAX_DIMENSION * 1.5f) {
                inSampleSize *= 2
            }

            // 2. Decode sampled bitmap
            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            inputStream = context.contentResolver.openInputStream(sourceUri)
            val sampledBitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            if (sampledBitmap == null) return@withContext null

            // 3. Inspect EXIF orientation
            var orientation = ExifInterface.ORIENTATION_NORMAL
            try {
                context.contentResolver.openInputStream(sourceUri)?.use { exifStream ->
                    val exif = ExifInterface(exifStream)
                    orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }
            } catch (_: Exception) {}

            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            // 4. Scale down to max 1600px long edge and apply rotation
            val currentMax = max(sampledBitmap.width, sampledBitmap.height)
            val scale = if (currentMax > MAX_DIMENSION) MAX_DIMENSION.toFloat() / currentMax else 1f

            val matrix = Matrix().apply {
                if (scale < 1f) postScale(scale, scale)
                if (rotationDegrees != 0f) postRotate(rotationDegrees)
            }

            val finalBitmap = if (scale < 1f || rotationDegrees != 0f) {
                Bitmap.createBitmap(
                    sampledBitmap,
                    0, 0,
                    sampledBitmap.width,
                    sampledBitmap.height,
                    matrix,
                    true
                ).also {
                    if (it != sampledBitmap) sampledBitmap.recycle()
                }
            } else {
                sampledBitmap
            }

            // 5. Compress to context.filesDir/photos/
            val photosDir = File(context.filesDir, "photos").also { it.mkdirs() }
            val outputFile = File(photosDir, "photo_${System.currentTimeMillis()}.jpg")

            FileOutputStream(outputFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            finalBitmap.recycle()

            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
