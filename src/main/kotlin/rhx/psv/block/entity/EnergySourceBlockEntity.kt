package rhx.psv.block.entity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import rhx.psv.registry.Registry
import rhx.psv.util.StaticEnergyStorage

class EnergySourceBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(Registry.ENERGY_SOURCE_BLOCK_ENTITY, pos, state) {
    private val energy =
        object : StaticEnergyStorage(ENERGY_TRANSFER) {
            override fun extractEnergy(
                maxExtract: Int,
                simulate: Boolean,
            ): Int {
                if (!simulate) {
                    level?.sendBlockUpdated(
                        worldPosition,
                        level!!.getBlockState(worldPosition),
                        level!!.getBlockState(worldPosition),
                        2,
                    )
                }
                return super.extractEnergy(maxExtract, simulate)
            }
        }

    override fun <T> getCapability(cap: Capability<T>): LazyOptional<T> =
        if (!remove && cap == ForgeCapabilities.ENERGY) LazyOptional.of { energy }.cast() else super.getCapability(cap)

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    fun onTickServer() {
        pushEnergy()
    }

    private fun pushEnergy() {
        if (level == null) return

        val level = level as ServerLevel

        for (direction in Direction.entries) {
            level.getBlockEntity(blockPos.relative(direction))?.let { entity ->
                entity.getCapability(ForgeCapabilities.ENERGY, direction.opposite).ifPresent { cap ->
                    if (cap.canReceive()) cap.receiveEnergy(ENERGY_TRANSFER, false)
                }
            }
        }
    }

    companion object {
        const val ENERGY_TRANSFER = 500_000_000
    }
}
