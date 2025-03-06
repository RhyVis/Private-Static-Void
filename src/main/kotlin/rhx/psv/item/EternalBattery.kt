package rhx.psv.item

import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import rhx.psv.util.ComponentPreAssemble.addMaxTransfer
import rhx.psv.util.StaticEnergyStorage

class EternalBattery :
    Item(
        Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant(),
    ) {
    private val energyCap = LazyOptional.of { StaticEnergyStorage(ENERGY_TRANSFER) }

    override fun isFoil(pStack: ItemStack): Boolean = true

    override fun appendHoverText(
        pStack: ItemStack,
        pLevel: Level?,
        pTooltipComponents: MutableList<Component>,
        pIsAdvanced: TooltipFlag,
    ) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced)
        pTooltipComponents.addMaxTransfer(ENERGY_TRANSFER)
    }

    override fun useOn(pContext: UseOnContext): InteractionResult {
        if (pContext.level.isClientSide ||
            pContext.player == null ||
            !pContext.player!!.isShiftKeyDown
        ) {
            return super.useOn(pContext)
        }
        val te = pContext.level.getBlockEntity(pContext.clickedPos) ?: return super.useOn(pContext)

        var flag = false

        te.getCapability(ForgeCapabilities.ENERGY, pContext.clickedFace).ifPresent { cap ->
            if (!cap.canReceive()) return@ifPresent
            cap.receiveEnergy(Int.MAX_VALUE, false)
            flag = true
        }

        return if (flag) InteractionResult.SUCCESS else super.useOn(pContext)
    }

    override fun initCapabilities(
        stack: ItemStack?,
        nbt: CompoundTag?,
    ): ICapabilityProvider =
        object : ICapabilityProvider {
            override fun <T : Any?> getCapability(
                cap: Capability<T>,
                side: Direction?,
            ): LazyOptional<T> = ForgeCapabilities.ENERGY.orEmpty(cap, energyCap.cast())
        }

    companion object {
        const val ENERGY_TRANSFER = 500_000_000
    }
}
