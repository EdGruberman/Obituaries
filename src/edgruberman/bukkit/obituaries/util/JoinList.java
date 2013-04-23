package edgruberman.bukkit.obituaries.util;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.configuration.ConfigurationSection;

/**
 * @author EdGruberman (ed@rjump.com)
 * @version 1.3.0
 */
public class JoinList<T> extends ArrayList<T> {
    private static final long serialVersionUID = 1L;

    protected static final String CONFIG_KEY_FORMAT = "format";
    protected static final String CONFIG_KEY_ITEM = "item";
    protected static final String CONFIG_KEY_DELIMITER = "delimiter";

    protected static final String DEFAULT_FORMAT = "{0}";
    protected static final String DEFAULT_ITEM = "{0}";
    protected static final String DEFAULT_DELIMITER = " ";

    protected static final String EMPTY_ITEMS = "";
    protected static final String RECURSIVE_REPRESENTATION = "{this}";

    protected final String format;
    protected final String item;
    protected final String delimiter;

    public JoinList() {
        this(JoinList.DEFAULT_FORMAT, JoinList.DEFAULT_ITEM, JoinList.DEFAULT_DELIMITER);
    }

    public JoinList(final String format, final String item, final String delimiter) {
        this.format = format;
        this.item = item;
        this.delimiter = delimiter;
    }

    public JoinList(final ConfigurationSection config) {
        this(( config != null ? config.getString(JoinList.CONFIG_KEY_FORMAT, JoinList.DEFAULT_FORMAT) : JoinList.DEFAULT_FORMAT )
                , ( config != null ? config.getString(JoinList.CONFIG_KEY_ITEM, JoinList.DEFAULT_ITEM) : JoinList.DEFAULT_ITEM )
                , ( config != null ? config.getString(JoinList.CONFIG_KEY_DELIMITER, JoinList.DEFAULT_DELIMITER) : JoinList.DEFAULT_DELIMITER ));
    }

    public String getFormat() {
        return this.format;
    }

    public String getItem() {
        return this.item;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    public boolean add(final Object... arguments) {
        return this.add((Object) arguments);
    }

    @Override
    public String toString() {
        final Iterator<T> i = this.iterator();
        if (!i.hasNext()) return MessageFormat.format(this.format, JoinList.EMPTY_ITEMS);

        final MessageFormat item = new MessageFormat(this.item);

        final StringBuilder concatenated = new StringBuilder();
        while (i.hasNext()) {
            final Object o = i.next();

            // prevent recursion
            if (o == this) {
                concatenated.append(JoinList.RECURSIVE_REPRESENTATION);
                continue;
            }

            // expand arguments added with varargs method
            final Object[] arguments = ( o instanceof Object[] ? (Object[]) o : new Object[] { o } );

            final StringBuffer sb = item.format(arguments, new StringBuffer(), new FieldPosition(0));
            concatenated.append(sb);

            if (i.hasNext()) concatenated.append(this.delimiter);
        }

        return MessageFormat.format(this.format, concatenated);
    }

}
