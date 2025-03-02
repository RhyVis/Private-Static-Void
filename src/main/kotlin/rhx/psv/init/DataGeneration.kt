package rhx.psv.init

import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.Tags
import net.minecraftforge.common.data.ExistingFileHelper
import net.minecraftforge.common.data.LanguageProvider
import net.minecraftforge.data.event.GatherDataEvent
import rhx.psv.PSV
import java.util.function.Consumer

object DataGeneration {
    fun handleGenerate(event: GatherDataEvent) {
        val generator = event.generator
        val output = generator.packOutput
        val helper = event.existingFileHelper

        generator.addProvider(event.includeServer(), Recipe(output))

        generator.addProvider(event.includeClient(), ItemModel(output, helper))
        generator.addProvider(event.includeClient(), LanguageEn(output))
        generator.addProvider(event.includeClient(), LanguageZh(output))
    }

    class Recipe(
        output: PackOutput,
    ) : RecipeProvider(output) {
        override fun buildRecipes(pWriter: Consumer<FinishedRecipe>) {
            ShapedRecipeBuilder
                .shaped(RecipeCategory.MISC, RegistryHandler.TELEPORT_CORE)
                .pattern("DDD")
                .pattern("DID")
                .pattern("DDD")
                .define('D', Tags.Items.ENDER_PEARLS)
                .define('I', ItemTags.ANVIL)
                .unlockedBy("unlock", has(Tags.Items.ENDER_PEARLS))
                .save(pWriter)
        }
    }

    class ItemModel(
        output: PackOutput,
        existingFileHelper: ExistingFileHelper,
    ) : ItemModelProvider(
            output,
            PSV.ID,
            existingFileHelper,
        ) {
        override fun registerModels() {
            singleTexture(
                "teleport_core",
                mcLoc("item/generated"),
                "layer0",
                modLoc("item/teleport_core"),
            )
        }
    }

    class LanguageEn(
        output: PackOutput,
    ) : LanguageProvider(output, PSV.ID, "en_us") {
        override fun addTranslations() {
            addItem("Teleport Core") { RegistryHandler.TELEPORT_CORE }
            add("biome.psv.void", "Void")
            add("tooltip.psv.teleport_core.original", "Original Position")
            add("tooltip.psv.teleport_core.void", "Target Position")
            add("message.psv.teleport_core.blocked", "You seem to be blocked by something!")
            add("message.psv.teleport_core.out_of_bounds", "The destination is out of the world bounds!")
        }
    }

    class LanguageZh(
        output: PackOutput,
    ) : LanguageProvider(output, PSV.ID, "zh_cn") {
        override fun addTranslations() {
            addItem("传送核心") { RegistryHandler.TELEPORT_CORE }
            add("biome.psv.void", "虚空")
            add("tooltip.psv.teleport_core.original", "原始位置")
            add("tooltip.psv.teleport_core.void", "目标位置")
            add("message.psv.teleport_core.blocked", "你似乎被什么东西挡住了！")
            add("message.psv.teleport_core.out_of_bounds", "目标位置超出了世界边界！")
        }
    }

    fun LanguageProvider.addItem(
        name: String,
        item: () -> Item,
    ) {
        addItem(item, name)
    }
}
