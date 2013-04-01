package edgruberman.bukkit.obituaries;

import java.text.MessageFormat;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import edgruberman.bukkit.obituaries.util.EntitySubtype;
import edgruberman.bukkit.obituaries.util.JoinList;

class Translator {

    /** 0 = name, 1 = data */
    private static final String MATERIAL_DATA = "{0}/{1}";

    public static String formatMaterial(final String name, final Short data) {
        final String result = Main.courier.format("materials." + MessageFormat.format(Translator.MATERIAL_DATA, name, data));
        if (result == null) Main.courier.format("materials." + name);
        return result;
    }

    public static String formatMaterial(final Material material) {
        return Translator.formatMaterial(material.name(), null);
    }

    public static String formatMaterial(final Block block) {
        return Translator.formatMaterial(block.getType().name(), (short) block.getData());
    }

    public static String formatMaterial(final BlockState state) {
        return Translator.formatMaterial(state.getType().name(), (short) state.getRawData());
    }

    public static String formatMaterial(final ItemStack item) {
        return Translator.formatMaterial(item.getType().name(), item.getDurability());
    }

    public static String formatPotion(final Potion potion) {
        String type = null;
        if (potion.getType() != null) {
            type = Main.courier.format("potion.types." + potion.getType().name());
            if (type == null) type = potion.getType().name();
        }
        if (type == null) type = String.valueOf(potion.getNameId());

        String formatted = Main.courier.format("potion.base", "", type);
        if (potion.getLevel() > 1) formatted = Main.courier.format("potion.level", formatted, (potion.getLevel() == 2 ? "II" : potion.getLevel()));
        if (potion.hasExtendedDuration()) formatted = Main.courier.format("potion.extended", formatted);
        return formatted;
    }

    public static String formatItem(final ItemStack item) {
        String formatted = Translator.formatMaterial(item);

        if (item.getType() == Material.POTION && item.getDurability() != PotionType.WATER.getDamageValue())
            formatted = Translator.formatPotion(Potion.fromItemStack(item));

        // TODO enumerate enchantments
        if (item.getEnchantments().size() > 0)
            formatted = Main.courier.format("enchanted", formatted);

        return formatted;
    }

    public static String describeEntity(final Entity entity) {
        if (entity instanceof Player) {
            return ((Player) entity).getDisplayName();
        }

        String result = Translator.formatEntitySubtype(entity);
        if (result == null) result = Translator.formatEntityType(entity);

        if (entity instanceof ThrownPotion) {
            final JoinList<String> effects = new JoinList<String>();
            for (final PotionEffect effect : ((ThrownPotion) entity).getEffects()) {
                final String pe = Main.courier.format("effects." + effect.getType().getName()); // TODO include level and duration
                effects.add(( pe != null ? pe : effect.getType().getName() ));
            }
            result = Main.courier.format("potion.base", result, effects);
        }

        if (result != null) return result;

        return entity.getType().name();
    }

    private static String formatEntityType(final Entity entity) {
        return Main.courier.format("entity-types." + entity.getType().name());
    }

    private static String formatEntitySubtype(final Entity entity) {
        EntitySubtype subtype;
        try {
            subtype = EntitySubtype.of(entity);
        } catch (final IllegalArgumentException e) {
            return null;
        }

        return Main.courier.format("entity-types." + subtype.getName());
    }

}
