package com.hemogoblins.betterrecords.block;

import com.hemogoblins.betterrecords.block.tile.TileRecordEtcher;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class BlockRecordEtcher extends Block {

    public BlockRecordEtcher(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileRecordEtcher();
    }
}
