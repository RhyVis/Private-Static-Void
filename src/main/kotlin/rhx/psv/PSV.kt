package rhx.psv

import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import rhx.psv.PSV.ID
import rhx.psv.init.DataGeneration
import rhx.psv.init.RegistryHandler
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist

@Mod(ID)
object PSV {
    const val ID = "psv"

    init {
        RegistryHandler.handleRegistry(MOD_BUS)

        MOD_BUS.addListener(DataGeneration::handleGenerate)

        runForDist(
            clientTarget = {
                MOD_BUS.addListener(RegistryHandler::handleCreativeTab)
            },
            serverTarget = {},
        )
    }
}

val LOGGER: Logger = LogManager.getLogger(ID)
