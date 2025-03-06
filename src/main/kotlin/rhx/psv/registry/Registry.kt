package rhx.psv.registry

import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import rhx.psv.MOD_ID
import rhx.psv.block.EnergySourceBlock
import rhx.psv.block.FuelSourceBlock
import rhx.psv.block.entity.EnergySourceBlockEntity
import rhx.psv.block.entity.FuelSourceBlockEntity
import rhx.psv.item.EternalBattery
import rhx.psv.item.TeleportCore
import rhx.psv.world.GridGenerator
import thedarkcolour.kotlinforforge.forge.registerObject

@Suppress("unused", "MemberVisibilityCanBePrivate")
object Registry {
    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID)
    private val BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID)
    private val BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID)
    private val CHUNK_GENERATOR = DeferredRegister.create(Registries.CHUNK_GENERATOR, MOD_ID)

    val TELEPORT_CORE: Item by
        ITEMS.registerObject("teleport_core") { TeleportCore() }

    val ETERNAL_BATTERY: Item by
        ITEMS.registerObject("eternal_battery") { EternalBattery() }

    val ENERGY_SOURCE_BLOCK: Block by
        BLOCKS.registerObject("energy_source") { EnergySourceBlock() }

    val ENERGY_SOURCE_BLOCK_ITEM: BlockItem by
        ITEMS.registerObject("energy_source") { BlockItem(ENERGY_SOURCE_BLOCK, Item.Properties()) }

    val ENERGY_SOURCE_BLOCK_ENTITY: BlockEntityType<EnergySourceBlockEntity> by
        BLOCK_ENTITIES.registerObject("energy_source") {
            BlockEntityType.Builder.of(::EnergySourceBlockEntity, ENERGY_SOURCE_BLOCK).build(null)
        }

    val FUEL_SOURCE_BLOCK: Block by
        BLOCKS.registerObject("fuel_source") { FuelSourceBlock() }

    val FUEL_SOURCE_BLOCK_ITEM: BlockItem by
        ITEMS.registerObject("fuel_source") { BlockItem(FUEL_SOURCE_BLOCK, Item.Properties()) }

    val FUEL_SOURCE_BLOCK_ENTITY: BlockEntityType<FuelSourceBlockEntity> by
        BLOCK_ENTITIES.registerObject("fuel_source") {
            BlockEntityType.Builder.of(::FuelSourceBlockEntity, FUEL_SOURCE_BLOCK).build(null)
        }

    val GRID_GENERATOR by
        CHUNK_GENERATOR.registerObject("grid_generator") { GridGenerator.CODEC }

    private fun handleRegistry(bus: IEventBus) {
        ITEMS.register(bus)
        BLOCKS.register(bus)
        BLOCK_ENTITIES.register(bus)
        CHUNK_GENERATOR.register(bus)
    }

    fun IEventBus.mountRegistry() {
        handleRegistry(this)
    }
}
