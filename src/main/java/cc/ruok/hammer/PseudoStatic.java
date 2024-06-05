package cc.ruok.hammer;

import java.util.LinkedList;

public class PseudoStatic {

    private String origin;
    private String target;
    private int var = 0;
    private LinkedList<String> spl;

    public PseudoStatic(String exp) {
        try {
            String[] split = exp.split(" -> ");
            origin = split[0];
            target = split[1];
            if (origin.contains("{") && origin.contains("}")) {
                int i = 1;
                while (origin.contains("{" + i + "}")) {
                    i++;
                    var++;
                }
                if (var > 0) {
                    String temp = origin;
                    spl = new LinkedList<>();
                    for (int j = 0; j < var; j++) {
                        String v = "{" + (j + 1) + "}";
                        String sub = temp.substring(0, temp.indexOf(v));
                        if (sub != null && !sub.isBlank()) {
                            spl.add(sub);
                        }
                        temp = temp.substring((sub + v).length(), temp.length());
                    }
                }
            }
        } catch (Exception e) {
            origin = null;
        }
    }

    public boolean isValid() {
        return origin != null && origin != null;
    }

    public String getOrigin() {
        return origin;
    }

    public String getTarget() {
        return target;
    }

    public boolean match(String url) {
        if (url.equals(origin)) return true;
        if (spl == null) return false;
        int i = 0;
        for (String s : spl) {
            if (url.contains(s)) {
                int c = url.indexOf(s);
                if (c >= i) {
                    i = c;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public String handler(String url) {
        if (match(url)) {
            if (var == 0) return target;
            String temp = target;
            int offset = 0;
            for (int i = 0; i < var; i++) {
                int end;
                if (spl.size() <= i + 1) {
                    end = url.length();
                } else {
                    end = url.indexOf(spl.get(i + 1));
                }
                int start = spl.get(i).length();
                String var = url.substring(start + offset, end);
                temp = temp.replaceAll("\\{" + (i + 1) + "}", var);
                offset = end;
            }
            return temp;
        }
        return null;
    }

}
