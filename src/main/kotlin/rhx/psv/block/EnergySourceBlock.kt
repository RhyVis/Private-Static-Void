package rhx.psv.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class EnergySourceBlock :
    Block(
        Properties
            .of()
            .strength(3.5f)
            .sound(SoundType.ANVIL),
    ),
    EntityBlock {
    override fun newBlockEntity(
        pPos: BlockPos,
        pState: BlockState,
    ): BlockEntity = EnergySourceBlockEntity(pPos, pState)

    override fun <T : BlockEntity?> getTicker(
        pLevel: Level,
        pState: BlockState,
        pBlockEntityType: BlockEntityType<T>,
    ): BlockEntityTicker<T>? =
        if (pLevel.isClientSide) {
            null
        } else {
            BlockEntityTicker { _, _, _, blockEntity ->
                if (blockEntity is EnergySourceBlockEntity) {
                    blockEntity.onTickServer()
                }
            }
        }
}
