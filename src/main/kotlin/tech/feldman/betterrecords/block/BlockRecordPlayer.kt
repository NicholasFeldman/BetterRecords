/**
 * The MIT License
 *
 * Copyright (c) 2019 Nicholas Feldman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tech.feldman.betterrecords.block

import tech.feldman.betterrecords.api.BetterRecordsAPI
import tech.feldman.betterrecords.api.wire.IRecordWire
import tech.feldman.betterrecords.api.wire.IRecordWireManipulator
import tech.feldman.betterrecords.block.tile.TileRecordPlayer
import tech.feldman.betterrecords.client.render.RenderRecordPlayer
import tech.feldman.betterrecords.helper.ConnectionHelper
import tech.feldman.betterrecords.helper.nbt.getSounds
import tech.feldman.betterrecords.helper.nbt.isRepeatable
import tech.feldman.betterrecords.helper.nbt.isShufflable
import tech.feldman.betterrecords.item.ItemRecord
import tech.feldman.betterrecords.network.PacketHandler
import tech.feldman.betterrecords.network.PacketRecordPlay
import tech.feldman.betterrecords.network.PacketSoundStop
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.*

class BlockRecordPlayer(name: String) : ModBlock(Material.WOOD, name), TESRProvider<TileRecordPlayer>, ItemModelProvider {

    init {
        setHardness(1f)
        setResistance(5f)
    }

    override fun getTileEntityClass() = TileRecordPlayer::class
    override fun getRenderClass() = RenderRecordPlayer::class


    override fun getBoundingBox(state: IBlockState?, block: IBlockAccess?, pos: BlockPos?): AxisAlignedBB {
        return AxisAlignedBB(.025, 0.0, .025, .975, .975, .975)
    }

    override fun onBlockAdded(world: World?, pos: BlockPos?, state: IBlockState?) {
        super.onBlockAdded(world, pos, state)
        world!!.notifyBlockUpdate(pos!!, state!!, state, 3)
    }

    override fun onBlockActivated(world: World?, pos: BlockPos?, state: IBlockState?, player: EntityPlayer, hand: EnumHand?, side: EnumFacing?, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        if (player.heldItemMainhand.isEmpty && player.heldItemMainhand.item is IRecordWireManipulator) return false
        val tileEntity = world!!.getTileEntity(pos!!)
        if (tileEntity == null || tileEntity !is TileRecordPlayer) return false
        val tileRecordPlayer = tileEntity as TileRecordPlayer?
        if (player.isSneaking) {
            if (world.getBlockState(pos.add(0, 1, 0)).block === Blocks.AIR) {
                if (!world.isRemote) {
                    tileRecordPlayer!!.opening = !tileRecordPlayer.opening
                }
                world.notifyBlockUpdate(pos, state!!, state, 3)
                if (tileRecordPlayer!!.opening)
                    world.playSound(pos.x.toDouble(), pos.y.toDouble() + 0.5, pos.z.toDouble(), SoundEvent.REGISTRY.getObject(ResourceLocation("block.chest.open")), SoundCategory.NEUTRAL, 0.2f, world.rand.nextFloat() * 0.2f + 3f, false)
                else
                    world.playSound(pos.x.toDouble(), pos.y.toDouble() + 0.5, pos.z.toDouble(), SoundEvent.REGISTRY.getObject(ResourceLocation("block.chest.open")), SoundCategory.NEUTRAL, 0.2f, world.rand.nextFloat() * 0.2f + 3f, false)
            }
        } else if (tileRecordPlayer!!.opening) {
            if (tileRecordPlayer.record.isEmpty) {
                if (player.heldItemMainhand.item is ItemRecord && getSounds(player.heldItemMainhand).isNotEmpty()) {
                    tileRecordPlayer.record = player.heldItemMainhand
                    world.notifyBlockUpdate(pos, state!!, state, 3)
                    player.heldItemMainhand.count--

                    if (!world.isRemote) {
                        PacketHandler.sendToAll(PacketRecordPlay(
                                tileRecordPlayer.pos,
                                tileRecordPlayer.world.provider.dimension,
                                tileRecordPlayer.songRadius,
                                isRepeatable(tileEntity.record),
                                isShufflable(tileEntity.record),
                                tileEntity.record
                        ))
                    }
                }
            } else { // There is a record in the player
                if (!world.isRemote) dropItem(world, pos)
                tileRecordPlayer.record = ItemStack.EMPTY
                world.notifyBlockUpdate(pos, state!!, state, 3)
            }
        }
        return true
    }

    public override fun createBlockState(): BlockStateContainer {
        return BlockStateContainer(this, BetterRecordsAPI.CARDINAL_DIRECTIONS)
    }

    override fun getMetaFromState(state: IBlockState?): Int {
        return state!!.getValue(BetterRecordsAPI.CARDINAL_DIRECTIONS).horizontalIndex
    }

    override fun getStateFromMeta(meta: Int): IBlockState {
        return defaultState.withProperty(BetterRecordsAPI.CARDINAL_DIRECTIONS, EnumFacing.getHorizontal(meta))
    }

    override fun onBlockPlacedBy(world: World?, pos: net.minecraft.util.math.BlockPos?, state: IBlockState?, placer: EntityLivingBase?, stack: ItemStack?) {
        world!!.setBlockState(pos!!, state!!.withProperty(BetterRecordsAPI.CARDINAL_DIRECTIONS, placer!!.horizontalFacing.opposite))
    }

    override fun removedByPlayer(state: IBlockState, world: World, pos: BlockPos, player: EntityPlayer, willHarvest: Boolean): Boolean {
        if (world.isRemote) return super.removedByPlayer(state, world, pos, player, willHarvest)
        val te = world.getTileEntity(pos)
        if (te != null && te is IRecordWire) ConnectionHelper.clearConnections(world, te as IRecordWire)
        return super.removedByPlayer(state, world, pos, player, willHarvest)
    }

    override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
        dropItem(world, pos)
        super.breakBlock(world, pos, state)
    }


    private fun dropItem(world: World, pos: BlockPos) {
        val tileEntity = world.getTileEntity(pos)
        if (tileEntity == null || tileEntity !is TileRecordPlayer)
            return

        val item = tileEntity.record

        if (!item.isEmpty) {
            val rand = Random()

            val rx = rand.nextFloat() * 0.8f + 0.1f
            val ry = rand.nextFloat() * 0.8f + 0.1f
            val rz = rand.nextFloat() * 0.8f + 0.1f

            val entityItem = EntityItem(world, (pos.x + rx).toDouble(), (pos.y + ry).toDouble(), (pos.z + rz).toDouble(), ItemStack(item.item, item.count, item.itemDamage))

            if (item.hasTagCompound())
                entityItem.item.tagCompound = item.tagCompound!!.copy()

            entityItem.motionX = rand.nextGaussian() * 0.05f
            entityItem.motionY = rand.nextGaussian() * 0.05f + 0.2f
            entityItem.motionZ = rand.nextGaussian() * 0.05f
            world.spawnEntity(entityItem)
            item.count = 0

            tileEntity.record = ItemStack.EMPTY
            PacketHandler.sendToAll(PacketSoundStop(tileEntity.pos, world.provider.dimension))
        }
    }
}
