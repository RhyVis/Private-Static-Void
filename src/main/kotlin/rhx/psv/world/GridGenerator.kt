package rhx.psv.world

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraftforge.registries.ForgeRegistries
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

class GridGenerator(
    private val setting: GridGeneratorSetting,
) : ChunkGenerator(FixedBiomeSource(setting.biome)) {
    companion object {
        val CODEC: Codec<GridGenerator> =
            RecordCodecBuilder.create { instance ->
                instance
                    .group(
                        GridGeneratorSetting.CODEC.fieldOf("setting").forGetter { it.setting },
                    ).apply(instance, ::GridGenerator)
            }
    }

    override fun codec(): Codec<out ChunkGenerator> = CODEC

    override fun fillFromNoise(
        pExecutor: Executor,
        pBlender: Blender,
        pRandom: RandomState,
        pStructureManager: StructureManager,
        pChunk: ChunkAccess,
    ): CompletableFuture<ChunkAccess> {
        val borderBlockState = setting.borderBlock.defaultBlockState()
        val innerBlockState = setting.innerBlock.defaultBlockState()

        val pos = BlockPos.MutableBlockPos()

        val heightMap1 = pChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG)
        val heightMap2 = pChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG)

        val y = setting.layerHeight

        for (x in 0 until 16) {
            for (z in 0 until 16) {
                val blockState = if (x == 0 || x == 15 || z == 0 || z == 15) borderBlockState else innerBlockState
                pChunk.setBlockState(pos.set(x, y, z), blockState, false)
                heightMap1.update(x, y, z, blockState)
                heightMap2.update(x, y, z, blockState)
            }
        }

        return CompletableFuture.completedFuture(pChunk)
    }

    override fun getGenDepth(): Int = 384

    override fun getSeaLevel(): Int = -64

    override fun getMinY(): Int = -64

    override fun getBaseHeight(
        pX: Int,
        pZ: Int,
        pType: Heightmap.Types,
        pLevel: LevelHeightAccessor,
        pRandom: RandomState,
    ): Int = pLevel.minBuildHeight

    override fun getBaseColumn(
        pX: Int,
        pZ: Int,
        pHeight: LevelHeightAccessor,
        pRandom: RandomState,
    ): NoiseColumn =
        NoiseColumn(
            pHeight.minBuildHeight,
            arrayOf(Blocks.AIR.defaultBlockState()),
        )

    override fun addDebugScreenInfo(
        pInfo: MutableList<String>,
        pRandom: RandomState,
        pPos: BlockPos,
    ) {
        pInfo.add("Grid Generator on layer ${setting.layerHeight}")
    }

    override fun applyCarvers(
        pLevel: WorldGenRegion,
        pSeed: Long,
        pRandom: RandomState,
        pBiomeManager: BiomeManager,
        pStructureManager: StructureManager,
        pChunk: ChunkAccess,
        pStep: GenerationStep.Carving,
    ) {}

    override fun buildSurface(
        pLevel: WorldGenRegion,
        pStructureManager: StructureManager,
        pRandom: RandomState,
        pChunk: ChunkAccess,
    ) {}

    override fun spawnOriginalMobs(pLevel: WorldGenRegion) {}

    class GridGeneratorSetting(
        val layerHeight: Int,
        val biome: Holder<Biome>,
        val borderBlock: Block,
        val innerBlock: Block,
    ) {
        companion object {
            val CODEC: Codec<GridGeneratorSetting> =
                RecordCodecBuilder.create { instance ->
                    instance
                        .group(
                            Codec.INT
                                .fieldOf("layer_height")
                                .orElse(128)
                                .forGetter { it.layerHeight },
                            Biome.CODEC.fieldOf("biome").forGetter { it.biome },
                            ForgeRegistries.BLOCKS.codec
                                .fieldOf("border_block")
                                .orElse(Blocks.AIR)
                                .forGetter { it.borderBlock },
                            ForgeRegistries.BLOCKS.codec
                                .fieldOf("inner_block")
                                .orElse(Blocks.AIR)
                                .forGetter { it.innerBlock },
                        ).apply(instance, ::GridGeneratorSetting)
                }
        }
    }
}
