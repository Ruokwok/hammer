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
    public HashMap<Integer, String> error_page;
    public HashMap<String, Boolean> permission;
    public String ssl_keystore;
    public String ssl_password;
    public List<String> pseudo_static;
    public HashMap<String, DatabasePool> database_pool;

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

    }
}
