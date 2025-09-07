package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EngineZipUtil extends EngineAPI implements Closeable {

    private ZipFile zipFile;

    public EngineZipUtil(Engine engine) {
        super(engine);
    }

    public void load(String path) throws IOException {
        try {
            this.zipFile = new ZipFile(engine.getWebSite().getPath() + "/" + path);
            engine.addCloseable(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(EngineFile file) throws IOException {
        this.load(engine.getWebSite().getPath() + "/" +  file.file);
    }

    public List<String> list() {
        return zipFile.stream()
                .map(ZipEntry::getName)
                .collect(Collectors.toList());
    }

    public byte[] read(String path) throws IOException {
        return zipFile.getInputStream(zipFile.getEntry(path)).readAllBytes();
    }

    public String readString(String path) throws IOException {
        return new String(read(path));
    }

    @Override
    public String getVarName() {
        return "ZipUtil";
    }

    @Override
    public void close() throws EngineException {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void keep() {
    }

    @Override
    public boolean isKeep() {
        return false;
    }
}
