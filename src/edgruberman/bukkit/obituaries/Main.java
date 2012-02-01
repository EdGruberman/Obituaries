package edgruberman.bukkit.obituaries;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public final class Main extends JavaPlugin {

    static MessageManager messageManager;

    @Override
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
    }

    @Override
    public void onEnable() {
        this.loadConfiguration();
        new Coroner(this);
        Main.messageManager.log("Plugin Enabled");
    }

    @Override
    public void onDisable() {
        Damage.last.clear();
        Main.messageManager.log("Plugin Disabled");
    }

    private void loadConfiguration() {
        FileConfiguration config = (new ConfigurationFile(this)).load();

        // Load default format.
        Coroner.causeFormats.clear();
        Coroner.causeFormats.put(null, config.getString("default"));

        // Load damage cause specific formats.
        for (String name: config.getConfigurationSection("DamageCause").getKeys(false)) {
            DamageCause cause = DamageCause.valueOf(name);
            if (cause == null) continue;

            Coroner.causeFormats.put(cause, config.getString("DamageCause." + cause.name(), Coroner.causeFormats.get(null)));
        }
        Main.messageManager.log(Coroner.causeFormats.size() + " cause formats loaded.", MessageLevel.CONFIG);

        // weapon
        Coroner.weaponFormat = config.getString("weapon", Coroner.weaponFormat);
        Main.messageManager.log("Weapon Format: " + Coroner.weaponFormat, MessageLevel.CONFIG);

        // hand
        Coroner.hand = config.getString("hand", Coroner.hand);
        Main.messageManager.log("Hand: " + Coroner.hand, MessageLevel.CONFIG);

        // enchanted
        Coroner.enchanted = config.getString("enchanted", Coroner.enchanted);
        Main.messageManager.log("Enchanted: " + Coroner.enchanted, MessageLevel.CONFIG);

        // owners
        Coroner.ownerFormats.clear();
        for (String name: config.getConfigurationSection("owners").getKeys(false)) {
            Coroner.ownerFormats.put(name, config.getString("owners." + name));
            Main.messageManager.log("Owner Format for " + name + ": " + Coroner.ownerFormats.get(name), MessageLevel.CONFIG);
        }

        // Entity
        Coroner.entityNames.clear();
        for (String name: config.getConfigurationSection("Entity").getKeys(false))
            Coroner.entityNames.put(name, config.getString("Entity." + name, name.toLowerCase()));

        Main.messageManager.log(Coroner.entityNames.size() + " entity names loaded.", MessageLevel.CONFIG);

        // Material
        Coroner.materialNames.clear();
        for (String name: config.getConfigurationSection("Material").getKeys(false)) {
            Material material = Material.valueOf(name);
            if (material == null) continue;

            Coroner.materialNames.put(material, config.getString("Material." + material.name(), material.name().toLowerCase()));
        }
        Main.messageManager.log(Coroner.materialNames.size() + " material names loaded.", MessageLevel.CONFIG);

        // MaterialData
        Coroner.materialDataNames.clear();
        for (String entry: config.getConfigurationSection("MaterialData").getKeys(false)) {
            Material material = Material.valueOf(entry.split(":")[0]);
            Byte data = Byte.parseByte(entry.split(":")[1]);
            Coroner.materialDataNames.put(new MaterialData(material, data), config.getString("MaterialData." + entry));
        }
        Main.messageManager.log(Coroner.materialDataNames.size() + " material data names loaded.", MessageLevel.CONFIG);
    }

}