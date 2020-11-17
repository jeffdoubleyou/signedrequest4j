# SignedRequest4J : A Java library supporting OAuth 1.0 signing and verifying

## What's this?

SignedRequest4J is a Java library supporting OAuth 1.0 signing and verifying.

This library supports sending OAuth 1.0 signed HTTP requests and verifying the signature of requests.

With SignedRequest4J, it's so simple to execute 2-legged or 3-legged OAuth 1.0 signed HTTP requests.

### Why update this old thing in 2020?

I am working on something that uses SignedRequest4J to build OAuth 1.0 headers and needs to be updated to use HMAC-SHA256 which was not implemented.  Instead of rewriting everything, I just assumed it would be easier to add HMAC-SHA256 to this repo.  I can't find another library which is capable of simply building a stand-alone OAuth 1.0 HTTP header.

### Why is it not merged upstream?

Project appears to be archived.

### 2-legged OAuth

* Service Provider, Consumer

* a.k.a Signed Fetch, Phone Home in the OpenSocial community

* OAuth Consumer Request 1.0 Draft 1

    <a href="http://oauth.googlecode.com/svn/spec/ext/consumer_request/1.0/drafts/1/spec.html">http://oauth.googlecode.com/svn/spec/ext/consumer_request/1.0/drafts/1/spec.html</a>

```
  Consumer                     Provider
     |                            |
     | consumer_key               |
     | [consumer_secret]          |
     |                            |
     | ---(HTTP)----------------> | <<Verify the signature>>
     | Authorization header       | Authorization header
     |                            | consumer_key
     |                            | [consumer_secret]
     |                            |
     | <----------------(HTTP)--- | <Valid>
     |                     200 OK |
     |                            |
     | <----------------(HTTP)--- | <Invalid>
     |           401 Unauthorized |
     |                            |
```

### 3-legged OAuth

* Service Provider, Consumer, User

* The term "OAuth" mostly means 3-legged OAuth

* OAuth Core 1.0

    <a href="http://oauth.net/core/1.0/#signing_process">http://oauth.net/core/1.0/#signing_process</a>

* RFC 5849: The OAuth 1.0 Protocol

    <a href="http://tools.ietf.org/html/rfc5849">http://tools.ietf.org/html/rfc5849</a>

```
  User          Consumer                     Provider
   |               |                            |
   | ------------> | token                      |
   |               | [token_secret]             |
   |               |                            |
   |               | consumer_key               |
   |               | [consumer_secret]          |
   |               |                            |
   |               | ---(HTTP)----------------> | <<Verify the signature>>
   |               | Authorization header       | Authorization header
   |               |                            | token
   |               |                            | [token_secret]
   |               |                            | consumer_key
   |               |                            | [consumer_secret]
   |               |                            |
   |               | <----------------(HTTP)--- | <Valid>
   |               |                     200 OK |
   |               |                            |
   |               | <----------------(HTTP)--- | <Invalid>
   |               |           401 Unauthorized |
   | <------------ |                            |
   |               |                            |
```


## How to install

### via Maven

```xml
<dependencies>
  <dependency>
    <groupId>com.github.seratch</groupId>
    <artifactId>signedrequest4j</artifactId>
    <version>2.14</version>
  </dependency>
</dependencies>
```

## Snippets

See also: https://github.com/seratch/signedrequest4j/tree/master/src/test/java/com/github/seratch/signedrequest4j/snippet

### 2-legged OAuth instance

```java
import com.github.seratch.signedrequest4j.*;

OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
SignedRequest twoLeggedOAuthRequest = SignedRequestFactory.create(consumer);
```

### 3-legged OAuth instance

```java
OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
OAuthAccessToken accessToken = new OAuthAccessToken("token", "token_secret");
SignedRequest threeLeggedOAuthRequest = SignedRequestFactory.create(consumer, accessToken);
```

### Signature with additional parameters

```java
import java.util.HashMap;
import java.util.Map;

Map<String, Object> additionalParams = new HashMap<String, Object>();
additionalParams.put("xoauth_requestor_id", "user@example.com");

SignedRequest signedRequest2 = SignedRequestFactory.create(consumer, additionalParams);
SignedRequest signedRequest3 = SignedRequestFactory.create(consumer, accessToken, additionalParams);
```

