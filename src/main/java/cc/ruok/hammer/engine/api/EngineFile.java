package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;
import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class EngineFile {

    protected final File file;
    private final boolean read;
    private final boolean write;

    public EngineFile(File file, Engine engine) {
        this.file = file;
        String path = FileUtil.getAbsolutePath(engine.getWebSite().getPath()) + File.separator;
        if (FileUtil.getAbsolutePath(file).startsWith(path)) {
            read = engine.getWebSite().getPermission("file_read");
            write = engine.getWebSite().getPermission("file_write");
        } else {
            read = engine.getWebSite().getPermission("public_file_read");
            write = engine.getWebSite().getPermission("public_file_write");
        }
    }

    public String getPath() {
        return FileUtil.getAbsolutePath(file.getParentFile());
    }

    public boolean exists() {
        return file.exists();
    }

    public boolean isFile() {
        return file.isFile();
    }

    public boolean isDir() {
        return file.isDirectory();
    }

    public long getSize() throws EngineException {
        if (!read) throw new EngineException("no permission.");
        return file.length();
    }

    public String read(String charset) throws EngineException {
        if (!read) throw new EngineException("no permission.");
        try {
            return FileUtils.readFileToString(file, charset);
        } catch (IOException e) {
            throw new EngineException(e.getMessage());
        }
    }

    public String read() throws EngineException {
        return read("utf-8");
    }

    public void write(String str, String charset) throws EngineException {
        if (!write) throw new EngineException("no permission.");
        try {
            FileUtils.writeStringToFile(file, str, charset);
        } catch (IOException e) {
            throw new EngineException(e.getMessage());
        }
    }

    public void write(String str) throws EngineException {
        write(str, "utf8");
    }

    public void append(String str, String charset) throws EngineException {
        if (!write) throw new EngineException("no permission.");
        try {
            FileUtils.writeStringToFile(file, str, charset, true);
        } catch (IOException e) {
            throw new EngineException(e.getMessage());
        }
    }

    public void append(String str) throws EngineException {
        append(str, "utf8");
    }

    public boolean create() throws EngineException {
        if (!write) throw new EngineException("no permission.");
        try {
            return file.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    public boolean delete() throws EngineException {
        if (!write) throw new EngineException("no permission.");
        return FileUtils.deleteQuietly(file);
    }

    public String getName() {
        if (exists()) return file.getName();
        return null;
    }

    @Override
    public String toString() {
        return FileUtil.getAbsolutePath(file);
    }

}
