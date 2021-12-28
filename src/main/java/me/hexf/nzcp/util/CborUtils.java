package me.hexf.nzcp.util;

import com.google.iot.cbor.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CborUtils {
    public static CborObject getMapObject(CborMap map, String key){
        return map.get(key);
    }

    public static CborObject getMapObject(CborMap map, int key) {
        return map.get(CborInteger.create(key));
    }

    public static long getMapNumber(CborMap map, String key){
        return ((CborNumber) getMapObject(map, key)).longValue();
    }

    public static long getMapNumber(CborMap map, int key){
        return ((CborNumber) getMapObject(map, key)).longValue();
    }

    public static byte[] getMapBytes(CborMap map, String key){
        return ((CborByteString) getMapObject(map, key)).byteArrayValue();
    }

    public static byte[] getMapBytes(CborMap map, int key){
        return ((CborByteString) getMapObject(map, key)).byteArrayValue();
    }

    public static UUID getMapBytesAsUuid(CborMap map, String key){
        ByteBuffer byteBuffer = ByteBuffer.wrap(getMapBytes(map, key));
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }

    public static UUID getMapBytesAsUuid(CborMap map, int key){
        ByteBuffer byteBuffer = ByteBuffer.wrap(getMapBytes(map, key));
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }

    public static String getMapBytesAsString(CborMap map, String key){
        return new String(getMapBytes(map, key), StandardCharsets.UTF_8);
    }

    public static String getMapBytesAsString(CborMap map, int key){
        return new String(getMapBytes(map, key), StandardCharsets.UTF_8);
    }
}
