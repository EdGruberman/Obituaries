package edgruberman.bukkit.obituaries;

import org.bukkit.configuration.Configuration;

import edgruberman.bukkit.obituaries.commands.Reload;
import edgruberman.bukkit.obituaries.messaging.ConfigurationCourier;
import edgruberman.bukkit.obituaries.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;

    private Coroner coroner = null;

    @Override
    public void onLoad() {
        this.putConfigMinimum("2.2.0");
        this.putConfigMinimum("en_US.yml", "2.2.2");
    }

    @Override
    public void onEnable() {
        this.reloadConfig();
        final Configuration language = this.loadConfig(this.getConfig().getString("language") + ".yml");
        Main.courier = ConfigurationCourier.Factory.create(this).setBase(language).setFormatCode("format-code").build();

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
