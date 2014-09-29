/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mano.Version;

/**
 * HTTP 协议版本。
 *
 * @author jun <jun@diosay.com>
 */
public final class HttpVersion implements Comparable<HttpVersion> {

    private static final Pattern VERSION_PATTERN
            = Pattern.compile("(\\S+)/(\\d+)\\.(\\d+)");

    private static final String HTTP_1_0_STRING = "HTTP/1.0";
    private static final String HTTP_1_1_STRING = "HTTP/1.1";
    private static final String HTTP_2_0_STRING = "HTTP/2.0";
    
    /**
     * HTTP 1.1
     */
    public static HttpVersion HTTP_11 = new HttpVersion("HTTP", new Version(1, 1));

    /**
     * HTTP 1.0
     */
    public static HttpVersion HTTP_10 = new HttpVersion("HTTP", new Version(1, 0));

    /**
     * HTTP 2.0 (草案)。
     */
    public static HttpVersion HTTP_20 = new HttpVersion("HTTP", new Version(2, 0));

    /**
     * 根据字符串获取 HttpVersion 实例。
     * @param text
     * @return 
     */
    public static HttpVersion valueOf(String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        text = text.trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException("text is empty");
        }

        // Try to match without convert to uppercase first as this is what 99% of all clients
        // will send anyway. Also there is a change to the RFC to make it clear that it is
        // expected to be case-sensitive
        //
        // See:
        // * http://trac.tools.ietf.org/wg/httpbis/trac/ticket/1
        // * http://trac.tools.ietf.org/wg/httpbis/trac/wiki
        //
        HttpVersion version;

        if (HTTP_1_1_STRING.equalsIgnoreCase(text)) {
            version = HttpVersion.HTTP_11;
        } else if (HTTP_1_0_STRING.equalsIgnoreCase(text)) {
            version = HttpVersion.HTTP_10;
        } else if (HTTP_2_0_STRING.equalsIgnoreCase(text)) {
            version = HttpVersion.HTTP_20;
        } else {
            Matcher m = VERSION_PATTERN.matcher(text);
            if (!m.matches()) {
                throw new IllegalArgumentException("invalid version format: " + text);
            }
            version = new HttpVersion(m.group(1).trim().toUpperCase(), new Version(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))));
        }

        return version;
    }

    private final String protocolName;
    private final Version version;
    private final String text;

    private HttpVersion(String name, Version version) {
        this.protocolName = name;
        this.version = version;
        text = protocolName + "/" + this.version.major + "." + this.version.minor;
    }

    /**
     * 获取协议名称。
     *
     * @return
     */
    public String protocolName() {
        return protocolName;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public int hashCode() {
        return protocolName.hashCode() * 31 + version.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HttpVersion other = (HttpVersion) obj;
        if (!Objects.equals(this.protocolName, other.protocolName)) {
            return false;
        }
        return Objects.equals(this.version, other.version);
    }

    @Override
    public int compareTo(HttpVersion o) {
        int val = protocolName.compareTo(o.protocolName);
        if (val != 0) {
            return val;
        }
        return version.compareTo(o.version);
    }
}
