package com.codingforcookies.betterrecords.client.handler

import com.codingforcookies.betterrecords.ID
import com.codingforcookies.betterrecords.api.event.RadioInsertEvent
import com.codingforcookies.betterrecords.api.event.RecordInsertEvent
import com.codingforcookies.betterrecords.api.event.SoundStopEvent
import com.codingforcookies.betterrecords.client.sound.SoundManager
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

@Mod.EventBusSubscriber(modid = ID, value = [Side.CLIENT])
object SoundEventHandler {

    @JvmStatic
    @SubscribeEvent
    fun onRecordInserted(event: RecordInsertEvent) {
        val (pos, dimension, playRadius, sounds) = event
        val player = Minecraft.getMinecraft().player

        if (playRadius > 100000 || Math.abs(Math.sqrt(Math.pow(player.posX - pos.x, 2.0) + Math.pow(player.posY - pos.y, 2.0) + Math.pow(player.posZ - pos.z, 2.0))).toFloat() < playRadius) {
            SoundManager.queueSongsAt(pos, dimension, sounds)
        }

    }

    @JvmStatic
    @SubscribeEvent
    fun onRadioInserted(event: RadioInsertEvent) {
        val (pos, dimension, playRadius, sound) = event
        val player = Minecraft.getMinecraft().player

        if (playRadius > 100000 || Math.abs(Math.sqrt(Math.pow(player.posX - pos.x, 2.0) + Math.pow(player.posY - pos.y, 2.0) + Math.pow(player.posZ - pos.z, 2.0))).toFloat() < playRadius) {
            SoundManager.queueStreamAt(pos, dimension, sound)
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun onSoundStopped(event: SoundStopEvent) {
        val (pos, dimension) = event

        SoundManager.stopQueueAt(pos, dimension)
    }
}
