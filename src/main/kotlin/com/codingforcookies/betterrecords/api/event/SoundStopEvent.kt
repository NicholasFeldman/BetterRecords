package com.codingforcookies.betterrecords.api.event

import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.eventhandler.Event

data class SoundStopEvent(
        val pos: BlockPos,
        val dimension: Int
) : Event()
