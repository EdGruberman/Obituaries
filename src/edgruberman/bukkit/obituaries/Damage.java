package edgruberman.bukkit.obituaries;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import edgruberman.bukkit.obituaries.util.JoinList;

public class Damage {

    private static final Map<DamageCause, Class<? extends Damage>> registered = new HashMap<DamageCause, Class<? extends Damage>>();

    public static void register(final DamageCause cause, final Class<? extends Damage> clazz) {
        Damage.registered.put(cause, clazz);
    }

    static Damage create(final Coroner coroner, final EntityDamageEvent damage) throws IllegalArgumentException, SecurityException, InstantiationException
            , IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class<? extends Damage> clazz = Damage.registered.get(damage.getCause());
        if (clazz == null) clazz = Damage.class;
        return clazz.getConstructor(Coroner.class, EntityDamageEvent.class).newInstance(coroner, damage);
    }



    protected final Coroner coroner;
    protected final int recorded;
    protected final DamageCause cause;

    public Damage(final Coroner coroner, final EntityDamageEvent event) {
        this.coroner = coroner;
        this.recorded = event.getEntity().getTicksLived();
        this.cause = event.getCause();
    }

    /** @return age of victim when damage occurred (ticks) */
    public int getRecorded() {
        return this.recorded;
    }

    public boolean isDamagerLiving() {
        return false;
    }

    public DamageCause getCause() {
        return this.cause;
    }

    public String describeAsKiller() {
        return null;
    }

    public String describeAsAccomplice() {
        return this.describeAsKiller();
    }

    public Object getDamager() {
        return this.cause;
    }

    public String formatDeath() {
        final String message = Main.courier.format("deaths." + this.cause.name(), Translator.describeEntity(this.coroner.getPlayer()), this.describeAsKiller());
        return this.formatAccomplices(message);
    }

    // TODO list in order from who did most to least damage
    protected String formatAccomplices(final String message) {
        final List<Object> accomplices = new ArrayList<Object>();
        final JoinList<String> descriptions = new JoinList<String>(Main.courier.getBase().getConfigurationSection("accomplices").getConfigurationSection("list"));

        final Object killer = this.getDamager();
        for (final Damage damage : this.coroner.getDamages()) {
            final Object accomplice = damage.getDamager();
            if (accomplice == null || !(accomplice instanceof UUID) || accomplice.equals(killer)) continue;
            if (accomplices.contains(accomplice)) continue;
            accomplices.add(accomplice);
            final String description = damage.describeAsAccomplice();
            if (description != null) descriptions.add(description);
        }

        if (descriptions.size() == 0) return message;
        final String formatted = Main.courier.format("accomplices.format", message, descriptions);
        return ( formatted != null ? formatted : message );
    }

}
