package cc.ruok.hammer.engine;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class EngineFile {

    private final File file;

    public EngineFile(File file) {
        this.file = file;
    }

    public String getPath() {
        return file.getAbsolutePath();
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

    public String read(String charset) throws EngineException {
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
        try {
            FileUtils.writeStringToFile(file, str, charset, true);
        } catch (IOException e) {
            throw new EngineException(e.getMessage());
        }
    }

    public void append(String str) throws EngineException {
        append(str, "utf8");
    }

    public boolean create() {
        try {
            return file.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    public boolean delete() {
        return FileUtils.deleteQuietly(file);
    }

}
