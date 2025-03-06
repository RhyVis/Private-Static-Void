package rhx.psv.util

import net.minecraftforge.energy.IEnergyStorage
import kotlin.math.min

open class StaticEnergyStorage(
    private val energyTransfer: Int,
    private val energyStorage: Int = energyTransfer,
) : IEnergyStorage {
    override fun receiveEnergy(
        maxReceive: Int,
        simulate: Boolean,
    ): Int = min(maxReceive, energyTransfer)

    override fun extractEnergy(
        maxExtract: Int,
        simulate: Boolean,
    ): Int = 0

    override fun getEnergyStored(): Int = energyStorage

    override fun getMaxEnergyStored(): Int = energyStorage

    override fun canExtract(): Boolean = true

    override fun canReceive(): Boolean = false
}
