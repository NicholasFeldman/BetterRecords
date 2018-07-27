package com.codingforcookies.betterrecords.item

import com.codingforcookies.betterrecords.ID
import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Mod.EventBusSubscriber(modid = ID)
object ModItems {

    val itemRecord: ModItem = ItemRecord("record")
    val itemMultiRecord: ModItem = ItemMultiRecord("multirecord")
    val itemFrequencyCrystal: ModItem = ItemFrequencyCrystal("frequencycrystal")
    val itemWire: ModItem = ItemWire("wire")
    val itemWireCutters: ModItem = ItemWireCutter("wirecutters")

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        event.registry.registerAll(
                itemRecord,
                itemMultiRecord,
                itemFrequencyCrystal,
                itemWire,
                itemWireCutters
        )
    }
}