### Signature method HMAC-SHA1 (default)

```java
SignedRequest signedRequest2 = SignedRequestFactory.create(consumer, SignatureMethod.HMAC_SHA1);
```

### Signature method RSA-SHA1

```java
SignedRequest signedRequest = SignedRequestFactory.create(consumer, SignatureMethod.RSA_SHA1);
signedRequest.setRsaPrivateKeyValue("-----BEGIN RSA PRIVATE KEY-----\n...");
```

### Signature method PLAINTEXT

```java
SignedRequest signedRequest = SignedRequestFactory.create(consumer, SignatureMethod.PLAINTEXT);
```

### Getting the signature string

```java
String Url = "http://example.com/";
HttpMethod method = HttpMethod.GET;
String nonce = "nonce_value";
long timestamp = 1272026745L;
String signature = signedRequest.getSignature(url, method, nonce, timestamp);
// -> "K7OrQ7UU+k94LnaezxFs4jBBekc="
```

## Sending requests

### GET

```java
HttpResponse response = signedRequest.doGet("http://example.com/", "UTF-8");
response.getStatusCode(); // -> int
response.getHeaders();    // -> Map<String, String>
response.getBody();       // -> byte[]
response.getTextBody();   // -> String
```

### POST

```java
Map<String, Object> requestParameters = new HashMap<String, Object>();
requestParameters.put("something", "updated");
HttpResponse response = signedRequest.doPost("http://example.com/", requestParameters, "UTF-8");
```

or

```java
byte[] body = "abc".getBytes();
String contentType = "text/plain";
RequestBody reuestBody = new RequestBody(body, contentType);
HttpResponse response = signedRequest.doPost("http://example.com/", reuestBody, "UTF-8");
```

## Verifying the signature of the request

### 2-legged OAuth

```java
String url = "http://localhost/test/";
String queryString = "foo=var";
String authorizationHeader = request.getHeader("Authorization");
OAuthConsumer consumer = new OAuthConsumer("key","secret");

boolean isValid = SignedRequestVerifier.verify(
                    url,
                    queryString,
                    authorizationHeader,
                    consumer,
                    HttpMethod.GET,
                    SignatureMethod.HMAC_SHA1);
```

or

```java
String url = "http://localhost/test/";
String queryString = "foo=var";
String authorizationHeader = request.getHeader("Authorization");
OAuthConsumer consumer = new OAuthConsumer("key","secret");
Map<String, String> formParams = new HashMap<String, String>();
formParams.put("fizz", "buzz");

boolean isValid = SignedRequestVerifier.verifyPOST(
                    url,
                    queryString,
                    authorizationHeader,
                    consumer,
                    SignatureMethod.HMAC_SHA1,
                    formParams);
```

### 3-legged OAuth

```java
String url = "http://localhost/test/";
String queryString = "foo=var";
String authorizationHeader = request.getHeader("Authorization");
OAuthConsumer consumer = new OAuthConsumer("key","secret");
OAuthAccessToken accessToken = new OAuthAccessToken("token", "token_secret");

boolean isValid = SignedRequestVerifier.verify(
                    url,
                    queryString,
                    authorizationHeader,
                    consumer,
                    accessToken,
                    HttpMethod.GET,
                    SignatureMethod.HMAC_SHA1);
```

or

```java
String url = "http://localhost/test/";
String queryString = "foo=var";
String authorizationHeader = request.getHeader("Authorization");
OAuthConsumer consumer = new OAuthConsumer("key","secret");
OAuthAccessToken accessToken = new OAuthAccessToken("token", "token_secret");
Map<String, String> formParams = new HashMap<String, String>();
formParams.put("fizz", "buzz");

boolean isValid = SignedRequestVerifier.verifyPOST(
                    url,
                    queryString,
                    authorizationHeader,
                    consumer,
                    accessToken,
                    SignatureMethod.HMAC_SHA1,
                    formParams);
```




