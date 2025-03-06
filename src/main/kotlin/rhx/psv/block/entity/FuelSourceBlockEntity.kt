package rhx.psv.block.entity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.ForgeCapabilities
import rhx.psv.registry.Registry

class FuelSourceBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(Registry.FUEL_SOURCE_BLOCK_ENTITY, pos, state) {
    private var ticker = 0

    fun onTickServer() {
        ticker--
        if (ticker <= 0) {
            ticker = TICK_INTERVAL
            pushFuel()
        }
    }

    override fun saveAdditional(pTag: CompoundTag) {
        super.saveAdditional(pTag)
        pTag.putInt(TICK_KEY, ticker)
    }

    override fun load(pTag: CompoundTag) {
        super.load(pTag)
        ticker = pTag.getInt(TICK_KEY)
    }

    private val coalStack by lazy { ItemStack(Items.COAL, 64) }

    private fun pushFuel() {
        if (level == null) return

        val level = level as ServerLevel

        Direction.entries.forEach { direction ->
            level.getBlockEntity(blockPos.relative(direction))?.let { entity ->
                entity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.opposite).ifPresent { cap ->
                    val available = cap.slots
                    if (available == 0) return@ifPresent

                    for (availableSlot in 0 until available) {
                        if (cap.isItemValid(availableSlot, coalStack)) {
                            cap.insertItem(availableSlot, coalStack.copy(), false)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TICK_INTERVAL = 32 * 20
        const val TICK_KEY = "FuelSourceBlockEntityTicker"
    }
}
