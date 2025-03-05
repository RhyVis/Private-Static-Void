package rhx.psv

import net.minecraftforge.fml.common.Mod
import rhx.psv.registry.CreativeTabRegister
import rhx.psv.registry.Registry.mountRegistry
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist

@Mod(MOD_ID)
object PSV {
    init {
        MOD_BUS.mountRegistry()

        runForDist(
            clientTarget = {
                MOD_BUS.addListener(CreativeTabRegister::handleCreativeTab)
            },
            serverTarget = {},
        )
    }
}
