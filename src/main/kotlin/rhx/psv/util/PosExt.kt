package rhx.psv.util

import net.minecraft.core.BlockPos

operator fun BlockPos.component1() = this.x

operator fun BlockPos.component2() = this.y

operator fun BlockPos.component3() = this.z
