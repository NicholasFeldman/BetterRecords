package com.hemogoblins.betterrecords.util;

import com.hemogoblins.betterrecords.BetterRecords;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistryUtil {

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(BetterRecords.MOD_ID, path);
    }

    public static <T extends IForgeRegistryEntry<T>> void register(
            IForgeRegistry<T> registry,
            ResourceLocation resourceLocation,
            IForgeRegistryEntry<T> entry
    ) {
        registry.register(entry.setRegistryName(resourceLocation));
    }

    public static <T extends IForgeRegistryEntry<T>> void register(
            IForgeRegistry<T> registry,
            String name,
            IForgeRegistryEntry<T> entry
    ) {
        register(registry, prefix(name), entry);
    }
}
