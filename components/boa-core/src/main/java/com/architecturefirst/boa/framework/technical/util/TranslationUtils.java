package com.architecturefirst.boa.framework.technical.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.*;

public class TranslationUtils {
    public static final int BUFFER_SIZE = 10000;
    public static final String ENCODING_UTF_8 = "UTF-8";


    public static String translate(String input) {
        try {
            var bytes = translate(input.getBytes());
            return new String(bytes);
        }
        catch(RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] translate(byte[] input) {

        var buffOut = ByteBuffer.allocate(input.length);

        try {
            Deflater compresser = new Deflater();
            compresser.setInput(input);
            compresser.finish();
            int numBytes = compresser.deflate(buffOut);

            var arr = Arrays.copyOf(buffOut.array(), buffOut.position());
            return arr;

//            String b64 = Base64.getEncoder().encodeToString(buffOut.array());
//            return b64.getBytes(ENCODING_UTF_8);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String detranslate(String input, int inputSize) {
        try {
            var bytes = detranslate(input.getBytes(ENCODING_UTF_8), inputSize);
            return new String(bytes);
        }
        catch(RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] detranslate(byte[] input, int inputSize) {

        try {
//            byte[] d64 = Base64.getDecoder().decode(input);

            Inflater decompresser = new Inflater();
            decompresser.setInput(input);

            var resultBuff = ByteBuffer.allocate(inputSize);
            decompresser.inflate(resultBuff);
            decompresser.end();

            var arr = Arrays.copyOf(resultBuff.array(), resultBuff.position());
            return arr;

            //return resultBuff.array();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


