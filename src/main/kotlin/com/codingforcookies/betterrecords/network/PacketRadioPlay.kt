package com.codingforcookies.betterrecords.network

import com.codingforcookies.betterrecords.api.event.RadioInsertEvent
import com.codingforcookies.betterrecords.api.event.RecordInsertEvent
import com.codingforcookies.betterrecords.api.sound.Sound
import io.netty.buffer.ByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class PacketRadioPlay @JvmOverloads constructor(
        var pos: BlockPos = BlockPos(0, 0, 0),
        var dimension: Int = -1,
        var playRadius: Float = -1F,
        var local: String = "",
        var url: String = ""
) : IMessage {

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(pos.x)
        buf.writeInt(pos.y)
        buf.writeInt(pos.z)
        buf.writeInt(dimension)

        buf.writeFloat(playRadius)

        ByteBufUtils.writeUTF8String(buf, local)
        ByteBufUtils.writeUTF8String(buf, url)
    }

    override fun fromBytes(buf: ByteBuf) {
        pos = BlockPos(buf.readInt(), buf.readInt(), buf.readInt())
        dimension = buf.readInt()

        playRadius = buf.readFloat()

        local = ByteBufUtils.readUTF8String(buf)
        url = ByteBufUtils.readUTF8String(buf)
    }

    class Handler : IMessageHandler<PacketRadioPlay, IMessage> {

        override fun onMessage(message: PacketRadioPlay, ctx: MessageContext): IMessage? {
            with(message) {
                MinecraftForge.EVENT_BUS.post(RadioInsertEvent(pos, dimension, playRadius, Sound(url, local)))
            }

            return null
        }
    }
}
