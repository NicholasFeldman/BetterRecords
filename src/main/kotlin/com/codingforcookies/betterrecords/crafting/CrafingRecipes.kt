package com.codingforcookies.betterrecords.crafting

import com.codingforcookies.betterrecords.ID
import com.codingforcookies.betterrecords.crafting.recipe.*
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Mod.EventBusSubscriber(modid = ID)
object CrafingRecipes {

    @SubscribeEvent
    fun registerRecipes(event: RegistryEvent.Register<IRecipe>) {
        event.registry.registerAll(
                RecipeMultiRecord(),
                RecipeRecordRepeatable(),
                RecipeRecordShuffle(),
                RecipeColoredFreqCrystal(),
                RecipeColoredRecord()
        )
    }
}
