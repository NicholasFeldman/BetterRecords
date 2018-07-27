package com.codingforcookies.betterrecords.extensions

import net.minecraft.util.math.BlockPos

fun BlockPos.distanceTo(pos: BlockPos) =
        Math.sqrt(
                Math.pow((x - pos.x).toDouble(), 2.0)
                        + Math.pow((y - pos.y).toDouble(), 2.0)
                        + Math.pow((z - pos.z).toDouble(), 2.0)
        ).toInt()
