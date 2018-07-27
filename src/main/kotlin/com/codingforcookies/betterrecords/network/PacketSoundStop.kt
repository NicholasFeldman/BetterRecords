package com.codingforcookies.betterrecords.network

import com.codingforcookies.betterrecords.api.event.SoundStopEvent
import io.netty.buffer.ByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class PacketSoundStop @JvmOverloads constructor(
        var pos: BlockPos = BlockPos(0, 0, 0),
        var dimension: Int = -1
) : IMessage {

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(pos.x)
        buf.writeInt(pos.y)
        buf.writeInt(pos.z)

        buf.writeInt(dimension)
    }

    override fun fromBytes(buf: ByteBuf) {
        pos = BlockPos(buf.readInt(), buf.readInt(), buf.readInt())

        dimension = buf.readInt()
    }

    class Handler : IMessageHandler<PacketSoundStop, IMessage> {

        override fun onMessage(message: PacketSoundStop, ctx: MessageContext): IMessage? {
            with(message) {
                MinecraftForge.EVENT_BUS.post(SoundStopEvent(pos, dimension))
            }

            return null
        }
    }
}
