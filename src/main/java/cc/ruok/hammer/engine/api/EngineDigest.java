package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;
import cn.hutool.crypto.digest.DigestUtil;

public class EngineDigest extends EngineAPI {

    public EngineDigest(Engine engine) {
        super(engine);
    }

    public String md5(Object obj) throws EngineException {
        if (obj instanceof EngineFile ef) {
            if (ef.isFile() && ef.exists()) {
                return DigestUtil.md5Hex(ef.readBytes());
            } else {
                return null;
            }
        }
        return DigestUtil.md5Hex(obj.toString());
    }

    public String md5Hex16(Object obj) throws EngineException {
        if (obj instanceof EngineFile ef) {
            if (ef.isFile() && ef.exists()) {
                return DigestUtil.md5Hex16(ef.readBytes());
            } else {
                return null;
            }
        }
        return DigestUtil.md5Hex16(obj.toString());
    }

    public String sha1(Object obj) throws EngineException {
        if (obj instanceof EngineFile ef) {
            if (ef.isFile() && ef.exists()) {
                return DigestUtil.sha1Hex(ef.readBytes());
            } else {
                return null;
            }
        }
        return DigestUtil.sha1Hex(obj.toString());
    }

    public String sha256(Object obj) throws EngineException {
        if (obj instanceof EngineFile ef) {
            if (ef.isFile() && ef.exists()) {
                return DigestUtil.sha256Hex(ef.readBytes());
            } else {
                return null;
            }
        } else if (obj instanceof EngineData ed) {
            return DigestUtil.sha256Hex(ed.getBytes());
        }
        return DigestUtil.sha256Hex(obj.toString());
    }

    public String sha512(Object obj) throws EngineException {
        if (obj instanceof EngineFile ef) {
            if (ef.isFile() && ef.exists()) {
                return DigestUtil.sha512Hex(ef.readBytes());
            } else {
                return null;
            }
        }
        return DigestUtil.sha512Hex(obj.toString());
    }

    @Override
    public String getVarName() {
        return "Digest";
    }
}
