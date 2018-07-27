package com.codingforcookies.betterrecords.extensions

import net.minecraft.client.renderer.GlStateManager

inline fun glMatrix(block: () -> Unit) {
    GlStateManager.pushMatrix()
    block()
    GlStateManager.popMatrix()
}

inline fun glVertices(mode: Int, block: () -> Unit) {
    GlStateManager.glBegin(mode)
    block()
    GlStateManager.glEnd()
}
