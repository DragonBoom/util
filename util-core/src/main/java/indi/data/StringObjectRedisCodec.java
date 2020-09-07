package indi.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Optional;

import indi.exception.WrapperException;
import indi.io.IOUtils;
import io.lettuce.core.codec.RedisCodec;

/**
 * 
 * 用于redis的转码器
 *
 *<p>通过Java自带的序列化功能，将对象以字符串的格式存入redis
 * 
 * @author DragonBoom
 *
 * @param <T>
 */
public class StringObjectRedisCodec implements RedisCodec<String, Object> {

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return Optional.ofNullable(IOUtils.readBytes(bytes)).map(String::new).get();
    }

    @Override
    public Object decodeValue(ByteBuffer byteBuffer) {
        // deserialize
        byte[] bytes = IOUtils.readBytes(byteBuffer);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new WrapperException(e);
        }
    }

    /**
     * 将key转化为字节数组，以传递给redis
     * 
     * @return ByteBuffer 可直接访问字节数组（占内存，待优化）
     */
    @Override
    public ByteBuffer encodeKey(String key) {
        return ByteBuffer.wrap(key.getBytes());
    }

    /**
     * 
     * @return ByteBuffer 可直接访问字节数组（占内存，待优化）
     */
    @Override
    public ByteBuffer encodeValue(Object value) {
        if (!(value instanceof Serializable)) {
            throw new IllegalArgumentException(value.getClass() + " 不能序列化，请先实现Serializable接口！");
        }
        // serialize
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(value);
        } catch (IOException e) {
            throw new WrapperException(e);
        }

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return ByteBuffer.wrap(byteArray);
    }

}
