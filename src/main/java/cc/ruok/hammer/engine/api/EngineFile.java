package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;
import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EngineFile {

    protected final File file;
    private final boolean read;
    private final boolean write;
    private final Engine engine;

    public EngineFile(File file, Engine engine) {
        this.engine = engine;
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

    public ArrayList<EngineFile> list() throws EngineException {
        if (!read) throw new EngineException("no permission.");
        File[] files = file.listFiles();
        if (files == null) return null;
        ArrayList<EngineFile> list = new ArrayList<>();
        for (File f : files) {
            list.add(new EngineFile(f, engine));
        }
        return list;
    }

    public String getSitePath() {
        return FileUtil.getAbsolutePath(file).substring(FileUtil.getAbsolutePath(engine.getWebSite().getPath()).length());
    }

    public boolean isDir() {
        return file.isDirectory();
    }

    public long getSize() throws EngineException {
        if (!read) throw new EngineException("no permission.");
        return file.length();
    }

    public String readString(String charset) throws EngineException {
        if (!read) throw new EngineException("no permission.");
        try {
            return FileUtils.readFileToString(file, charset);
        } catch (IOException e) {
            throw new EngineException(e.getMessage());
        }
    }

    public String readString() throws EngineException {
        return readString("utf-8");
    }

    public EngineData readData() throws EngineException {
        if (!read) throw new EngineException("no permission.");
        try {
            byte[] bytes = FileUtils.readFileToByteArray(file);
            return new EngineData(bytes, engine);
        } catch (IOException e) {
            throw new EngineException(e.getMessage());
        }
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

    public void write(EngineData data) throws EngineException {
        if (!write) throw new EngineException("no permission.");
        try {
            FileUtils.writeByteArrayToFile(file, data.getBytes());
        } catch (IOException e) {
            throw new EngineException(e.getMessage());
        }
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

    public boolean mkdir() throws EngineException {
        if (!write) throw new EngineException("no permission.");
        try {
            FileUtils.forceMkdir(file);
            return true;
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

    public void move(String path) throws EngineException, IOException {
        EngineFile ef = new EngineFile(new File(engine.getWebSite().getPath() + "/" + path), engine);
        if (!ef.write) throw new EngineException("no permission.");
        if (file.isFile()) {
            FileUtils.moveFile(file, ef.file);
        } else {
            FileUtils.moveDirectory(file, ef.file);
        }
    }

    public void copy(String path) throws EngineException, IOException {
        EngineFile ef = new EngineFile(new File(engine.getWebSite().getPath() + "/" + path), engine);
        if (!ef.write) throw new EngineException("no permission.");
        if (file.isFile()) {
            FileUtils.copyFile(file, ef.file);
        } else {
            FileUtils.copyDirectory(file, ef.file);
        }
    }

    @Override
    public String toString() {
        return FileUtil.getAbsolutePath(file);
    }

    public static File getFile(EngineFile file) {
        return file.file;
    }

}