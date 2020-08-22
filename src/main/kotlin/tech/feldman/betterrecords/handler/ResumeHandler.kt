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
package tech.feldman.betterrecords.handler

import tech.feldman.betterrecords.BetterRecords
import tech.feldman.betterrecords.helper.nbt.getSounds
import tech.feldman.betterrecords.api.event.RadioInsertEvent
import tech.feldman.betterrecords.api.event.SoundStopEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.entity.player.EntityPlayerMP
import tech.feldman.betterrecords.block.tile.TileRadio
import tech.feldman.betterrecords.network.PacketHandler
import tech.feldman.betterrecords.network.PacketRadioPlay
import tech.feldman.betterrecords.network.PacketSoundStop
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent

open class ResumeHandler {
    private val streams = mutableMapOf<EntityPlayerMP, MutableList<Pair<BlockPos, Int>>>()

    @SubscribeEvent
    fun onPlayerMoved(event: LivingUpdateEvent) {
        if (event.entity !is EntityPlayerMP) return;
        if (event.entity.world.isRemote) return;
        val world = event.entity.getEntityWorld()
        val tiles = world.loadedTileEntityList
        val player = event.entity as EntityPlayerMP
        // race condition where event goes out before
        // client side soundmanager is ready
        if (player.ticksExisted < 40 ) return;
        tiles.forEach {
            if ( it is TileRadio ) {
                val radioDistance = event.entity.getDistance(it.pos.x.toDouble(),
                                                             it.pos.y.toDouble(),
                                                             it.pos.z.toDouble())
                val streamKey = Pair(it.pos, player.dimension)
                val playerStreams: MutableList<Pair<BlockPos, Int>>? = streams[player]
                if (playerStreams == null) return
                if ( radioDistance > it.songRadius) {
                    if (playerStreams.contains(streamKey)) {
                        BetterRecords.logger.debug("Removing radio stream for ${player.name} at ${it.pos} in ${player.dimension} due to being out of range")
                        val radioPacket = PacketSoundStop(it.pos, player.dimension)
                        PacketHandler.sendToPlayer(radioPacket, player)
                        playerStreams.remove(streamKey)
                        streams[player] = playerStreams
                    }
                } else {
                    if (it.crystal != null) {
                        if (playerStreams.contains(streamKey)) return
                        BetterRecords.logger.debug("Adding radio stream for ${player.name} at ${it.pos} in ${player.dimension}")
                        playerStreams.add(streamKey)
                        streams[player] = playerStreams
                        val crystalSounds = getSounds(it.crystal)
                        if (crystalSounds.size == 0) return
                        val radioSounds = crystalSounds.first()
                        val radioPacket = PacketRadioPlay(
                            it.pos,
                            player.dimension,
                            it.songRadius,
                            radioSounds.name,
                            radioSounds.url)
                        PacketHandler.sendToPlayer(radioPacket,
                                                   player)
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onLoggedIn(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityPlayerMP) return;
        val player = event.entity as EntityPlayerMP
        if (player.world.isRemote) return;
        val playerStreams: MutableList<Pair<BlockPos, Int>>? = streams[player]
        if (playerStreams != null) return;
        streams[player] = mutableListOf<Pair<BlockPos, Int>>()
        BetterRecords.logger.debug("Registered ${player.name} for stream tracking")
    }

    @SubscribeEvent
    fun onLoggedOut(event: PlayerLoggedOutEvent) {
        val player = event.player as EntityPlayerMP
        if (player.world.isRemote) return;
        streams.remove(player)
        BetterRecords.logger.debug("Removed all streams for ${player.name}")
    }

    @SubscribeEvent
    fun onDimensionChange(event: PlayerChangedDimensionEvent) {
        if (event.player.world.isRemote) return;
        val player = event.player as EntityPlayerMP
        val playerStreams: MutableList<Pair<BlockPos, Int>>? = streams[player]
        if (playerStreams == null ) return;
        with (playerStreams.iterator()) {
             forEach() {
                     val pos = it.first
                     val dimension = it.second
                     if (event.fromDim == dimension ) {
                         BetterRecords.logger.debug("Removing radio stream for ${player.name} at ${pos} in ${dimension} due to dimensional shift")
                         val radioPacket = PacketSoundStop(pos, dimension)
                         PacketHandler.sendToPlayer(radioPacket, player)
                         remove()
                     }
             }
        }
        streams[player] = playerStreams
    }
}
