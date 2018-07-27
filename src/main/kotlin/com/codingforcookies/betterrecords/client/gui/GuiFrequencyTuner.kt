package com.codingforcookies.betterrecords.client.gui

import com.codingforcookies.betterrecords.ID
import com.codingforcookies.betterrecords.block.tile.TileFrequencyTuner
import com.codingforcookies.betterrecords.client.sound.IcyURLConnection
import com.codingforcookies.betterrecords.network.PacketHandler
import com.codingforcookies.betterrecords.network.PacketURLWrite
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.ResourceLocation
import org.apache.commons.io.FilenameUtils
import java.net.MalformedURLException
import java.net.URL

class GuiFrequencyTuner(inventoryPlayer: InventoryPlayer, val tileEntity: TileFrequencyTuner) : GuiContainer(ContainerFrequencyTuner(inventoryPlayer, tileEntity)) {

    val GUI = ResourceLocation(ID, "textures/gui/frequencytuner.png")

    lateinit var nameField: GuiTextField
    lateinit var urlField: GuiTextField

    var checkedURL = false
    var checkURLTime = 0L

    var error = ""

    override fun initGui() {
        super.initGui()
        nameField = GuiTextField(1, fontRenderer, 44, 20, 124, 10)
        urlField = GuiTextField(2, fontRenderer, 44, 35, 124, 10)
        urlField.maxStringLength = 128
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        checkedURL = false
        checkURLTime = System.currentTimeMillis() + 2000

        when {
            nameField.isFocused -> nameField.textboxKeyTyped(typedChar, keyCode)
            urlField.isFocused -> urlField.textboxKeyTyped(typedChar, keyCode)
            else -> super.keyTyped(typedChar, keyCode)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)

        val x = mouseX - (width - xSize) / 2
        val y = mouseY - (height - ySize) / 2

        nameField.mouseClicked(x, y, mouseButton)
        urlField.mouseClicked(x, y, mouseButton)

        if (error == I18n.format("gui.betterrecords.frequencytuner.ready") && x in 44..75 && y in 51..66) {
            val superName = FilenameUtils.getName(urlField.text).split("#", "?")[0]
            PacketHandler.sendToServer(PacketURLWrite(
                    tileEntity.pos,
                    0,
                    superName,
                    urlField.text,
                    nameField.text
            ))
        }
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        with (fontRenderer) {
            drawString(I18n.format("tile.betterrecords:frequencytuner.name"), 8, 6, 4210752)
            drawString(I18n.format("container.inventory"), 8, ySize - 96 + 2, 4210752)
            drawString(I18n.format("gui.betterrecords.label.name") + ": ", 10, 21, 4210752)
            drawString(I18n.format("gui.betterrecords.label.url") + ": ", 10, 36, 4210752)
        }

        val x = mouseX - (width - xSize) / 2
        val y = mouseY - (height - ySize) / 2

        with (fontRenderer) {
            val tuneColor = if (error == I18n.format("gui.betterrecords.frequencytuner.ready")) {
                if (x in 44..75 && y in 51..66) {
                    0xFFFF55
                } else {
                    0xFFFFFF
                }
            } else {
                0x555555
            }

            val errorColor = if (error == I18n.format("gui.betterrecords.frequencytuner.ready")) 0x229922 else 0x992222

            drawStringWithShadow(I18n.format("gui.betterrecords.frequencytuner.tune"), 48F, 53F, tuneColor)
            drawString(error, 172 - fontRenderer.getStringWidth(error), 65, errorColor)
        }

        nameField.drawTextBox()
        urlField.drawTextBox()

        if (tileEntity.crystal.isEmpty) {
            error = I18n.format("gui.betterrecords.frequencytuner.status.noCrystal")
        } else if (tileEntity.crystal.hasTagCompound() && tileEntity.crystal.tagCompound!!.hasKey("url")) {
            error = I18n.format("gui.betterrecords.frequencytuner.status.notBlank")
        } else if (nameField.text.isEmpty()) {
            error = I18n.format("gui.betterrecords.status.noName")
        } else if (nameField.text.length < 3) {
            error = I18n.format("gui.betterrecords.status.nameTooShort")
        } else if (urlField.text.isEmpty()) {
            error = I18n.format("gui.betterrecords.status.noUrl")
        } else if (!checkedURL) {
            error = I18n.format("gui.betterrecords.status.validating")

            if (checkURLTime < System.currentTimeMillis()) {
                checkURLTime = 0

                try {
                    val connection = IcyURLConnection(URL(urlField.text.replace(" ", "%20"))).apply {
                        instanceFollowRedirects = true
                        requestMethod = "HEAD"
                        connect()
                    }

                    error = if (connection.responseCode == 200) {
                        I18n.format("gui.betterrecords.frequencytuner.ready")
                    } else {
                        I18n.format("gui.betterrecords.status.urlUnavailable")
                    }
                } catch(e: MalformedURLException) {
                    error = I18n.format("gui.betterrecords.frequencytuner.status.invalidUrl")
                }

                checkedURL = true
            }
        }
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        GlStateManager.color(1F, 1F, 1F, 1F)

        val x = (width - xSize) / 2
        val y = (height - ySize) / 2

        mc.renderEngine.bindTexture(GUI)

        drawTexturedModalRect(x, y, 0, 0, xSize, ySize)
        drawTexturedModalRect(x + 43, y + 51, 0, if (error == I18n.format("gui.betterrecords.frequencytuner.ready")) 166 else 178, 33, 12)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        super.drawScreen(mouseX, mouseY, partialTicks)
        renderHoveredToolTip(mouseX, mouseY)
    }
}
