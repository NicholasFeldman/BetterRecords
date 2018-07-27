package com.codingforcookies.betterrecords.block.tile

import com.codingforcookies.betterrecords.ModConfig
import com.codingforcookies.betterrecords.api.connection.RecordConnection
import com.codingforcookies.betterrecords.api.wire.IRecordWire
import com.codingforcookies.betterrecords.api.wire.IRecordWireHome
import net.minecraft.util.ITickable
import java.util.*

abstract class SimpleRecordWireHome : ModTile(), IRecordWireHome, ITickable {

    var formTreble = ArrayList<Float>()
    var formBass = ArrayList<Float>()

    @Synchronized override fun addTreble(form: Float) {
        formTreble.add(form)
    }

    @Synchronized override fun addBass(form: Float) {
        formBass.add(form)
    }

    override var connections = mutableListOf<RecordConnection>()

    override var wireSystemInfo = HashMap<String, Int>()

    internal var playRadius = 0f

    override fun increaseAmount(wireComponent: IRecordWire) {
        val key = wireComponent.getName()
        val increase = if (wireSystemInfo.containsKey(key)) wireSystemInfo[key]!! + 1 else 1
        wireSystemInfo.put(key, increase)
        playRadius += wireComponent.songRadiusIncrease
    }

    override fun decreaseAmount(wireComponent: IRecordWire) {
        val key = wireComponent.getName()
        wireSystemInfo[key]?.let {
            wireSystemInfo.put(key, it - 1)
            if (it <= 0) {
                wireSystemInfo.remove(key)
            }
            playRadius -= wireComponent.songRadiusIncrease
        }
    }

    override fun update() {
        if (world.isRemote) {
            while (formTreble.size > 2500) {
                for (i in 0..24) {
                    formTreble.removeAt(0)
                    formBass.removeAt(0)
                }
            }
        }
    }

    abstract val songRadiusIncrease: Float

    override val songRadius: Float
        get() {
            val radius = songRadiusIncrease + playRadius
            val maxRadius = ModConfig.maxSpeakerRadius
            return if (radius <= maxRadius || maxRadius == -1) radius else maxRadius.toFloat()
        }

    override val tileEntity = this
}