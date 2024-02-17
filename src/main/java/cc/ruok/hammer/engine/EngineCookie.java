package cc.ruok.hammer.engine;

public class EngineCookie {

    protected String name;
    protected String value;
    protected String path;
    protected String domain;
    protected int age;
    protected boolean httpOnly;

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getValue() {
        return value;
    }

    public String getDomain() {
        return domain;
    }

    public int getMaxAge() {
        return age;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            boolean a = value.equals(obj.toString());
            return a;
        }
        return super.equals(obj);
    }
}
