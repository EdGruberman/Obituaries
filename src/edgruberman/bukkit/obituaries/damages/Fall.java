package edgruberman.bukkit.obituaries.damages;

import org.bukkit.event.entity.EntityDamageEvent;

import edgruberman.bukkit.obituaries.Attack;
import edgruberman.bukkit.obituaries.Coroner;
import edgruberman.bukkit.obituaries.Damage;
import edgruberman.bukkit.obituaries.Main;

public class Fall extends Damage {

    protected final float fallen;

    public Fall(final Coroner coroner, final EntityDamageEvent event) {
        super(coroner, event);
        this.fallen = event.getEntity().getFallDistance();
    }

    @Override
    public String describeAsKiller() {
        final String distance = Main.courier.format("fall.distance", this.fallen);
        final Attack pusher = this.pusher();
        if (pusher == null) return distance;
        return Main.courier.format("fall.pushed", distance, pusher.describeAsKiller());
    }

    @Override
    public String describeAsAccomplice() {
        final Attack pusher = this.pusher();
        if (pusher == null) return null;
        return this.pusher().describeAsAccomplice();
    }

    @Override
    public Object getDamager() {
        final Attack pusher = this.pusher();
        if (pusher == null) return super.getDamager();
        return this.pusher().getDamager();
    }

    public Attack pusher() {
        final int size = this.coroner.getDamages().size();
        if (size < 2) return null;

        for (int i = size - 2; i >= 0; i--) {
            final Damage damage = this.coroner.getDamages().get(i);
            if (damage instanceof Attack) return (Attack) damage;
        }

        return null;
    }

}
