package edgruberman.bukkit.obituaries;

import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static final String MINIMUM_VERSION_CONFIG = "2.0.0b20";

    private Coroner coroner = null;

    @Override
    public void onEnable() {
        final ConfigurationFile config = new ConfigurationFile(this);
        config.setMinVersion(Main.MINIMUM_VERSION_CONFIG);
        config.load();
        this.setLoggingLevel(this.getConfig().getString("logLevel", "INFO"));
        this.start(this, config.getConfig());
    }

    @Override
    public void onDisable() {
        this.coroner.clear();
        this.coroner = null;
    }

    private void start(final Plugin context, final ConfigurationSection config) {
        final Translator translator = new Translator(context, new ConfigurationFile(context, "lang/en_US.yml", "/defaults/lang/en_US.yml").getConfig());
        this.coroner = new Coroner(context, translator);
    }

    private void setLoggingLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final IllegalArgumentException e) {
            this.getLogger().warning("Unrecognized java.util.logging.Level: " + name + "; " + e.getMessage());
            return;
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Logging level set to: " + this.getLogger().getLevel());
    }

}
