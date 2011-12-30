package edgruberman.bukkit.simpledeathnotices;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public final class Main extends JavaPlugin {
    
    static ConfigurationFile configurationFile;
    static MessageManager messageManager;
    
    @Override
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Main.configurationFile = new ConfigurationFile(this);
    }
    
    @Override
    public void onEnable() {
        this.loadConfiguration();
        new DeathMonitor(this);
        new ReportShredder(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    @Override
    public void onDisable() {
        DamageReport.last.clear();
        
        Main.messageManager.log("Plugin Disabled");
    }
    
    private void loadConfiguration() {
        FileConfiguration cfg = Main.configurationFile.getConfig();
        
        // Load default format.
        DeathMonitor.causeFormats.clear();
        DeathMonitor.causeFormats.put(null, cfg.getString("default", DeathMonitor.DEFAULT_FORMAT));
        
        // Load damage cause specific formats.
        for (String name: cfg.getConfigurationSection("DamageCause").getKeys(false)) {
            DamageCause cause = DamageCause.valueOf(name);
            if (cause == null) continue;
            
            DeathMonitor.causeFormats.put(cause, cfg.getString("DamageCause." + cause.name(), DeathMonitor.DEFAULT_FORMAT));
        }
        Main.messageManager.log(DeathMonitor.causeFormats.size() + " cause formats loaded.", MessageLevel.CONFIG);
        
        // weapon
        DeathMonitor.weaponFormat = cfg.getString("weapon", DeathMonitor.DEFAULT_WEAPON_FORMAT);
        Main.messageManager.log("Weapon Format: " + DeathMonitor.weaponFormat, MessageLevel.CONFIG);
        
        // hand
        DeathMonitor.hand = cfg.getString("hand", DeathMonitor.DEFAULT_HAND);
        Main.messageManager.log("Hand: " + DeathMonitor.hand, MessageLevel.CONFIG);
        
        // enchanted
        DeathMonitor.enchanted = cfg.getString("enchanted", DeathMonitor.DEFAULT_ENCHANTED);
        Main.messageManager.log("Enchanted: " + DeathMonitor.enchanted, MessageLevel.CONFIG);
        
        // owners
        DeathMonitor.ownerFormats.clear();
        for (String name: cfg.getConfigurationSection("owners").getKeys(false)) {
            DeathMonitor.ownerFormats.put(name, cfg.getString("owners." + name));
            Main.messageManager.log("Owner Format for " + name + ": " + DeathMonitor.ownerFormats.get(name), MessageLevel.CONFIG);
        }

        // Entity
        DeathMonitor.entityNames.clear();
        for (String name: cfg.getConfigurationSection("Entity").getKeys(false))
            DeathMonitor.entityNames.put(name, cfg.getString("Entity." + name, name.toLowerCase()));
        
        Main.messageManager.log(DeathMonitor.entityNames.size() + " entity names loaded.", MessageLevel.CONFIG);
        
        // Material
        DeathMonitor.materialNames.clear();
        for (String name: cfg.getConfigurationSection("Material").getKeys(false)) {
            Material material = Material.valueOf(name);
            if (material == null) continue;
            
            DeathMonitor.materialNames.put(material, cfg.getString("Material." + material.name(), material.name().toLowerCase()));
        }
        Main.messageManager.log(DeathMonitor.materialNames.size() + " material names loaded.", MessageLevel.CONFIG);
        
        // MaterialData
        DeathMonitor.materialDataNames.clear();
        for (String entry: cfg.getConfigurationSection("MaterialData").getKeys(false)) {
            Material material = Material.valueOf(entry.split(":")[0]);
            Byte data = Byte.parseByte(entry.split(":")[1]);
            DeathMonitor.materialDataNames.put(new MaterialData(material, data), cfg.getString("MaterialData." + entry));
        }
        Main.messageManager.log(DeathMonitor.materialDataNames.size() + " material data names loaded.", MessageLevel.CONFIG);
    }
}