/* 
 * Copyright 2011 Kazuhiro Sera
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 */
package com.github.seratch.signedrequest4j;

import com.github.seratch.signedrequest4j.pem.PEMReader;
import com.github.seratch.signedrequest4j.pem.PKCS1EncodedKeySpec;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

/**
 * Default implementation of {@link SignedRequest}
 */
class SignedRequestImpl implements SignedRequest {

    /**
     * 2 Legged OAuth Request
     */
    public SignedRequestImpl(
            OAuthRealm realm, OAuthConsumer consumer, SignatureMethod signatureMethod) {
        this(realm, consumer, null, signatureMethod);
    }

    /**
     * 2 Legged OAuth Request
     */
    public SignedRequestImpl(
            OAuthRealm realm, OAuthConsumer consumer, SignatureMethod signatureMethod,
            Map<String, Object> additionalParameters) {
        this(realm, consumer, null, signatureMethod, additionalParameters);
    }

    public SignedRequestImpl(
            OAuthRealm realm, OAuthConsumer consumer, OAuthAccessToken accessToken, SignatureMethod signatureMethod) {
        this.realm = realm;
        this.consumer = consumer;
        this.accessToken = accessToken;
        this.signatureMethod = signatureMethod;
    }

    public SignedRequestImpl(
            OAuthRealm realm, OAuthConsumer consumer, OAuthAccessToken accessToken, SignatureMethod signatureMethod,
            Map<String, Object> additionalParameters) {
        this.realm = realm;
        this.consumer = consumer;
        this.accessToken = accessToken;
        this.signatureMethod = signatureMethod;
        this.additionalParameters = additionalParameters;
    }

    private OAuthRealm realm;

    private OAuthConsumer consumer;

    private OAuthAccessToken accessToken;

    private SignatureMethod signatureMethod;

    private String oAuthVersion = "1.0";

    private Map<String, Object> additionalParameters;

    private String rsaPrivateKeyValue;

