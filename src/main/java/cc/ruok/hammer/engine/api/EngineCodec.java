package cc.ruok.hammer.engine.api;

import cn.hutool.core.codec.Base32;
import cn.hutool.core.codec.Base62;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.HexUtil;

public class EngineCodec {

    public String base32Encode(Object obj) {
        if (obj == null) return null;
        if (obj instanceof EngineFile) {
            return Base32.encode(FileUtil.readBytes(((EngineFile) obj).file));
        } else {
            return Base32.encode(obj.toString());
        }
    }

    public String base32Decode(String str) {
        return Base32.decodeStr(str);
    }

    public String base62Encode(Object obj) {
        if (obj == null) return null;
        if (obj instanceof EngineFile) {
            return Base62.encode(FileUtil.readBytes(((EngineFile) obj).file));
        } else {
            return Base62.encode(obj.toString());
        }
    }

    public String base62Decode(String str) {
        return Base62.decodeStr(str);
    }

    public String base64Encode(Object obj) {
        if (obj == null) return null;
        if (obj instanceof EngineFile) {
            return Base64.encode(FileUtil.readBytes(((EngineFile) obj).file));
        } else {
            return Base64.encode(obj.toString());
        }
    }

    public String base64Decode(String str) {
        return Base64.decodeStr(str);
    }

    public String strToHex(String str) {
        return HexUtil.encodeHexStr(str);
    }

    public String hexToStr(String hex) {
        return HexUtil.decodeHexStr(hex);
    }

}
