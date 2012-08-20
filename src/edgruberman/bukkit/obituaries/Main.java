package edgruberman.bukkit.obituaries;

import org.bukkit.configuration.Configuration;

import edgruberman.bukkit.obituaries.commands.Reload;
import edgruberman.bukkit.obituaries.messaging.ConfigurationCourier;
import edgruberman.bukkit.obituaries.messaging.Courier;
import edgruberman.bukkit.obituaries.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static Courier courier;

    private Coroner coroner = null;

    @Override
    public void onLoad() {
        this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "2.2.0");
        this.putConfigMinimum("en_US.yml", "2.2.2");
    }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = new ConfigurationCourier(this);

        final Configuration language = this.loadConfig(this.getConfig().getString("language") + ".yml");
        final Translator translator = new Translator(this, language);
        this.coroner = new Coroner(this, translator);

        this.getCommand("obituaries:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        this.coroner.clear();
        this.coroner = null;
        Main.courier = null;
    }

}
