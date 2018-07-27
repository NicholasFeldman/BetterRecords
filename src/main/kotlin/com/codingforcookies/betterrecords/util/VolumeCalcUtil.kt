package com.codingforcookies.betterrecords.util

import com.codingforcookies.betterrecords.api.wire.IRecordWireHome
import com.codingforcookies.betterrecords.block.tile.TileSpeaker
import com.codingforcookies.betterrecords.extensions.distanceTo
import net.minecraft.client.Minecraft
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos

fun getVolumeForPlayerFromBlock(pos: BlockPos): Float {
    val player = Minecraft.getMinecraft().player
    val world = Minecraft.getMinecraft().world

    // The record player or Radio.
    // If it isn't one of those, we return a volume of zero.
    val te = world.getTileEntity(pos) as? IRecordWireHome ?: return -80F

    var distance = player.position.distanceTo(pos)

    // If there are any speakers closer to the player, we calculate the volume from that
    te.connections.forEach {
        val pos = BlockPos(it.x2, it.y2, it.z2)

        // If the tile isn't a speaker, we don't care about it
        if (world.getTileEntity(pos) !is TileSpeaker) {
            return@forEach
        }

        val d = player.position.distanceTo(pos)

        // If it's closer to the player, we want to use that distance instead for our volume calculation
        if (d < distance) {
            distance = d
        }
    }

    // Calculate volume from whatever block was selected
    return if (distance > te.songRadius + 10F) {
        // If the player is outside of the radius of the player, we don't want them to hear it
        -80F
    } else {
        // Take into account the user's settings
        val volume = distance * (50F / te.songRadius /
                (Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER) *
                        Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS)))

        if (volume > 80F) {
            -80F
        } else {
            0F - volume
        }
    }
}
