package rhx.psv.util.ext

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

fun MutableComponent.space(): MutableComponent = this.append(" ")

fun MutableComponent.comma(): MutableComponent = this.append(", ")

fun MutableComponent.withBrackets(inner: Component): MutableComponent = this.append("(").append(inner).append(")")
