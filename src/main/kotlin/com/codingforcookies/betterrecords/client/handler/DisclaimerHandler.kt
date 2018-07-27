package com.codingforcookies.betterrecords.client.handler

import com.codingforcookies.betterrecords.ID
import com.codingforcookies.betterrecords.ModConfig
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side

/**
 * Handler to warn the player when they log in about flashing lights.
 */
@Mod.EventBusSubscriber(modid = ID, value = [Side.CLIENT])
object DisclaimerHandler {

    /**
     * Send a message to the player warning them if it is their first time joining a world
     *
     * We do this by checking if flashMode is -1.
     * If for any reason they set it back to -1, they will be warned again.
     */
    @SubscribeEvent
    fun onFirstJoin(event: EntityJoinWorldEvent) {
        if (event.entity is EntityPlayer && ModConfig.client.flashMode == -1) {

            val message = TextComponentTranslation("betterrecords.warning").apply {
                style = Style().apply {
                    color = TextFormatting.GREEN
                }
            }
            event.entity.sendMessage(message)

            // Update default flash mode, to show we've already warned them
            ModConfig.client.flashMode = 1
            ModConfig.update()
        }
    }
}
