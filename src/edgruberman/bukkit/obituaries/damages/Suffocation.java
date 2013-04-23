package edgruberman.bukkit.obituaries.damages;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import edgruberman.bukkit.obituaries.Accident;
import edgruberman.bukkit.obituaries.Coroner;

public class Suffocation extends Accident {

    // identify block at player's top half since player model will drop to floor before death
    public Suffocation(final Coroner coroner, final EntityDamageEvent damage) {
        super(coroner, damage, ((LivingEntity) damage.getEntity()).getEyeLocation().getBlock().getState());
    }

}
