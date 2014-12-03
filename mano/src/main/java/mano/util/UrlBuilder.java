/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author junhwong
 */
public class UrlBuilder {

        /**
         * The protocol to use (ftp, http, nntp, ... etc.) .
         *
         * @serial
         */
        private String protocol;
        /**
         * The host name to connect to.
         *
         * @serial
         */
        private String host;
        /**
         * The protocol port to connect to.
         *
         * @serial
         */
        private int port = -1;

        /**
         * The path part of this URL.
         *
         * @serial
         */
        private String path;
        /**
         * The username part of this URL.
         *
         * @serial
         */
        private String username;
        /**
         * The password part of this URL.
         *
         * @serial
         */
        private String password;

        /**
         * # reference.
         *
         * @serial
         */
        private String ref;

        public UrlBuilder() {
        }

        public UrlBuilder(URL url) {
            if (url == null) {
                throw new NullPointerException("url");
            }

            this.setHost(url.getHost());
            this.setPassword(url.getAuthority());
            this.setPath(url.getPath());
            this.setPort(url.getPort());
            this.setProtocol(url.getProtocol());
            this.setQuery(url.getQuery());
            this.setRef(url.getRef());
            this.setUsername(url.getUserInfo());
        }

        public UrlBuilder(String url) throws MalformedURLException {
            this(new URL(url));
        }

        /**
         * @return the protocol
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         * @param protocol the protocol to set
         */
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        /**
         * @return the host
         */
        public String getHost() {
            return host;
        }

        /**
         * @param host the host to set
         */
        public void setHost(String host) {
            this.host = host;
        }

        /**
         * @return the port
         */
        public int getPort() {
            return port;
        }

        /**
         * @param port the port to set
         */
        public void setPort(int port) {
            this.port = port;
        }

        /**
         * @return the query
         */
        public String getQuery() {
            StringBuilder sb = new StringBuilder();
            query.forEach((k, v) -> {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(k).append("=").append(v);
            });
            return sb.toString();
        }
        Map<String, String> query = new HashMap<>();

        /**
         * @param query the query to set
         */
        public void setQuery(String query) {
            this.query.clear();
            if (query == null || "".equals(query)) {
                return;
            }
            if (query.startsWith("?")) {
                query = query.substring(1);
            }
            int index;
            String name;
            String value = "";
            for (String s : query.split("&")) {
                s = s.trim();
                if ("".equals(s)) {
                    continue;
                }
                index = s.indexOf("=");
                if (index > 0) {
                    name = s.substring(0, index).trim();
                    value = s.substring(index + 1).trim();
                } else {
                    name = s;
                }
                if (this.query.containsKey(name)) {
                    this.query.put(name, "".equals(this.query.get(name)) ? value : this.query.get(name) + "," + value);
                } else {
                    this.query.put(name, value);
                }
            }
        }

        public UrlBuilder set(String key, Object value) {
            query.put(key, value == null ? "" : value.toString());
            return this;
        }

        public String get(String key) {
            if (query.containsKey(key)) {
                return query.get(key);
            }
            return null;
        }

        /**
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * @param path the path to set
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * @return the username
         */
        public String getUsername() {
            return username;
        }

        /**
         * @param username the username to set
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * @return the password
         */
        public String getPassword() {
            return password;
        }

        /**
         * @param password the password to set
         */
        public void setPassword(String password) {
            this.password = password;
        }

        /**
         * @return the ref
         */
        public String getRef() {
            return ref;
        }

        /**
         * @param ref the ref to set
         */
        public void setRef(String ref) {
            this.ref = ref;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (this.getProtocol() != null) {
                sb.append(this.getProtocol()).append("://");
            }

            if (this.getHost() != null) {
                sb.append(this.getHost());

                if (this.getPort() > -1) {
                    sb.append(":").append(this.getPort());
                }
            }
            if (this.getPath() != null) {
                if (this.getPath().startsWith("/")) {
                    sb.append(this.getPath());
                } else {
                    sb.append("/").append(this.getPath());
                }
            } else {
                sb.append("/");
            }
            String q = this.getQuery();
            if (q != null && !"".equals(q)) {
                sb.append("?").append(q);
            }
            if (this.getRef() != null) {
                if (this.getRef().startsWith("#")) {
                    sb.append(this.getRef());
                } else {
                    sb.append("#").append(this.getRef());
                }
            }

            return sb.toString();
        }
    }
