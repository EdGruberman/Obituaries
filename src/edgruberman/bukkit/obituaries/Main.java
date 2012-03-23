package edgruberman.bukkit.obituaries;

import java.util.logging.Handler;
import java.util.logging.Level;

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

    private static final String MINIMUM_VERSION_CONFIG = "2.0.0b18";

    static MessageManager messageManager;

    private ConfigurationFile configurationFile;

    @Override
    public void onEnable() {
        this.configurationFile = new ConfigurationFile(this);
        this.configurationFile.setMinVersion(Main.MINIMUM_VERSION_CONFIG);
        this.configurationFile.load();
        this.setLoggingLevel();

        Main.messageManager = new MessageManager(this);

        this.configure();

        new Coroner(this);
    }

    @Override
    public void onDisable() {
        Damage.last.clear();
    }

    private void setLoggingLevel() {
        final String name = this.configurationFile.getConfig().getString("logLevel", "INFO");
        Level level = MessageLevel.parse(name);
        if (level == null) level = Level.INFO;

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().log(Level.CONFIG, "Logging level set to: " + this.getLogger().getLevel());
    }

    private void configure() {
        final FileConfiguration config = (new ConfigurationFile(this)).load();

        Coroner.causeFormats.clear();
        for (final String name: config.getConfigurationSection("DamageCause").getKeys(false)) {
            // TODO error checking for NPE
            final DamageCause cause = DamageCause.valueOf(name);
            if (cause == null) continue;

            Coroner.causeFormats.put(cause, config.getString("DamageCause." + cause.name(), Coroner.causeFormats.get(null)));
        }
        this.getLogger().log(Level.CONFIG, Coroner.causeFormats.size() + " cause formats loaded");

        Coroner.weaponFormat = config.getString("weapon", Coroner.weaponFormat);
       this.getLogger().log(Level.CONFIG, "Weapon Format: " + Coroner.weaponFormat);

        Coroner.hand = config.getString("hand", Coroner.hand);
       this.getLogger().log(Level.CONFIG, "Hand: " + Coroner.hand);

        Coroner.enchanted = config.getString("enchanted", Coroner.enchanted);
       this.getLogger().log(Level.CONFIG, "Enchanted: " + Coroner.enchanted);

        Coroner.edible = config.getString("edible", Coroner.edible);
       this.getLogger().log(Level.CONFIG, "Edible: " + Coroner.edible);

        Coroner.drinkable = config.getString("drinkable", Coroner.drinkable);
       this.getLogger().log(Level.CONFIG, "Drinkable: " + Coroner.drinkable);

        Coroner.ownerFormats.clear();
        for (final String name: config.getConfigurationSection("owners").getKeys(false)) {
            Coroner.ownerFormats.put(name, config.getString("owners." + name));
           this.getLogger().log(Level.CONFIG, "Owner Format for " + name + ": " + Coroner.ownerFormats.get(name));
        }

        Coroner.entityNames.clear();
        for (final String name: config.getConfigurationSection("Entity").getKeys(false))
            Coroner.entityNames.put(name, config.getString("Entity." + name, name.toLowerCase()));
       this.getLogger().log(Level.CONFIG, Coroner.entityNames.size() + " entity names loaded");

        Coroner.materialNames.clear();
        for (final String name: config.getConfigurationSection("Material").getKeys(false)) {
            final Material material = Material.valueOf(name);
            if (material == null) continue;

            Coroner.materialNames.put(material, config.getString("Material." + material.name(), material.name().toLowerCase()));
        }
       this.getLogger().log(Level.CONFIG, Coroner.materialNames.size() + " material names loaded");

        Coroner.materialDataNames.clear();
        for (final String entry: config.getConfigurationSection("MaterialData").getKeys(false)) {
            final Material material = Material.valueOf(entry.split(":")[0]);
            final Byte data = Byte.parseByte(entry.split(":")[1]);
            Coroner.materialDataNames.put(new MaterialData(material, data), config.getString("MaterialData." + entry));
        }
       this.getLogger().log(Level.CONFIG, Coroner.materialDataNames.size() + " material data names loaded");

        Coroner.itemStackNames.clear();
        for (final String entry: config.getConfigurationSection("ItemStack").getKeys(false)) {
            final Material material = Material.valueOf(entry.split(":")[0]);
            final Short data = Short.parseShort(entry.split(":")[1]);
            Coroner.itemStackNames.put(new ItemStack(material, 0, data), config.getString("ItemStack." + entry));
        }
       this.getLogger().log(Level.CONFIG, Coroner.itemStackNames.size() + " item stack names loaded");

        Coroner.potionEffectTypeNames.clear();
        for (final String entry: config.getConfigurationSection("PotionEffectType").getKeys(false)) {
            final PotionEffectType potionEffectType = PotionEffectType.getByName(entry);
            Coroner.potionEffectTypeNames.put(potionEffectType, entry);
        }
       this.getLogger().log(Level.CONFIG, Coroner.potionEffectTypeNames.size() + " potion effect type names loaded");
    }

}
