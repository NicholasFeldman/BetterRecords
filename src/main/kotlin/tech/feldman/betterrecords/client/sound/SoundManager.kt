/**
 * The MIT License
 *
 * Copyright (c) 2019 Nicholas Feldman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tech.feldman.betterrecords.client.sound

import tech.feldman.betterrecords.api.sound.Sound
import net.minecraft.util.math.BlockPos
import kotlin.concurrent.thread

object SoundManager {

    private val jobs = hashMapOf<Pair<BlockPos, Int>, Thread>()

    fun queueSongsAt(pos: BlockPos, dimension: Int, sounds: List<Sound>, shuffle: Boolean = false, repeat: Boolean = false) {
        val job = thread {
            while (true) {
                sounds.forEach {
                    SoundPlayer.playSound(pos, dimension, it)
                }

                if (!repeat) {
                    return@thread
                }
            }
        }

        jobs[Pair(pos, dimension)] = job
    }

    fun queueStreamAt(pos: BlockPos, dimension: Int, sound: Sound) {
        val jobkey = Pair(pos, dimension)
        if (jobkey in jobs) return
        val job = thread {
            SoundPlayer.playSoundFromStream(pos, dimension, sound)
        }

        jobs[Pair(pos, dimension)] = job
    }

    fun stopQueueAt(pos: BlockPos, dimension: Int) {
        val jobkey = Pair(pos, dimension)
        if (jobkey !in jobs) return
        SoundPlayer.stopPlayingAt(pos, dimension)
        jobs[jobkey]?.stop()
        jobs.remove(jobkey)
    }

    fun stopSongs() {
        with(jobs.iterator()) {
            forEach() {
                (jobkey, thread) ->
                        val pos = jobkey.first
                        val dimension = jobkey.second
                        stopQueueAt(pos, dimension)
            }
        }
    }
}
