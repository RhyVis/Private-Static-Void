package rhx.psv.init

import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import rhx.psv.PSV.ID
import rhx.psv.item.TeleportCore
import rhx.psv.world.GridGenerator
import thedarkcolour.kotlinforforge.forge.registerObject

@Suppress("unused")
object RegistryHandler {
    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, ID)
    private val CHUNK_GENERATOR = DeferredRegister.create(Registries.CHUNK_GENERATOR, ID)

    val TELEPORT_CORE by
        ITEMS.registerObject("teleport_core") { TeleportCore() }

    val GRID_GENERATOR by
        CHUNK_GENERATOR.registerObject("grid_generator") { GridGenerator.CODEC }

    fun handleRegistry(bus: IEventBus) {
        ITEMS.register(bus)
        CHUNK_GENERATOR.register(bus)
    }

    fun handleCreativeTab(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TELEPORT_CORE)
        }
    }
}
