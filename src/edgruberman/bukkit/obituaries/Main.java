package edgruberman.bukkit.obituaries;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.obituaries.commands.Reload;
import edgruberman.bukkit.obituaries.messaging.couriers.ConfigurationCourier;
import edgruberman.bukkit.obituaries.messaging.couriers.TimestampedConfigurationCourier;

public final class Main extends JavaPlugin {

    private static final Version MINIMUM_CONFIGURATION = new Version("2.0.0");

    public static ConfigurationCourier courier;

    private Coroner coroner = null;

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = new TimestampedConfigurationCourier(this, "messages");

        final Configuration language = this.loadConfig(this.getConfig().getString("language") + ".yml", null);
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

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        super.reloadConfig();
        this.setLogLevel(this.getConfig().getString("logLevel"));

        final Version version = new Version(this.getConfig().getString("version"));
        if (version.compareTo(Main.MINIMUM_CONFIGURATION) >= 0) return;

        this.archiveConfig("config.yml", version);
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    @Override
    public void saveDefaultConfig() {
        this.extractConfig("config.yml", false);
    }

    private void archiveConfig(final String resource, final Version version) {
        final String backupName = "%1$s - Archive version %2$s - %3$tY%3$tm%3$tdT%3$tH%3$tM%3$tS.yml";
        final File backup = new File(this.getDataFolder(), String.format(backupName, resource.replaceAll("(?i)\\.yml$", ""), version, new Date()));
        final File existing = new File(this.getDataFolder(), resource);

        if (!existing.renameTo(backup))
            throw new IllegalStateException("Unable to archive configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");

        this.getLogger().warning("Archived configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");
    }

    private void extractConfig(final String resource, final boolean replace) {
        final Charset source = Charset.forName("UTF-8");
        final Charset target = Charset.defaultCharset();
        if (target.equals(source)) {
            super.saveResource(resource, replace);
            return;
        }

        final File config = new File(this.getDataFolder(), resource);
        if (config.exists()) return;

        final char[] cbuf = new char[1024]; int read;
        try {
            final Reader in = new BufferedReader(new InputStreamReader(this.getResource(resource), source));
            final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config), target));
            while((read = in.read(cbuf)) > 0) out.write(cbuf, 0, read);
            out.close(); in.close();

        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not extract configuration file \"" + resource + "\" to " + config.getPath() + "\";" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private Configuration loadConfig(final String resource, final Version required) {
        // Extract default if not existing
        this.extractConfig(resource, false);

        final File existing = new File(this.getDataFolder(), resource);
        final Configuration config = YamlConfiguration.loadConfiguration(existing);
        if (required == null) return config;

        // Verify required or later version
        final Version version = new Version(config.getString("version"));
        if (version.compareTo(required) >= 0) return config;

        this.archiveConfig(resource, version);

        // Extract default and reload
        return this.loadConfig(resource, null);
    }

    private void setLogLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Log level defaulted to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Log level set to: " + this.getLogger().getLevel());
    }

}
