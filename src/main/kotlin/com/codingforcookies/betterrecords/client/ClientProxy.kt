package com.codingforcookies.betterrecords.client

import com.codingforcookies.betterrecords.CommonProxy
import com.codingforcookies.betterrecords.client.handler.ClientRenderHandler
import com.codingforcookies.betterrecords.client.handler.RenderEventHandler
import com.codingforcookies.betterrecords.item.ModItem
import com.codingforcookies.betterrecords.item.ModItems
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.item.Item
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

class ClientProxy : CommonProxy() {

    companion object {
        var encodings = mutableListOf(
                "audio/ogg", "application/ogg",
                "audio/mp3",
                "audio/mpeg", "audio/mpeg; charset=UTF-8",
                "application/octet-stream",
                "audio/wav", "audio/x-wav")
    }

    override fun init(event: FMLInitializationEvent) {
        super.init(event)

        Item.REGISTRY
                .filterIsInstance<ModItem>()
                .forEach(ModItem::registerRender)
    }

    override fun postInit(event: FMLPostInitializationEvent) {
        super.postInit(event)

        val color = IItemColor { stack, tintIndex ->
            if (tintIndex > 0 && stack.hasTagCompound() && stack.tagCompound!!.hasKey("color")) {
                stack.tagCompound!!.getInteger("color")
            } else {
                0xFFFFFF
            }
        }

        Minecraft.getMinecraft().itemColors.run {
            registerItemColorHandler(color, ModItems.itemRecord)
            registerItemColorHandler(color, ModItems.itemMultiRecord)
            registerItemColorHandler(color, ModItems.itemFrequencyCrystal)
        }
    }
}