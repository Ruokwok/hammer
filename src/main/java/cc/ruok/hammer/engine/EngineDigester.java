package cc.ruok.hammer.engine;

import cn.hutool.crypto.digest.DigestUtil;

import java.io.File;

public class EngineDigester {

    public String md5(Object obj) {
        if (obj instanceof EngineFile ef) {
            if (ef.isFile() && ef.exists()) {
                return DigestUtil.md5Hex(ef.file);
            } else {
                return null;
            }
        }
        return DigestUtil.md5Hex(obj.toString());
    }

    public String md5Hex16(Object obj) {
        if (obj instanceof EngineFile ef) {
            if (ef.isFile() && ef.exists()) {
                return DigestUtil.md5Hex16(ef.file);
            } else {
                return null;
            }
        }
        return DigestUtil.md5Hex16(obj.toString());
    }

    public String sha1(Object obj) {
        if (obj instanceof EngineFile ef) {
            if (ef.isFile() && ef.exists()) {
                return DigestUtil.sha1Hex(ef.file);
            } else {
                return null;
            }
        }
        return DigestUtil.sha1Hex(obj.toString());
    }

    public String sha256(Object obj) {
        if (obj instanceof EngineFile ef) {
            if (ef.isFile() && ef.exists()) {
                return DigestUtil.sha256Hex(ef.file);
            } else {
                return null;
            }
        }
        return DigestUtil.sha256Hex(obj.toString());
    }

    public String sha512(Object obj) {
        if (obj instanceof EngineFile ef) {
            if (ef.isFile() && ef.exists()) {
                return DigestUtil.sha512Hex(ef.file);
            } else {
                return null;
            }
        }
        return DigestUtil.sha512Hex(obj.toString());
    }

}