    /**
     * {inheritDoc}
     */
    @Override
    public SignedRequest setRsaPrivateKeyValue(String rsaPrivateKeyValue) {
        this.rsaPrivateKeyValue = rsaPrivateKeyValue;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse doDelete(String url) throws IOException {
        return doRequest(url, HttpMethod.DELETE, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse doGet(String url, String charset) throws IOException {
        return doRequest(url, HttpMethod.GET, null, charset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse doHead(String url) throws IOException {
        return doRequest(url, HttpMethod.HEAD, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse doOptions(String url) throws IOException {
        return doRequest(url, HttpMethod.OPTIONS, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse doPost(String url, Map<String, Object> requestParameters, String charset)
            throws IOException {
        return doRequest(url, HttpMethod.POST, requestParameters, charset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse doPut(String url) throws IOException {
        return doRequest(url, HttpMethod.PUT, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse doTrace(String url) throws IOException {
        return doRequest(url, HttpMethod.TRACE, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse doRequest(String url, HttpMethod method, Map<String, Object> requestParameters, String charset)
            throws IOException {
        if (method == HttpMethod.GET && requestParameters != null
                && requestParameters.size() > 0) {
            for (String key : requestParameters.keySet()) {
                String param = key + "=" + requestParameters.get(key);
                url += (url.contains("?") ? "&" : "?") + param;
            }
        }
        HttpURLConnection conn = getHttpURLConnection(url, method);
        if (method == HttpMethod.POST && requestParameters != null
                && requestParameters.size() > 0) {
            OutputStream os = null;
            OutputStreamWriter writer = null;
            try {
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                os = conn.getOutputStream();
                writer = new OutputStreamWriter(os);
                for (String key : requestParameters.keySet()) {
                    writer.append("&");
                    writer.append(getUTF8EncodedValue(key));
                    writer.append("=");
                    writer.append(getUTF8EncodedValue(requestParameters.get(key).toString()));
                }
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Exception e) {
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        HttpResponse response = new HttpResponse();
        response.setStatusCode(conn.getResponseCode());
        response.setHeaders(conn.getHeaderFields());
        try {
            response.setContent(getResponseCotent(conn, charset));
        } catch (IOException e) {
            throw e;
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpURLConnection getHttpURLConnection(String url, HttpMethod method) throws IOException {
        String oAuthNonce = String.valueOf(new SecureRandom().nextLong());
        Long oAuthTimestamp = System.currentTimeMillis() / 1000;
        String signature = getSignature(url, method, oAuthNonce, oAuthTimestamp);
        String authorizationHeader = getAuthorizationHeader(signature,
                oAuthNonce, oAuthTimestamp);
        HttpURLConnection conn = (HttpURLConnection) new URL(url)
                .openConnection();
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent",
                "SignedRequest4J HTTP Fetcher (+https://github.com/seratch/signedrequest4j)");
        conn.setRequestMethod(method.toString());
        conn.setRequestProperty("Authorization", authorizationHeader);
        return conn;
    }

    static class Parameter {

        private final String key;
        private final Object value;

        public Parameter(String key, Object value) {
            this.key = key;
            if (value instanceof NotString) {
                throw new IllegalArgumentException("Invalid parameter value");
            }
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSignatureBaseString(String url, HttpMethod method, String oAuthNonce, Long oAuthTimestamp) {
        StringBuilder baseStringBuf = new StringBuilder();
        StringBuilder normalizedParamsBuf = new StringBuilder();
        for (Parameter param : getNormalizedParameters(oAuthNonce,
                oAuthTimestamp)) {
            if (normalizedParamsBuf.length() > 0) {
                normalizedParamsBuf.append("&");
            }
            normalizedParamsBuf.append(param.getKey());
            normalizedParamsBuf.append("=");
            normalizedParamsBuf.append(param.getValue());
        }
        baseStringBuf.append(OAuthEncoding.encode(method.toString().toUpperCase()));
        baseStringBuf.append("&");
        baseStringBuf.append(OAuthEncoding.encode(OAuthEncoding.normalizeURL(url)));
        baseStringBuf.append("&");
        baseStringBuf.append(OAuthEncoding.encode(normalizedParamsBuf.toString()));
        return baseStringBuf.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSignature(String url, HttpMethod method, String oAuthNonce, Long oAuthTimestamp) {
        String baseString = getSignatureBaseString(url, method, oAuthNonce,
                oAuthTimestamp);
        if (signatureMethod == SignatureMethod.HMAC_SHA1) {
            String algorithm = "HmacSHA1";
            String key = consumer.getConsumerSecret() + "&" +
                    ((accessToken != null && accessToken.getTokenSecret() != null)
                            ? accessToken.getTokenSecret() : "");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), algorithm);
            try {
                Mac mac = Mac.getInstance(algorithm);
                mac.init(keySpec);
                byte[] rawValue = mac.doFinal(baseString.getBytes());
                return Base64.encode(rawValue);
            } catch (NoSuchAlgorithmException e) {
                throw new SignedRequestClientException("Invalid Alogrithm : "
                        + e.getLocalizedMessage());
            } catch (InvalidKeyException e) {
                throw new SignedRequestClientException("Invalid key : "
                        + e.getLocalizedMessage());
            }
        } else if (signatureMethod == SignatureMethod.RSA_SHA1) {
            if (rsaPrivateKeyValue == null || rsaPrivateKeyValue.length() == 0) {
                throw new SignedRequestClientException("RSA Private Key value is required.");
            }
            try {
                PEMReader reader = new PEMReader(new ByteArrayInputStream(
                        this.rsaPrivateKeyValue.getBytes("UTF-8")));
                byte[] bytes = reader.getDerBytes();
                // PEM Reader's native string constructor is for filename.
                KeySpec keySpec = null;
                if (PEMReader.PRIVATE_PKCS1_MARKER.equals(reader.getBeginMarker())) {
                    keySpec = (new PKCS1EncodedKeySpec(bytes)).getKeySpec();
                } else if (PEMReader.PRIVATE_PKCS8_MARKER.equals(reader.getBeginMarker())) {
                    keySpec = new PKCS8EncodedKeySpec(bytes);
                } else {
                    throw new SignedRequestClientException("Invalid PEM file: Unknown marker " +
                            "for private key " + reader.getBeginMarker());
                }
                KeyFactory fac = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = fac.generatePrivate(keySpec);
                Signature signer = Signature.getInstance("SHA1withRSA");
                signer.initSign(privateKey);
                signer.update(baseString.getBytes());
                return Base64.encode(signer.sign());
            } catch (Exception e) {
                throw new SignedRequestClientException("Cannot make a signature(RSA)", e);
            }
        } else if (signatureMethod == SignatureMethod.PLAINTEXT) {
            return baseString;
        } else {
            throw new SignedRequestClientException(
                    "Invalid Signature Method (oauth_signature_method) : "
                            + signatureMethod.toString());
        }
    }

    @Override
    public String getAuthorizationHeader(String signature, String oAuthNonce, Long oAuthTimestamp) {
        StringBuilder buf = new StringBuilder();
        buf.append("OAuth ");
        if (realm != null) {
            buf.append("realm=\"");
            buf.append(OAuthEncoding.encode(realm));
            buf.append("\",");
        }
        if (accessToken != null) {
            buf.append("oauth_token=\"");
            buf.append(OAuthEncoding.encode(accessToken.getToken()));
            buf.append("\",");
        }
        buf.append("oauth_consumer_key=\"");
        buf.append(OAuthEncoding.encode(consumer.getConsumerKey()));
        buf.append("\",");
        buf.append("oauth_signature_method=\"");
        buf.append(OAuthEncoding.encode(signatureMethod));
        buf.append("\",");
        buf.append("oauth_signature=\"");
        buf.append(OAuthEncoding.encode(signature));
        buf.append("\",");
        buf.append("oauth_timestamp=\"");
        buf.append(OAuthEncoding.encode(oAuthTimestamp));
        buf.append("\",");
        buf.append("oauth_nonce=\"");
        buf.append(OAuthEncoding.encode(oAuthNonce));
        buf.append("\",");
        buf.append("oauth_version=\"");
        buf.append(OAuthEncoding.encode(oAuthVersion));
        buf.append("\"");
        if (additionalParameters != null && additionalParameters.size() > 0) {
            for (String key : additionalParameters.keySet()) {
                buf.append(",");
                buf.append(OAuthEncoding.encode(key));
                buf.append("=\"");
                buf.append(OAuthEncoding.encode(additionalParameters.get(key)));
                buf.append("\"");
            }
        }
        return buf.toString();
    }

    List<Parameter> getNormalizedParameters(String oAuthNonce, Long oAuthTimestamp) {
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter("oauth_consumer_key", consumer.getConsumerKey()));
        if (accessToken != null) {
            // 2 Legged OAuth does not need
            params.add(new Parameter("oauth_token", accessToken.getToken()));
        }
        params.add(new Parameter("oauth_nonce", oAuthNonce));
        params.add(new Parameter("oauth_signature_method", signatureMethod));
        params.add(new Parameter("oauth_timestamp", oAuthTimestamp));
        params.add(new Parameter("oauth_version", oAuthVersion));
        if (additionalParameters != null && additionalParameters.size() > 0) {
            for (String key : additionalParameters.keySet()) {
                params.add(new Parameter(key, OAuthEncoding.encode(additionalParameters.get(key))));
            }
        }
        Collections.sort(params, new Comparator<Parameter>() {
            public int compare(Parameter p1, Parameter p2) {
                return p1.getKey().compareTo(p2.getKey());
            }
        });
        return params;
    }

    String getResponseCotent(HttpURLConnection conn, String charset) throws IOException {
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = conn.getInputStream();
            Reader isr = (charset != null) ? new InputStreamReader(is, charset)
                    : new InputStreamReader(is);
            br = new BufferedReader(isr);
            StringBuilder buf = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                buf.append(line);
                buf.append("\n");
            }
            return buf.toString();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e2) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e2) {
                }
            }
        }
    }

    String getUTF8EncodedValue(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException tested) {
        }
        return value;
    }

}
