package edgruberman.bukkit.obituaries;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public final class Main extends JavaPlugin {

    static MessageManager messageManager;

    @Override
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
    }

    @Override
    public void onEnable() {
        this.loadConfiguration();
        new Coroner(this);
    }

    @Override
    public void onDisable() {
        Damage.last.clear();
    }

    private void loadConfiguration() {
        final FileConfiguration config = (new ConfigurationFile(this)).load();

        Coroner.causeFormats.clear();
        for (final String name: config.getConfigurationSection("DamageCause").getKeys(false)) {
            // TODO error checking for NPE
            final DamageCause cause = DamageCause.valueOf(name);
            if (cause == null) continue;

            Coroner.causeFormats.put(cause, config.getString("DamageCause." + cause.name(), Coroner.causeFormats.get(null)));
        }
        Main.messageManager.log(Coroner.causeFormats.size() + " cause formats loaded", MessageLevel.CONFIG);

        Coroner.weaponFormat = config.getString("weapon", Coroner.weaponFormat);
        Main.messageManager.log("Weapon Format: " + Coroner.weaponFormat, MessageLevel.CONFIG);

        Coroner.hand = config.getString("hand", Coroner.hand);
        Main.messageManager.log("Hand: " + Coroner.hand, MessageLevel.CONFIG);

        Coroner.enchanted = config.getString("enchanted", Coroner.enchanted);
        Main.messageManager.log("Enchanted: " + Coroner.enchanted, MessageLevel.CONFIG);

        Coroner.edible = config.getString("edible", Coroner.edible);
        Main.messageManager.log("Edible: " + Coroner.edible, MessageLevel.CONFIG);

        Coroner.drinkable = config.getString("drinkable", Coroner.drinkable);
        Main.messageManager.log("Drinkable: " + Coroner.drinkable, MessageLevel.CONFIG);

        Coroner.ownerFormats.clear();
        for (final String name: config.getConfigurationSection("owners").getKeys(false)) {
            Coroner.ownerFormats.put(name, config.getString("owners." + name));
            Main.messageManager.log("Owner Format for " + name + ": " + Coroner.ownerFormats.get(name), MessageLevel.CONFIG);
        }

        Coroner.entityNames.clear();
        for (final String name: config.getConfigurationSection("Entity").getKeys(false))
            Coroner.entityNames.put(name, config.getString("Entity." + name, name.toLowerCase()));
        Main.messageManager.log(Coroner.entityNames.size() + " entity names loaded", MessageLevel.CONFIG);

        Coroner.materialNames.clear();
        for (final String name: config.getConfigurationSection("Material").getKeys(false)) {
            final Material material = Material.valueOf(name);
            if (material == null) continue;

            Coroner.materialNames.put(material, config.getString("Material." + material.name(), material.name().toLowerCase()));
        }
        Main.messageManager.log(Coroner.materialNames.size() + " material names loaded", MessageLevel.CONFIG);

        Coroner.materialDataNames.clear();
        for (final String entry: config.getConfigurationSection("MaterialData").getKeys(false)) {
            final Material material = Material.valueOf(entry.split(":")[0]);
            final Byte data = Byte.parseByte(entry.split(":")[1]);
            Coroner.materialDataNames.put(new MaterialData(material, data), config.getString("MaterialData." + entry));
        }
        Main.messageManager.log(Coroner.materialDataNames.size() + " material data names loaded", MessageLevel.CONFIG);

        Coroner.itemStackNames.clear();
        for (final String entry: config.getConfigurationSection("ItemStack").getKeys(false)) {
            final Material material = Material.valueOf(entry.split(":")[0]);
            final Short data = Short.parseShort(entry.split(":")[1]);
            Coroner.itemStackNames.put(new ItemStack(material, 0, data), config.getString("ItemStack." + entry));
        }
        Main.messageManager.log(Coroner.itemStackNames.size() + " item stack names loaded", MessageLevel.CONFIG);

        Coroner.potionEffectTypeNames.clear();
        for (final String entry: config.getConfigurationSection("PotionEffectType").getKeys(false)) {
            final PotionEffectType potionEffectType = PotionEffectType.getByName(entry);
            Coroner.potionEffectTypeNames.put(potionEffectType, entry);
        }
        Main.messageManager.log(Coroner.potionEffectTypeNames.size() + " potion effect type names loaded", MessageLevel.CONFIG);
    }

}
