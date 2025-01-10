package cc.ruok.hammer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config {

    public String name;
    public String path;
    public String type;
    public List<String> domain;
    public HashMap<Integer, String> error_page;
    public HashMap<String, Boolean> permission;
    public String ssl_keystore;
    public String ssl_password;
    public List<String> pseudo_static;
    public HashMap<String, DatabasePool> database_pool;

    @Override
    public Config clone() {
        Config config = new Config();
        config.name = name;
        config.path = path;
        config.type = type;
        config.domain = new ArrayList<>(domain);
        config.permission = new HashMap<>(permission);
        config.ssl_keystore = ssl_keystore;
        config.ssl_password = ssl_password;
        if (error_page != null) config.error_page = new HashMap<>(error_page);
        return config;
    }

    public static class DatabasePool {

        public String url;
        public String username;
        public String password;

    }
}
