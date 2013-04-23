package edgruberman.bukkit.obituaries.damages;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.obituaries.Attack;
import edgruberman.bukkit.obituaries.Coroner;
import edgruberman.bukkit.obituaries.Main;
import edgruberman.bukkit.obituaries.Translator;

public class Magic extends Attack {

    protected final ItemStack consumed;

    public Magic(final Coroner coroner, final EntityDamageEvent event) {
        super(coroner, event);

        if (!(event instanceof EntityDamageByEntityEvent)) {
            // consumed potions come in as EntityDamageEvent for the individual effect
            final ItemStack consumed = coroner.getConsumed(event.getEntity().getTicksLived());
            this.consumed = ( consumed != null && consumed.getType() == Material.POTION ? consumed : null );
        } else {
            this.consumed = null;
        }
    }

    @Override
    public String describeAsKiller() {
        if (this.damager instanceof ThrownPotion) {
            return Translator.describeEntity(this.damager);
        }

        if (this.consumed != null) {
            final String potion = Translator.formatItem(this.consumed);
            final String drunk = Main.courier.format("potion.drunk", potion);
            return drunk != null ? drunk : this.consumed.getType().name();
        }

        return this.damager != null ? this.damager.getClass().getSimpleName() : null;
    }

    @Override
    public UUID getDamager() {
        if (this.damager instanceof ThrownPotion) {
            final ThrownPotion thrown = (ThrownPotion) this.damager;
            return thrown.getShooter().getUniqueId();
        }

        if (this.consumed != null) {
            return this.coroner.getPlayerId();
        }

        return this.damager.getUniqueId();
    }

}
