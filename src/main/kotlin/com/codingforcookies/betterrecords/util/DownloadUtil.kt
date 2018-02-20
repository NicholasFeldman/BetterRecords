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
                  update: (curr: Long, total: Long) -> Unit = { _, _ -> },
                  success: () -> Unit = {},
                  failure: () -> Unit = {}) {

    launch {
        val connection = url.openConnection()

        val size = connection.contentLength.toLong()

        if (size / 1024 / 1024 > ModConfig.client.downloadMax) {
            failure()
            return@launch
        }

        val inputStream = BufferedInputStream(url.openStream())
        val outputStream = FileOutputStream(target)

        var bytesCopied = 0L
        val buffer = ByteArray(1024)
        var bytes = inputStream.read(buffer)
        while (bytes >= 0) {
            outputStream.write(buffer, 0, bytes)
            bytesCopied += bytes
            update(bytesCopied, size)
            bytes = inputStream.read(buffer)
        }

        inputStream.close()
        outputStream.close()

        success()
    }
}
