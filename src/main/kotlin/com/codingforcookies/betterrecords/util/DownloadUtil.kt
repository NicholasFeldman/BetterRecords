package com.codingforcookies.betterrecords.util

import com.codingforcookies.betterrecords.ModConfig
import kotlinx.coroutines.experimental.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Downloads a resource asynchronously.
 */
fun downloadAsync(url: URL, target: File,
                  update: (curr: Float, total: Float) -> Unit = { _, _ -> },
                  success: () -> Unit = {},
                  failure: () -> Unit = {}) {

    launch {
        val connection = url.openConnection()
        val size = connection.contentLength.toLong()

        if (size == target.length()) {
            success()
            return@launch
        }

        if (size / 1024 / 1024 > ModConfig.client.downloadMax) {
            failure()
            return@launch
        }

        // Delete the target in case it is broken / A different file in the cache for whatever reason
        target.delete()

        val inputStream = BufferedInputStream(url.openStream())
        val outputStream = FileOutputStream(target)

        var bytesCopied = 0L
        val buffer = ByteArray(1024)
        var bytes = inputStream.read(buffer)
        while (bytes >= 0) {
            outputStream.write(buffer, 0, bytes)
            bytesCopied += bytes
            update(bytesCopied.toFloat(), size.toFloat())
            bytes = inputStream.read(buffer)
        }

        inputStream.close()
        outputStream.close()

        success()
    }
}
