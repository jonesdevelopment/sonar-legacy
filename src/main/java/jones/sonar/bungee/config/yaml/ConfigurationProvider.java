package jones.sonar.bungee.config.yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationProvider {
    private static final Map<Class<? extends ConfigurationProvider>, ConfigurationProvider> PROVIDERS = new HashMap<>();

    static {
        PROVIDERS.put(YamlConfiguration.class, new YamlConfiguration());
    }

    public static ConfigurationProvider getProvider(Class<? extends ConfigurationProvider> provider) {
        return PROVIDERS.get( provider );
    }

    public abstract void save(Configuration config, Writer writer);

    public abstract Configuration load(File file) throws IOException;

    public abstract Configuration load(File file, Configuration defaults) throws IOException;

    public abstract Configuration load(Reader reader, Configuration defaults);

    public abstract Configuration load(InputStream is, Configuration defaults);

    public abstract Configuration load(String string, Configuration defaults);
}
