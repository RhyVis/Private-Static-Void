package rhx.psv.util

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object ComponentPreAssemble {
    fun ofMaxTransfer(
        energyTransfer: Int,
        timeUnit: String = "t",
    ): Component =
        Component
            .translatable("tooltip.psv.common.max_transfer")
            .append(
                Component.literal(energyTransfer.toString()).withStyle(ChatFormatting.AQUA),
            ).append(" FE/$timeUnit")

    fun MutableList<Component>.addMaxTransfer(
        energyTransfer: Int,
        timeUnit: String = "t",
    ) {
        add(ofMaxTransfer(energyTransfer, timeUnit))
    }
}
