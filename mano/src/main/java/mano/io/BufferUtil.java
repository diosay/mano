/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.io;

/**
 *
 * @author sixmoon
 */
public class BufferUtil {
    
    public static final byte[] CRLF=new byte[]{'\r', '\n'};
    
    public static int bytesIndexOf(byte[] haystack, int index, int count, byte[] needle) {
        return bytesIndexOf(haystack, index, count, needle, 0, -1);
    }

    public static int bytesIndexOf(byte[] haystack, int index, int count, byte[] needle, int needleIndex, int needleCount) {
        if (index + count > haystack.length) {
            return -1;
        }

        if (needleCount == -1) {
            needleCount = needle.length;
        }

        if (count < needleCount || needleIndex + needleCount > needle.length) {
            return -1;
        }

        for (int i = index; i < count + index; i++) {
            if (haystack[i] == needle[needleIndex]) //找到第一匹配的位置
            {
                for (int j = 0; j < needleCount - 1; j++) //连续匹配
                {
                    if (i + j >= count + index || haystack[i + j] != needle[j + needleIndex]) //如果中途不匹配
                    {
                        return bytesIndexOf(haystack, i + 1, count - (i - index) - 1, needle, needleIndex, needleCount);//从不匹配位置回溯
                    }
                }
                return i;
            }
        }

        return -1;
    }
}
