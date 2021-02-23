package com.hemogoblins.betterrecords.item;

import com.hemogoblins.betterrecords.BetterRecords;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class BetterRecordsCreativeTab extends ItemGroup {

    public static final BetterRecordsCreativeTab INSTANCE = new BetterRecordsCreativeTab();

    public BetterRecordsCreativeTab() {
        super(BetterRecords.MOD_ID);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(Items.MUSIC_DISC_CAT);
    }
}
