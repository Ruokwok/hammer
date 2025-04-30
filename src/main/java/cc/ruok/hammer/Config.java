package cc.ruok.hammer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config {

    private File file;

    public String name;
    public String path;
    public String type;
    public List<String> domain;
    public HashMap<String, String> error_page = new HashMap<>();
    public HashMap<String, Boolean> permission;
    public String ssl_keystore;
    public String ssl_password;
    public List<String> pseudo_static;
    public HashMap<String, DatabasePool> database_pool;
    public List<String> protects;
    public HashMap<String, Task> tasks;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public static class DatabasePool {

        public String url;
        public String username;
        public String password;
        public int init_size = 5;
        public int min_size = 5;
        public int max_size = 20;
        public int max_idle_time = 3600;
        public int idle_conn_test = 1800;
    }

    public static class Task {

        public String cron;
        public String script;

    }
}
