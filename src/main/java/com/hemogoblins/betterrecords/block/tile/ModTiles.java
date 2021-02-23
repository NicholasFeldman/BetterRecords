package com.hemogoblins.betterrecords.block.tile;

import com.hemogoblins.betterrecords.BetterRecords;
import com.hemogoblins.betterrecords.block.ModBlocks;
import com.hemogoblins.betterrecords.lib.BlockNames;
import com.hemogoblins.betterrecords.util.RegistryUtil;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(
        modid = BetterRecords.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ModTiles {

    public static final TileEntityType<TileRecordPlayer> recordPlayer = TileEntityType.Builder.create(
            TileRecordPlayer::new,
            ModBlocks.recordPlayer
    ).build(null);

    public static final TileEntityType<TileRecordEtcher> recordEtcher = TileEntityType.Builder.create(
            TileRecordEtcher::new,
            ModBlocks.recordEtcher
    ).build(null);

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();

        RegistryUtil.register(registry, BlockNames.RECORD_PLAYER, recordPlayer);
        RegistryUtil.register(registry, BlockNames.RECORD_ETCHER, recordEtcher);
    }
}
