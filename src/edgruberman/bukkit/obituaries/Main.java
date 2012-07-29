package edgruberman.bukkit.obituaries;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static final Version MINIMUM_CONFIGURATION = new Version("2.1.0");

    private Coroner coroner = null;

    @Override
    public void onEnable() {
        this.reloadConfig();

        final Configuration language = this.loadConfig("lang/" + this.getConfig().getString("language") + ".yml", null);
        final Translator translator = new Translator(this, language);
        this.coroner = new Coroner(this, translator);
    }

    @Override
    public void onDisable() {
        this.coroner.clear();
        this.coroner = null;
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        super.reloadConfig();

        final Version version = new Version(this.getConfig().getString("version"));
        if (version.compareTo(Main.MINIMUM_CONFIGURATION) >= 0) {
            this.setLogLevel(this.getConfig().getString("logLevel"));
            return;
        }

        this.archiveConfig("config.yml", version);
        this.saveDefaultConfig();
        super.reloadConfig();
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

    private void extractConfig(final String resource, final boolean replace) {
        final Charset source = Charset.forName("UTF-8");
        final Charset target = Charset.defaultCharset();
        if (target.equals(source)) {
            super.saveResource(resource, replace);
            return;
        }

        final File config = new File(this.getDataFolder(), resource);
        if (config.exists()) return;

        final byte[] buffer = new byte[1024]; int read;
        try {
            final InputStream in = new BufferedInputStream(this.getResource(resource));
            final OutputStream out = new BufferedOutputStream(new FileOutputStream(config));
            while((read = in.read(buffer)) > 0) out.write(target.encode(source.decode(ByteBuffer.wrap(buffer, 0, read))).array());
            out.close(); in.close();

        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not extract configuration file \"" + resource + "\" to " + config.getPath() + "\";" + e.getClass().getName() + ": " + e.getMessage());
        }
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
