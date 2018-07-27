package com.codingforcookies.betterrecords.item

import com.codingforcookies.betterrecords.api.record.IRecord
import com.codingforcookies.betterrecords.api.wire.IRecordWireHome
import com.codingforcookies.betterrecords.api.sound.Sound
import com.codingforcookies.betterrecords.network.PacketHandler
import com.codingforcookies.betterrecords.network.PacketRecordPlay
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraft.util.text.translation.I18n as ServerI18n

open class ItemRecord(name: String) : ModItem(name), IRecord {

    init {
        maxStackSize = 1
    }

    override fun isRecordValid(itemStack: ItemStack) =
        itemStack.hasTagCompound() && itemStack.tagCompound!!.hasKey("name")

    override fun onRecordInserted(wireHome: IRecordWireHome, itemStack: ItemStack) {
        itemStack.tagCompound?.let {
            PacketHandler.sendToAll(PacketRecordPlay(
                    wireHome.tileEntity.pos,
                    wireHome.tileEntity.world.provider.dimension,
                    wireHome.songRadius,
                    it.getBoolean("repeat"),
                    it.getBoolean("shuffle"),
                    sound = Sound(it.getString("url"), it.getString("local"))
            ))
        }
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        stack.tagCompound?.let {
            tooltip += I18n.format("item.betterercords:record.desc.by", it.getString("author"))
            tooltip += I18n.format("item.betterercords:record.desc.size", it.getInteger("size"))
            if (it.getBoolean("repeat")) {
                tooltip += ""
                tooltip += "\u00a7e" + I18n.format("item.betterercords:record.desc.repeat")
            }
        }
    }

    override fun getItemStackDisplayName(stack: ItemStack): String =
            if (stack.hasTagCompound() && stack.tagCompound!!.hasKey("local")) {
                stack.tagCompound!!.getString("local")
            } else {
                ServerI18n.translateToLocal("$unlocalizedName.name")
            }
}
