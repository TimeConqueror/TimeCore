package ru.timeconqueror.timecore.api.devtools.gen.lang;

import lombok.extern.log4j.Log4j2;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.timeconqueror.timecore.api.util.EnvironmentUtils;
import ru.timeconqueror.timecore.mixins.accessor.GatherDataEventAccessor;
import ru.timeconqueror.timecore.storage.Storage;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Generates lang entries and places them in en_us.json file.
 * It doesn't delete en_us.json!
 * If you don't have any special marks in your lang file,
 * it will generate stuff in its end.
 * <p>
 * All public methods here are runtime-safely. While <b>it works in runData launch mode</b>, it is quietly turned off in runClient/runServer or in common runtime.
 * <p>
 * If you have stuff to be generated not in the end of the file, you should place
 * <b>#MARK AUTO GEN START</b> and <b>#MARK AUTO GEN END</b> on any two lines of file, where you want to see generated stuff.
 * Be careful, all lines that are between these two marks will be deleted before generating, so don't place custom lang entries there.
 * <p>
 * Example:
 * <blockquote>
 * <pre>
 *  entity.timecore.test_entity.location=Test Entity
 *
 *  #MARK AUTO GEN START
 *  <- (here generator will create all its entries) ->
 *  #MARK AUTO GEN END
 *
 *  advancement.timecore.hello=Hello!
 *  </pre>
 * </blockquote>
 */
