package com.codingforcookies.betterrecords.block

import com.codingforcookies.betterrecords.ID
import com.codingforcookies.betterrecords.block.itemblock.ItemBlockSpeaker
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Mod.EventBusSubscriber(modid = ID)
object ModBlocks {

    val blockRecordEtcher = BlockRecordEtcher("recordetcher")
    val blockRecordPlayer = BlockRecordPlayer("recordplayer")
    val blockFrequencyTuner = BlockFrequencyTuner("frequencytuner")
    val blockRadio = BlockRadio("radio")
    val blockSpeaker = BlockSpeaker("speaker")
    val blockStrobeLight = BlockStrobeLight("strobelight")
    val blockLaser = BlockLaser("laser")
    val blockLaserCluster = BlockLaserCluster("lasercluster")

    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        event.registry.registerAll(
                blockRecordEtcher,
                blockRecordPlayer,
                blockFrequencyTuner,
                blockRadio,
                blockSpeaker,
                blockStrobeLight,
                blockLaser,
                blockLaserCluster
        )
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        arrayOf<ModBlock>(
                blockRecordEtcher,
                blockRecordPlayer,
                blockFrequencyTuner,
                blockRadio,
                blockStrobeLight,
                blockLaser,
                blockLaserCluster
        ).map {
            ItemBlock(it).setRegistryName(it.registryName)
        }.forEach(event.registry::register)

        // Register the speaker specially since it has variants
        event.registry.register(ItemBlockSpeaker(blockSpeaker).setRegistryName(blockSpeaker.registryName))

        Block.REGISTRY
                .filterIsInstance<ModBlock>()
                .forEach {
                    if (it is TileEntityProvider<*>) {
                        it.registerTileEntity(it)
                    }
                }
    }

    @SubscribeEvent
    fun registerModels(event: ModelRegistryEvent) {
        Block.REGISTRY
                .filterIsInstance<ModBlock>()
                .forEach {
                    it.setStateMapper()

                    if (it is ItemModelProvider) {
                        it.registerItemModel(it)
                    }

                    if (it is TESRProvider<*>) {
                        it.bindTESR()
                        it.registerTESRItemStacks(it)
                    }
                }
    }
}
