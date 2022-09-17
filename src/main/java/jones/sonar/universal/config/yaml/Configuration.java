package jones.sonar.universal.config.yaml;

import java.util.*;

public final class Configuration {

    static final char SEPARATOR = '.';

    final Map<String, Object> self;

    final Configuration defaults;

    public Configuration(final Configuration defaults) {
        this(new LinkedHashMap<String, Object>(), defaults);
    }

    Configuration(final Map<?, ?> map, Configuration defaults) {
        self = new LinkedHashMap<>();
        this.defaults = defaults;

        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            final String key = (entry.getKey() == null) ? "null" : entry.getKey().toString();

            if (entry.getValue() instanceof Map) {
                self.put(key, new Configuration((Map) entry.getValue(), (defaults == null) ? null : defaults.getSection(key)));
            } else {
                self.put(key, entry.getValue());
            }
        }
    }

    Configuration getSectionFor(final String path) {
        final int index = path.indexOf(SEPARATOR);

        if (index == -1) {
            return this;
        }

        final String root = path.substring(0, index);

        Object section = self.get(root);

        if (section == null) {
            section = new Configuration((defaults == null) ? null : defaults.getSection(root));
            self.put(root, section);
        }

        return (Configuration) section;
    }

    String getChild(final String path) {
        final int index = path.indexOf(SEPARATOR);
        return (index == -1) ? path : path.substring(index + 1);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final String path, final T def) {
        final Configuration section = getSectionFor(path);

        Object val;

        if (section == this) {
            val = self.get(path);
        } else {
            val = section.get(getChild(path), def);
        }

        if (val == null && def instanceof Configuration) {
            self.put(path, def);
        }

        return (val != null) ? (T) val : def;
    }

    public boolean contains(final String path) {
        return get(path, null) != null;
    }

    public Object get(final String path) {
        return get(path, getDefault(path));
    }

    public Object getDefault(final String path) {
        return (defaults == null) ? null : defaults.get(path);
    }

    public void set(final String path, Object value) {
        if (value instanceof Map) {
            value = new Configuration((Map) value, (defaults == null) ? null : defaults.getSection(path));
        }

        final Configuration section = getSectionFor(path);

        if (section == this) {
            if (value == null) {
                self.remove(path);
            } else {
                self.put(path, value);
            }
        } else {
            section.set(getChild(path), value);
        }
    }

    public Configuration getSection(final String path) {
        final Object def = getDefault(path);
        return (Configuration) get(path, (def instanceof Configuration) ? def : new Configuration((defaults == null) ? null : defaults.getSection(path)));
    }

    public int getInt(final String path) {
        Object def = getDefault(path);
        return getInt(path, (def instanceof Number) ? ((Number) def).intValue() : 0);
    }

    public int getInt(final String path, final int def) {
        Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).intValue() : def;
    }

    public boolean getBoolean(final String path) {
        final Object def = getDefault(path);
        return getBoolean(path, (def instanceof Boolean) ? (Boolean) def : false);
    }

    public boolean getBoolean(final String path, final boolean def) {
        final Object val = get(path, def);
        return (val instanceof Boolean) ? (Boolean) val : def;
    }

    public List<Boolean> getBooleanList(final String path) {
        final List<?> list = getList(path);
        final List<Boolean> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Boolean) {
                result.add((Boolean) object);
            }
        }

        return result;
    }

    public String getString(final String path) {
        final Object def = getDefault(path);
        return getString(path, (def instanceof String) ? (String) def : "");
    }

    public String getString(final String path, final String def) {
        final Object val = get(path, def);
        return (val instanceof String) ? (String) val : def;
    }

    public List<String> getStringList(final String path) {
        final List<?> list = getList(path);
        final List<String> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof String) {
                result.add((String) object);
            }
        }

        return result;
    }

    public List<?> getList(final String path) {
        final Object def = getDefault(path);
        return getList(path, (def instanceof List<?>) ? (List<?>) def : Collections.EMPTY_LIST);
    }

    public List<?> getList(final String path, final List<?> def) {
        final Object val = get(path, def);
        return (val instanceof List<?>) ? (List<?>) val : def;
    }
}
