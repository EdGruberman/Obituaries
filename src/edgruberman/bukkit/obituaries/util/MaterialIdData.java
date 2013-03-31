package edgruberman.bukkit.obituaries.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;

public class MaterialIdData {

    /** (<{@link #ID_CODE}>&lt;MATERIAL_ID>|<{@link Material MATERIAL_NAME}>)[<{@link #DELIMITER}>&lt;DATA>] */
    private static final Pattern FORMAT = Pattern.compile(
            "(?:"
                    + "#(\\d+)" // number symbol followed by an integer for the id
                    + "|"       // or
                    + "([^/]*)" // any other characters but the data delimiter
            + ")"
            + "(?:/(\\d+))?"    // optional data value after delimiter
            );

    /**
     * @param value textual representation of a material id/type and data
     * in {@link #FORMAT FORMAT} format
     * @throws IllegalArgumentException for unrecognizable input
     */
    public static MaterialIdData parse(final String input) throws IllegalArgumentException {
        final Matcher parsed = MaterialIdData.FORMAT.matcher(input);
        if (!parsed.find()) throw new IllegalArgumentException("unrecognized Material: " + input);

        final Material material;
        if (parsed.group(1) != null) {
            final int id;
            try {
                id = Integer.valueOf(parsed.group(1));
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("invalid Material id: " + parsed.group(1) + "; input: " + input, e);
            }
            material = Material.getMaterial(id);
            if (material == null) throw new IllegalArgumentException("unrecognized Material id: " + parsed.group(1) + "; input: " + input);
        } else {
            material = Material.matchMaterial(parsed.group(2));
            if (material == null) throw new IllegalArgumentException("unrecognized Material name: " + parsed.group(2) + "; input: " + input);
        }

        Short data = null;
        if (parsed.group(2) != null) {
            try {
                data = Short.valueOf(parsed.group(2));
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("invalid Material data: " + parsed.group(2) + "; input: " + input, e);
            }
        }

        return new MaterialIdData(material.getId(), data);
    }



    private final int id;
    private final Short data;

    public MaterialIdData(final int id, final Short data) {
        this.id = id;
        this.data = data;
    }

    public int getId() {
        return this.id;
    }

    public Short getData() {
        return this.data;
    }

}
