package com.codingforcookies.betterrecords.client.sound

import com.codingforcookies.betterrecords.BetterRecords
import com.codingforcookies.betterrecords.api.sound.Sound
import com.codingforcookies.betterrecords.client.handler.ClientRenderHandler
import com.codingforcookies.betterrecords.util.downloadAsync
import com.codingforcookies.betterrecords.util.getVolumeForPlayerFromBlock
import kotlinx.coroutines.experimental.launch
import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.InputStream
import java.net.URL
import javax.sound.sampled.*

object SoundPlayer {

    private val downloadFolder = File(Minecraft.getMinecraft().mcDataDir, "betterrecords/cache")

    private val playingSounds = HashMap<Pair<BlockPos, Int>, Sound>()

    fun playSound(pos: BlockPos, dimension: Int, radius: Float, sound: Sound) {
        BetterRecords.logger.info("Playing sound at $pos in Dimension $dimension")

        ClientRenderHandler.nowDownloading = sound.localName
        ClientRenderHandler.showDownloading = true
        downloadAsync(URL(sound.url), File(downloadFolder, FilenameUtils.getName(sound.url)),
                update = { curr, total ->
                    ClientRenderHandler.downloadPercent = curr / total
                },
                success = {
                    ClientRenderHandler.showDownloading = false
                    playingSounds[Pair(pos, dimension)] = sound
                    ClientRenderHandler.showPlayingWithTimeout(sound.localName)
                    playFile(File(downloadFolder, FilenameUtils.getName(sound.url)), pos, dimension)
                },
                failure = {
                    ClientRenderHandler.showDownloading = false
                }
        )
    }

    fun playSoundFromStream(pos: BlockPos, dimension: Int, radius: Float, sound: Sound) {
        BetterRecords.logger.info("Playing sound from stream at $pos in $dimension")

        val urlConn = IcyURLConnection(URL(if (sound.url.startsWith("http")) sound.url else "http://${sound.url}")).apply {
            instanceFollowRedirects = true
        }

        urlConn.connect()

        playingSounds[Pair(pos, dimension)] = sound

        launch {
            playStream(urlConn.inputStream, pos, dimension)
        }
    }

    fun isSoundPlayingAt(pos: BlockPos, dimension: Int) =
            playingSounds.containsKey(Pair(pos, dimension))

    fun getSoundPlayingAt(pos: BlockPos, dimension: Int) =
            playingSounds[Pair(pos, dimension)]

    fun stopPlayingAt(pos: BlockPos, dimension: Int) {
        BetterRecords.logger.info("Stopping sound at $pos in Dimension $dimension")
        playingSounds.remove(Pair(pos, dimension))
    }

    private fun playFile(file: File, pos: BlockPos, dimension: Int) {
        play(AudioSystem.getAudioInputStream(file), pos, dimension)
    }

    private fun playStream(stream: InputStream, pos: BlockPos, dimension: Int) {
        play(AudioSystem.getAudioInputStream(stream), pos, dimension)
    }

    private fun play(ain: AudioInputStream, pos: BlockPos, dimension: Int) {
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
        rawPlay(decodedFormat, din, pos, dimension)
        ain.close()
    }

    private fun rawPlay(targetFormat: AudioFormat, din: AudioInputStream, pos: BlockPos, dimension: Int) {
        val line = getLine(targetFormat)

        val volumeControl = line.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl

        line.start()

        val buffer = ByteArray(4096)
        var bytes = din.read(buffer)

        while (bytes >= 0 && isSoundPlayingAt(pos, dimension)) {
            volumeControl.value = getVolumeForPlayerFromBlock(pos)
            line.write(buffer, 0, bytes)
            bytes = din.read(buffer)
        }

        stopPlayingAt(pos, dimension)

        line.drain()
        line.stop()
        line.close()
        din.close()
    }

    private fun getLine(audioFormat: AudioFormat): SourceDataLine {
        val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
        val res = AudioSystem.getLine(info)
        res.open()
        return res as SourceDataLine
    }
}
