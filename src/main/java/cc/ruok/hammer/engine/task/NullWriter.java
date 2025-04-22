package cc.ruok.hammer.engine.task;

import java.io.OutputStream;
import java.io.PrintWriter;

public class NullWriter extends PrintWriter {

    public NullWriter() {
        super(OutputStream.nullOutputStream());
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
