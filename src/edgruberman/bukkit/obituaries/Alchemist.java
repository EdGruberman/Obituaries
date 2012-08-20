package edgruberman.bukkit.obituaries;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Alchemist implements Listener {

    private final Coroner coroner;
    private final Map<Entity, String> potions = new HashMap<Entity, String>();

    Alchemist(final Coroner coroner) {
        this.coroner = coroner;
        coroner.plugin.getServer().getPluginManager().registerEvents(this, coroner.plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionSplash(final PotionSplashEvent splash) {
        for (final PotionEffect effect : splash.getPotion().getEffects()) {
            if (!effect.getType().equals(PotionEffectType.HARM)) continue;

            for (final Entity affected : splash.getAffectedEntities()) {
                if (!(affected instanceof Player)) continue;

                final String potion = this.coroner.translator.formatName(splash.getPotion());
                final String thrower = this.coroner.translator.formatName(splash.getPotion().getShooter());
                final String thrown = MessageFormat.format(this.coroner.translator.potionFormatThrown, potion, thrower);
                this.potions.put(affected, thrown);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        // TODO - use event.isCancelled() when bug is fixed that doesn't check right clicking on air with item returning true
        if (event.useInteractedBlock() == Result.DENY && event.useItemInHand() == Result.DENY) return;

        if (event.getItem() == null || event.getItem().getType() != Material.POTION) return;

        final String potion = this.coroner.translator.formatItem(event.getItem());
        final String drank = MessageFormat.format(this.coroner.translator.potionFormatDrunk, potion);
        this.potions.put(event.getPlayer(), drank);
    }

    String getPotion(final Entity entity) {
        return this.potions.get(entity);
    }

    void remove(final Entity entity) {
        this.potions.remove(entity);
    }

    void clear() {
        this.potions.clear();
    }

}
