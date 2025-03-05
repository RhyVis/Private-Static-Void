package rhx.psv.init

import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.loot.packs.VanillaBlockLoot
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.data.tags.ItemTagsProvider
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.Tags
import net.minecraftforge.common.data.BlockTagsProvider
import net.minecraftforge.common.data.ExistingFileHelper
import net.minecraftforge.common.data.LanguageProvider
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.registries.ForgeRegistries
import rhx.psv.MOD_ID
import rhx.psv.registry.Registry
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@Suppress("Unused")
@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
object DataGeneration {
    @SubscribeEvent
    fun handleGenerate(event: GatherDataEvent) {
        val generator = event.generator
        val output = generator.packOutput
        val helper = event.existingFileHelper
        val lookup = event.lookupProvider

        generator.addProvider(event.includeServer(), Recipe(output))
        generator.addProvider(event.includeServer(), LootTables(output))
        generator.addProvider(event.includeServer(), BlockTag(output, lookup, helper))

        generator.addProvider(event.includeClient(), BlockState(output, helper))
        generator.addProvider(event.includeClient(), ItemModel(output, helper))
        generator.addProvider(event.includeClient(), LanguageEn(output))
        generator.addProvider(event.includeClient(), LanguageZh(output))
    }

    class Recipe(
        output: PackOutput,
    ) : RecipeProvider(output) {
        override fun buildRecipes(pWriter: Consumer<FinishedRecipe>) {
            ShapedRecipeBuilder
                .shaped(RecipeCategory.MISC, Registry.TELEPORT_CORE)
                .pattern("DDD")
                .pattern("DID")
                .pattern("DDD")
                .define('D', Tags.Items.ENDER_PEARLS)
                .define('I', ItemTags.ANVIL)
                .unlockedBy("unlock", has(Tags.Items.ENDER_PEARLS))
                .save(pWriter)
            ShapedRecipeBuilder
                .shaped(RecipeCategory.MISC, Registry.ENERGY_SOURCE_BLOCK_ITEM)
                .pattern("AAA")
                .pattern("DID")
                .pattern("AAA")
                .define('A', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('D', Tags.Items.DUSTS_GLOWSTONE)
                .define('I', ItemTags.ANVIL)
                .unlockedBy("unlock", has(Tags.Items.DUSTS_GLOWSTONE))
                .save(pWriter)
        }
    }

    class ItemModel(
        output: PackOutput,
        existingFileHelper: ExistingFileHelper,
    ) : ItemModelProvider(
            output,
            MOD_ID,
            existingFileHelper,
        ) {
        override fun registerModels() {
            basicItem(Registry.TELEPORT_CORE)
            withExistingParent("energy_source", modLoc("block/energy_source"))
        }
    }

    class ItemTag(
        output: PackOutput,
        lookup: CompletableFuture<HolderLookup.Provider>,
        blockTags: BlockTagsProvider,
        helper: ExistingFileHelper,
    ) : ItemTagsProvider(output, lookup, blockTags.contentsGetter(), MOD_ID, helper) {
        override fun addTags(pProvider: HolderLookup.Provider) {
        }
    }

    class BlockTag(
        output: PackOutput,
        lookup: CompletableFuture<HolderLookup.Provider>,
        helper: ExistingFileHelper?,
    ) : BlockTagsProvider(output, lookup, MOD_ID, helper) {
        override fun addTags(pProvider: HolderLookup.Provider) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Registry.ENERGY_SOURCE_BLOCK)
        }
    }

    class BlockState(
        output: PackOutput,
        existingFileHelper: ExistingFileHelper,
    ) : BlockStateProvider(
            output,
            MOD_ID,
            existingFileHelper,
        ) {
        override fun registerStatesAndModels() {
            simpleBlock(Registry.ENERGY_SOURCE_BLOCK)
        }
    }

    class LootTables(
        output: PackOutput,
    ) : LootTableProvider(
            output,
            emptySet(),
            listOf(
                SubProviderEntry(::BlockLoot, LootContextParamSets.BLOCK),
            ),
        ) {
        class BlockLoot : VanillaBlockLoot() {
            override fun generate() {
                dropSelf(Registry.ENERGY_SOURCE_BLOCK)
            }

            override fun getKnownBlocks(): MutableIterable<Block> =
                ForgeRegistries.BLOCKS.entries
                    .filter { it.key.location().namespace == MOD_ID }
                    .map { it.value }
                    .toMutableList()
        }
    }

    class LanguageEn(
        output: PackOutput,
    ) : LanguageProvider(output, MOD_ID, "en_us") {
        override fun addTranslations() {
            addItem("Teleport Core") { Registry.TELEPORT_CORE }
            addBlock("Energy Source Block") { Registry.ENERGY_SOURCE_BLOCK }
            add("biome.psv.void", "Void")
            add("tooltip.psv.teleport_core.original", "Original Position")
            add("tooltip.psv.teleport_core.void", "Target Position")
            add("message.psv.teleport_core.blocked", "You seem to be blocked by something!")
            add("message.psv.teleport_core.out_of_bounds", "The destination is out of the world bounds!")
        }
    }

    class LanguageZh(
        output: PackOutput,
    ) : LanguageProvider(output, MOD_ID, "zh_cn") {
        override fun addTranslations() {
            addItem("传送核心") { Registry.TELEPORT_CORE }
            addBlock("能量源") { Registry.ENERGY_SOURCE_BLOCK }
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

    fun LanguageProvider.addBlock(
        name: String,
        block: () -> Block,
    ) {
        addBlock(block, name)
    }
}
