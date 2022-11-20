package com.architecturefirst.boa.framework.technical.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.zip.*;

public class CompressionUtils {
    public static final int BUFFER_SIZE = 10000;
    public static final String ENCODING_UTF_8 = "UTF-8";


    public static String compress(String input) {
        try {
            var bytes = compress(input.getBytes());
            return new String(bytes);
        }
        catch(RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] compress(byte[] input) {

        var buffOut = ByteBuffer.allocate(input.length);

        try {
            Deflater compresser = new Deflater();
            compresser.setInput(input);
            compresser.finish();
            compresser.deflate(buffOut);

            String b64 = Base64.getEncoder().encodeToString(buffOut.array());
            return b64.getBytes(ENCODING_UTF_8);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decompress(String input, int inputSize) {
        try {
            var bytes = decompress(input.getBytes(ENCODING_UTF_8), inputSize);
            return new String(bytes);
        }
        catch(RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decompress(byte[] input, int inputSize) {

        try {
            byte[] d64 = Base64.getDecoder().decode(input);

            Inflater decompresser = new Inflater();
            decompresser.setInput(d64);

            var resultBuff = ByteBuffer.allocate(inputSize);
            decompresser.inflate(resultBuff);
            decompresser.end();

            return resultBuff.array();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


