package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Timer;

public class EngineDate {

    private final Engine engine;

    public EngineDate(Engine engine) {
        this.engine = engine;
    }

    public long getTime() {
        return System.currentTimeMillis();
    }

    public String format(String exp, long time) {
        Date date = new Date(time);
        SimpleDateFormat ft = new SimpleDateFormat (exp);
        return ft.format(date);
    }

    public String format(String exp) {
        return format(exp, getTime());
    }

    public int getYear() {
        return LocalDate.now().getYear();
    }

    public int getMonth() {
        return LocalDate.now().getMonthValue();
    }

    public int getDayOfMonth() {
        return LocalDate.now().getDayOfMonth();
    }

    public int getDayOfYear() {
        return LocalDate.now().getDayOfYear();
    }

    public int getDayOfWeek() {
        return LocalDate.now().getDayOfWeek().getValue();
    }


}
