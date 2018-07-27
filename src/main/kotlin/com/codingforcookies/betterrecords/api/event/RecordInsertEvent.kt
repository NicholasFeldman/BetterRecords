package com.codingforcookies.betterrecords.api.event

import com.codingforcookies.betterrecords.api.sound.Sound
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.eventhandler.Event

data class RecordInsertEvent(
        val pos: BlockPos,
        val dimension: Int,
        val playRadius: Float,
        val sounds: List<Sound>
) : Event()
