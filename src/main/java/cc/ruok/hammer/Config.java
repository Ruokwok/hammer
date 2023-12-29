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

    @Override
    public Config clone() {
        Config config = new Config();
        config.name = name;
        config.path = path;
        config.type = type;
        config.domain = new ArrayList<>(domain);
        if (error_page != null) config.error_page = new HashMap<>(error_page);
        return config;
    }
}
