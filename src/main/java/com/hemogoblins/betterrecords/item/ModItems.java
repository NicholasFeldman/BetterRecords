package com.hemogoblins.betterrecords.item;

import com.hemogoblins.betterrecords.BetterRecords;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(
        modid = BetterRecords.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ModItems {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
    }

    public static Item.Properties defaultProperties() {
        return new Item.Properties().group(BetterRecordsCreativeTab.INSTANCE);
    }
}
