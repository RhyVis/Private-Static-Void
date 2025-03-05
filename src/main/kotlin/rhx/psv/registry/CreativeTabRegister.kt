package rhx.psv.registry

import net.minecraft.world.item.CreativeModeTabs
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import rhx.psv.registry.Registry.ENERGY_SOURCE_BLOCK_ITEM
import rhx.psv.registry.Registry.TELEPORT_CORE

object CreativeTabRegister {
    fun handleCreativeTab(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TELEPORT_CORE)
        }
        if (event.tabKey == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ENERGY_SOURCE_BLOCK_ITEM)
        }
    }
}