@Log4j2
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LangGeneratorFacade {
    private final LangJsonGenerator generator = new LangJsonGenerator();
    private final HashMap<String, LangSection<?>> sections = new LinkedHashMap<>();

    private final LangSection<CreativeModeTab> itemGroupSection = addSection(DefaultSections.ITEM_GROUPS.get());
    private final LangSection<Block> blockSection = addSection(DefaultSections.BLOCKS.get());
    private final LangSection<Item> itemSection = addSection(DefaultSections.ITEMS.get());
    private final LangSection<ArmorItem> armorSection = addSection(DefaultSections.ARMOR.get());
    private final LangSection<EntityType<?>> entitySection = addSection(DefaultSections.ENTITIES.get());
    private final LangSection<String> miscSection = addSection(DefaultSections.MISC.get());

    /**
     * Adds item entry to {@link DefaultSections#ITEMS}, which will be processed by generator on {@link GatherDataEvent}.
     * Generator will generate entries only in {@code runData} launch mode.
     *
     * @param item   item for which english location will be added to file
     * @param enName english localization location of item
     */
    public void addItemEntry(Item item, String enName) {
        if (shouldSave()) {
            itemSection.addEntry(item, enName);
        }
    }

    /**
     * Adds block entry to {@link DefaultSections#BLOCKS}, which will be processed by generator on {@link GatherDataEvent}.
     * Generator will generate entries only in {@code runData} launch mode.
     *
     * @param block  block for which english location will be added to file
     * @param enName english localization location of block
     */
    public void addBlockEntry(Block block, String enName) {
        if (shouldSave()) {
            blockSection.addEntry(block, enName);
        }
    }

    /**
     * Adds entity entry to {@link DefaultSections#ENTITIES}, which will be processed by generator on {@link GatherDataEvent}.
     * Generator will generate entries only in {@code runData} launch mode.
     *
     * @param entityEntry entry of entity for which english location will be added to file
     * @param enName      english localization location of entity
     */
    public void addEntityEntry(EntityType<?> entityEntry, String enName) {
        if (shouldSave()) {
            entitySection.addEntry(entityEntry, enName);
        }
    }

    /**
     * Adds item group entry to {@link DefaultSections#ITEM_GROUPS}, which will be processed by generator on {@link GatherDataEvent}.
     * Generator will generate entries only in {@code runData} launch mode.
     *
     * @param itemGroup item group for which english location will be added to file
     * @param enName    english localization location of item group
     */
    public void addItemGroupEntry(CreativeModeTab itemGroup, String enName) {
        if (shouldSave()) {
            itemGroupSection.addEntry(itemGroup, enName);
        }
    }

    /**
     * Adds armor item entry to {@link DefaultSections#ARMOR}, which will be processed by generator on {@link GatherDataEvent}.
     * Generator will generate entries only in {@code runData} launch mode.<br>
     * <p>
     * This method is only for common armor stuff names, like the "Diamond Helmet", where equipment slot ("Helmet") is the last word.<br>
     * The last word, which represents the equipment slot, will be framed automatically.<br>
     * <p>
     * For uncommon names see {@link #addArmorEntry(ArmorItem, String)} to set location directly.
     *
     * @param item           armor item for which english location will be added to file
     * @param materialEnName the english location of material, will be the first word in the full location
     */
    public void addArmorEntryByMaterial(ArmorItem item, String materialEnName) {
        if (shouldSave()) {
            EquipmentSlot equipmentSlot = item.getEquipmentSlot();

            String fullLocalizedName;
            switch (equipmentSlot) {
                case HEAD:
                    fullLocalizedName = materialEnName + "Helmet";
                    break;
                case CHEST:
                    fullLocalizedName = materialEnName + "Chestplate";
                    break;
                case LEGS:
                    fullLocalizedName = materialEnName + "Leggings";
                    break;
                case FEET:
                    fullLocalizedName = materialEnName + "Boots";
                    break;
                default:
                    throw new IllegalStateException("Unsupported equipment slot: " + equipmentSlot);
            }

            addArmorEntry(item, fullLocalizedName);
        }
    }

    /**
     * Adds armor item entry to {@link DefaultSections#ARMOR}, which will be processed by generator on {@link GatherDataEvent}.
     * Generator will generate entries only in {@code runData} launch mode.<br>
     * <p>
     * This method is only for uncommon armor stuff names, like the "Helmet of The Dark One", where "Helmet" is the first word, not the last.<br>
     * <p>
     * For common names see {@link #addArmorEntryByMaterial(ArmorItem, String)}
     *
     * @param item   armor item for which english location will be added to file
     * @param enName english localization location of item
     */
    public void addArmorEntry(ArmorItem item, String enName) {
        if (shouldSave()) {
            armorSection.addEntry(item, enName);
        }
    }

    /**
     * Adds miscellaneous entry to {@link DefaultSections#MISC}, which will be processed by generator on {@link GatherDataEvent}.
     * Generator will generate entries only in {@code runData} launch mode.
     *
     * @param key    full localization key of this thing
     * @param enName english localization location of this thing
     */
    public void addMiscEntry(String key, String enName) {
        if (shouldSave()) {
            miscSection.addEntry(key, enName);
        }
    }

    /**
     * Here you can add custom sections, which will be processed later by the generator.
     * Generator will generate entries only in {@code runData} launch mode.
     * <p>
     * All entries should be added before section is written to the file, otherwise exception will be thrown.
     *
     * @param langSection section to be added to the generator
     */
    public <T> LangSection<T> addSection(LangSection<T> langSection) {
        if (sections.put(langSection.getName(), langSection) != null) {
            throw new IllegalArgumentException("Lang section with location " + langSection.getName() + " already exists.");
        }

        return langSection;
    }

    private static boolean shouldSave() {
        return EnvironmentUtils.isInDataMode();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDataEvent(GatherDataEvent event) {
        GatherDataEventAccessor hiddenStuff = (GatherDataEventAccessor) event;

        for (String mod : hiddenStuff.getConfig().getMods()) {
            try {
                LangGeneratorFacade langGeneratorFacade = Storage.getFeatures(mod).getLangGeneratorFacade();
                langGeneratorFacade.generator.save(mod, langGeneratorFacade.sections);
            } catch (Exception ex) {
                throw new RuntimeException("Caught exception while saving lang entries for '%s' mod".formatted(mod), ex);
            }
        }
    }
}
