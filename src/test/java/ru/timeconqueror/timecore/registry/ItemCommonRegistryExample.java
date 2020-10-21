package ru.timeconqueror.timecore.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import ru.timeconqueror.timecore.api.client.resource.location.TextureLocation;
import ru.timeconqueror.timecore.registry.common.ItemTimeRegistry;

@TimeAutoRegistrable
public class ItemCommonRegistryExample extends ItemTimeRegistry {
    public static ItemPropsFactory miscGrouped = new ItemPropsFactory(ItemGroup.TAB_MISC);

    public static Item mcDiamond = new Item(miscGrouped.createProps());

    @Override
    public void register() {
        regItem(mcDiamond, "test_diamond").regDefaultModel(new TextureLocation("minecraft", "item/diamond"));
    }
}
