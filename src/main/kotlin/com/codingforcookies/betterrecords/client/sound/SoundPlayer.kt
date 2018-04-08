package com.codingforcookies.betterrecords.client.sound

import com.codingforcookies.betterrecords.client.handler.ClientRenderHandler
import com.codingforcookies.betterrecords.util.downloadAsync
import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.net.URL
import javax.sound.sampled.*

object SoundPlayer {

    private val downloadFolder = File(Minecraft.getMinecraft().mcDataDir, "betterrecords/cache")

    fun playSound(pos: BlockPos, dimension: Int, radius: Float, sound: Sound) {

        ClientRenderHandler.nowDownloading = sound.local
        ClientRenderHandler.showDownloading = true
        downloadAsync(URL(sound.url), File(downloadFolder, FilenameUtils.getName(sound.url)),
                update = { curr, total ->
                    ClientRenderHandler.downloadPercent = (curr / total).toFloat()
                },
                success = {
                    ClientRenderHandler.showDownloading = false
                    playFile(File(downloadFolder, FilenameUtils.getName(sound.url)))
                },
                failure = {
                    ClientRenderHandler.showDownloading = false
                }
        )
    }

    private fun playFile(file: File) {
        val ain = AudioSystem.getAudioInputStream(file)
        val baseFormat = ain.format
        val decodedFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.sampleRate, 16,
                baseFormat.channels,
                baseFormat.channels * 2,
                baseFormat.sampleRate,
                false
        )

        val din = AudioSystem.getAudioInputStream(decodedFormat, ain)
        rawPlay(decodedFormat, din)
        ain.close()
    }

    private fun rawPlay(targetFormat: AudioFormat, din: AudioInputStream) {
        val line = getLine(targetFormat)

        line?.let {
            line.start()

            val buffer = ByteArray(4096)
            var bytes = din.read(buffer)

            while (bytes >= 0) {
                line.write(buffer, 0, bytes)
                bytes = din.read(buffer)
            }

            line.drain()
            line.stop()
            line.close()
            din.close()
        }
    }

    private fun getLine(audioFormat: AudioFormat): SourceDataLine? {
        val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
        val res = AudioSystem.getLine(info)
        res.open()
        return res as? SourceDataLine
    }
}
