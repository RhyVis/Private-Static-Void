package rhx.psv.init

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.dimension.DimensionType
import rhx.psv.MOD_ID

object ResourceKeys {
    val VoidDimension: ResourceKey<DimensionType> =
        ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation(MOD_ID, "void"))
    val VoidBiome: ResourceKey<Biome> =
        ResourceKey.create(Registries.BIOME, ResourceLocation(MOD_ID, "void"))
    val VoidLevel: ResourceKey<Level> =
        ResourceKey.create(Registries.DIMENSION, ResourceLocation(MOD_ID, "void"))
}
