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
package tech.feldman.betterrecords.item

import tech.feldman.betterrecords.api.sound.IColorableSoundHolder
import tech.feldman.betterrecords.api.sound.IRepeatableSoundHolder
import tech.feldman.betterrecords.api.sound.IShufflableSoundHolder
import tech.feldman.betterrecords.api.sound.ISoundHolder
import tech.feldman.betterrecords.helper.nbt.getSounds
import tech.feldman.betterrecords.helper.nbt.isRepeatable
import tech.feldman.betterrecords.helper.nbt.isShufflable
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ItemRecord(name: String) : ModItem(name), ISoundHolder, IRepeatableSoundHolder, IShufflableSoundHolder, IColorableSoundHolder {

    override val maxSounds = 12

    init {
        maxStackSize = 1
    }

    override fun getUnlocalizedName(stack: ItemStack): String {
        val songs = getSounds(stack)

        return when (songs.count()) {
            0 -> "item.betterrecords:record.blank"
        // If there's only one song, the name will be set in [getItemStackDisplayName]
            else -> "item.betterrecords:record.multi"
        }
    }

    override fun getItemStackDisplayName(stack: ItemStack): String {
        val songs = getSounds(stack)

        // If there's only one song on the record, we don't want to localize anything
        // and just use that name instead
        if (songs.count() == 1) {
            return songs.first().name
        }

        // If it has no songs or more than one, we fall back on localizing it.
        return super.getItemStackDisplayName(stack)
    }

    override fun addInformation(stack: ItemStack, world: World?, tooltip: MutableList<String>, flag: ITooltipFlag) {
        val songs = getSounds(stack)

        // We only want to add a tooltip to the record if it is not empty
        if (songs.isNotEmpty()) {

            // Add the song info to the tooltip
            if (songs.size > 1) {
                // If there are multiple songs on the record, we want to display them all in an itemized list
                songs.forEachIndexed { index, sound ->
                    tooltip += I18n.format("item.betterrecords:record.desc.song", index + 1, sound.name)
                }
            } else {
                // If there is only one song on the record, we only want to display the author,
                // Because the name of the song is displayed as the name
                tooltip += I18n.format("item.betterrecords:record.desc.by", songs.first().author)
            }

            // Add the size to the record.
            val size = songs.sumBy { it.size }
            tooltip += I18n.format("item.betterrecords:record.desc.size", size)

            // If it's repeatable, we show that.
            if (isRepeatable(stack)) {
                tooltip += "\u00a7e" + I18n.format("item.betterrecords:record.desc.repeatable")
            }

            // If it's shufflable, we show that.
            if (isShufflable(stack)) {
                tooltip += "\u00a7e" + I18n.format("item.betterrecords:record.desc.shufflable")
            }
        }
    }
}
