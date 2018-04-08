package com.codingforcookies.betterrecords.network

import com.codingforcookies.betterrecords.client.sound.Sound
import com.codingforcookies.betterrecords.client.sound.SoundHandler
import com.codingforcookies.betterrecords.client.sound.SoundPlayer
import com.codingforcookies.betterrecords.extensions.forEachTag
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class PacketRecordPlay @JvmOverloads constructor(
        var pos: BlockPos = BlockPos(0, 0, 0),
        var dimension: Int = -1,
        var playRadius: Float = -1F,
        var repeat: Boolean = false,
        var shuffle: Boolean = false,
        // Following arguments used to construct sounds.
        // One of the two should be provided.
        // or both if you want. I'm a comment, not a cop.
        var sound: Sound = Sound(0,0,0, -1, -1F),
        var nbt: NBTTagCompound = NBTTagCompound()
) : IMessage {


    val sounds = mutableListOf<Sound>()

    init {
        if (sound.playRadius != -1F) {
            sounds += sound
        }

        nbt.getTagList("songs", 10).forEachTag {
            sounds += Sound().setInfo(
                    it.getString("name"),
                    it.getString("url"),
                    it.getString("local")
            )
        }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(pos.x)
        buf.writeInt(pos.y)
        buf.writeInt(pos.z)

        buf.writeInt(dimension)

        buf.writeFloat(playRadius)

        // Write the amount of sounds we're going to send,
        // to rebuild on the other side.
        buf.writeInt(sounds.size)
        sounds.forEach {
            ByteBufUtils.writeUTF8String(buf, it.toString())
        }

        buf.writeBoolean(repeat)
        buf.writeBoolean(shuffle)
    }

    override fun fromBytes(buf: ByteBuf) {
        pos = BlockPos(buf.readInt(), buf.readInt(), buf.readInt())

        dimension = buf.readInt()

        playRadius = buf.readFloat()

        val amount = buf.readInt()
        for (i in 1..amount) {
            sounds.add(Sound.fromString(ByteBufUtils.readUTF8String(buf)).apply {
                this.x = this@PacketRecordPlay.pos.x
                this.y = this@PacketRecordPlay.pos.y
                this.z = this@PacketRecordPlay.pos.z
                this.dimension = this@PacketRecordPlay.dimension
                this.playRadius = this@PacketRecordPlay.playRadius
            })
        }

        repeat = buf.readBoolean()
        shuffle = buf.readBoolean()
    }

    class Handler : IMessageHandler<PacketRecordPlay, IMessage> {

        override fun onMessage(message: PacketRecordPlay, ctx: MessageContext): IMessage? {
            val player = Minecraft.getMinecraft().player

            with(message) {
                if (playRadius > 100000 || Math.abs(Math.sqrt(Math.pow(player.posX - pos.x, 2.0) + Math.pow(player.posY - pos.y, 2.0) + Math.pow(player.posZ - pos.z, 2.0))).toFloat() < playRadius) {
                    SoundPlayer.playSound(pos, dimension, playRadius, sounds.first())
                }
            }

            return null
        }

    }
}
