package com.codingforcookies.betterrecords.client.sound

import com.codingforcookies.betterrecords.api.sound.Sound
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import net.minecraft.util.math.BlockPos

object SoundManager {

    private val jobs = hashMapOf<Pair<BlockPos, Int>, Job>()

    fun queueSongsAt(pos: BlockPos, dimension: Int, sounds: List<Sound>, shuffle: Boolean = false, repeat: Boolean = false) {
        val job = launch {
            while(true) {
                (if (shuffle) {
                    sounds.shuffled()
                } else {
                    sounds
                }).forEach {
                    SoundPlayer.playSound(pos, dimension, it)

                    if (!repeat) {
                        return@launch
                    }
                }
            }
        }

        jobs[Pair(pos, dimension)] = job
    }

    fun queueStreamAt(pos: BlockPos, dimension: Int, sound: Sound) {
        val job = launch {
            SoundPlayer.playSoundFromStream(pos, dimension, sound)
        }

        jobs[Pair(pos, dimension)] = job
    }

    fun stopQueueAt(pos: BlockPos, dimension: Int) {
        jobs.remove(Pair(pos, dimension))
    }
}
