package rhx.psv.item

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.TicketType
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import rhx.psv.LOGGER
import rhx.psv.init.ResourceKeys
import rhx.psv.util.ext.comma
import rhx.psv.util.ext.component1
import rhx.psv.util.ext.component2
import rhx.psv.util.ext.component3
import rhx.psv.util.ext.space
import rhx.psv.util.ext.withBrackets

class TeleportCore :
    Item(
        Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .fireResistant(),
    ) {
    override fun appendHoverText(
        pStack: ItemStack,
        pLevel: Level?,
        pTooltipComponents: MutableList<Component>,
        pIsAdvanced: TooltipFlag,
    ) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced)

        val nbt = pStack.tag ?: return

        if (pLevel.isVoid) {
            val (x, y, z) = nbt.getStoredPos(TagType.ORIGINAL)
            val level = nbt.getStoredDim(TagType.ORIGINAL)
            pTooltipComponents.add(
                assembleChatComponent(x, y, z, "tooltip.psv.teleport_core.original", level.location().path.toString()),
            )
        } else {
            val (x, y, z) = nbt.getStoredPos(TagType.VOID)
            pTooltipComponents.add(
                assembleChatComponent(x, y, z, "tooltip.psv.teleport_core.void", "void"),
            )
        }
    }

    override fun use(
        pLevel: Level,
        pPlayer: Player,
        pUsedHand: InteractionHand,
    ): InteractionResultHolder<ItemStack> {
        val itemStack = pPlayer.getItemInHand(pUsedHand)
        if (pPlayer.isShiftKeyDown) return InteractionResultHolder.pass(itemStack)

        if (!pLevel.isClientSide) {
            pLevel as ServerLevel
            pPlayer as ServerPlayer

            val nbt = itemStack.orCreateTag

            if (pLevel.isVoid) {
                val pos = nbt.getStoredPos(TagType.ORIGINAL)
                val level =
                    pPlayer.server.getLevel(nbt.getStoredDim(TagType.ORIGINAL))
                        ?: return InteractionResultHolder.fail(itemStack)

                nbt.setStoredPos(TagType.VOID, pPlayer.blockPosition().below())
                teleportPlayer(pPlayer, level, pos) || return InteractionResultHolder.fail(itemStack)
            } else {
                val pos = nbt.getStoredPos(TagType.VOID)
                val level =
                    pPlayer.server.getLevel(ResourceKeys.VoidLevel)
                        ?: return InteractionResultHolder.fail(itemStack)

                nbt.setStoredPos(TagType.ORIGINAL, pPlayer.blockPosition().below())
                nbt.setStoredDim(TagType.ORIGINAL, pLevel.dimension())
                teleportPlayer(pPlayer, level, pos) || return InteractionResultHolder.fail(itemStack)
            }

            pPlayer.cooldowns.addCooldown(this, 20 * 3)
        }

        return InteractionResultHolder.pass(itemStack)
    }

    private enum class TagType(
        val prefixPos: String,
        val prefixDim: String,
    ) {
        ORIGINAL("teleport_original_pos", "teleport_original_dim"),
        VOID("teleport_void_pos", "teleport_void_dim"),
    }

    companion object {
        private val pos64 by lazy { BlockPos(0, 64, 0) }
        private val pos128 by lazy { BlockPos(0, 128, 0) }

        private val Level?.isVoid: Boolean
            get() = this?.dimensionTypeId() == ResourceKeys.VoidDimension

        private fun CompoundTag.getStoredPos(type: TagType): BlockPos {
            if (!contains(type.prefixPos)) return if (type == TagType.VOID) pos128 else pos64
            return getCompound(type.prefixPos).let {
                BlockPos(it.getInt("x"), it.getInt("y"), it.getInt("z"))
            }
        }

        private fun CompoundTag.setStoredPos(
            type: TagType,
            pos: BlockPos,
        ) {
            CompoundTag()
                .apply {
                    putInt("x", pos.x)
                    putInt("y", pos.y)
                    putInt("z", pos.z)
                }.also {
                    put(type.prefixPos, it)
                }
        }

        private fun CompoundTag.getStoredDim(type: TagType): ResourceKey<Level> {
            if (!contains(type.prefixDim)) return Level.OVERWORLD
            return ResourceKey.create(Registries.DIMENSION, ResourceLocation(getString(type.prefixDim)))
        }

        private fun CompoundTag.setStoredDim(
            type: TagType,
            dim: ResourceKey<Level>,
        ) {
            putString(type.prefixDim, dim.location().toString())
        }

        private fun assembleChatComponent(
            x: Int,
            y: Int,
            z: Int,
            key: String,
            dim: String,
        ) = Component
            .translatable(key)
            .space()
            .withBrackets(
                Component
                    .literal(x.toString())
                    .comma()
                    .append(y.toString())
                    .comma()
                    .append(z.toString())
                    .withStyle(ChatFormatting.AQUA),
            ).append(" - $dim")

        private fun teleportPlayer(
            player: ServerPlayer,
            targetLevel: ServerLevel,
            targetPos: BlockPos,
        ): Boolean {
            if (!targetLevel.isInWorldBounds(targetPos)) {
                player.sendSystemMessage(Component.translatable("message.psv.teleport_core.out_of_bounds"))
                return false
            }

            if (targetLevel.getBlockState(targetPos).isAir) {
                LOGGER.info("Generating block at $targetPos")
                targetLevel.setBlockAndUpdate(targetPos, Blocks.GLASS.defaultBlockState())
            }

            val standingPos = targetPos.above()
            if (!targetLevel.getBlockState(standingPos).isAir ||
                !targetLevel.getBlockState(standingPos.above()).isAir
            ) {
                player.sendSystemMessage(Component.translatable("message.psv.teleport_core.blocked"))
            }

            val chunkPos = ChunkPos(standingPos)
            targetLevel.chunkSource.addRegionTicket(
                TicketType.POST_TELEPORT,
                chunkPos,
                1,
                player.id,
            )

            player.stopRiding()

            player.teleportTo(
                targetLevel,
                standingPos.x.toDouble() + 0.5,
                standingPos.y.toDouble(),
                standingPos.z.toDouble() + 0.5,
                Mth.wrapDegrees(player.yRot),
                Mth.wrapDegrees(player.xRot),
            )

            return true
        }
    }
}
