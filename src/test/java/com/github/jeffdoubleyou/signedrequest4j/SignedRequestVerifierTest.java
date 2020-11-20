package com.github.jeffdoubleyou.signedrequest4j;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SignedRequestVerifierTest {

	@Test
	public void type() throws Exception {
		assertNotNull(SignedRequestVerifier.class);
	}

	@Test
	public void instantiation() throws Exception {
		SignedRequestVerifier target = new SignedRequestVerifier();
		assertNotNull(target);
	}

	@Test
	public void verify_A$String$OAuthConsumer$HttpMethod$SignatureMethod() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_consumer_key=\"admin\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"t7L5ym9EKERK0wauFOJ%2BGlr3O50%3D\"" +
				",oauth_timestamp=\"1301921304\"" +
				",oauth_nonce=\"-1425143696163906497\"" +
				",oauth_version=\"1.0\"";
		OAuthConsumer consumer = new OAuthConsumer("admin", "test");
		boolean actual = SignedRequestVerifier.verify("http://localhost:8080/topics/", "keywords=test",
				authorizationHeader, consumer, HttpMethod.GET, SignatureMethod.HMAC_SHA1);
		boolean expected = true;
		assertEquals(expected, actual);
	}

	@Test
	public void verifyLegacyUncorrected_A$String$OAuthConsumer$HttpMethod$SignatureMethod() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_consumer_key=\"admin\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"JnEDRdkzvNOsox7jnsH4lKvmdEg%3D\"" +
				",oauth_timestamp=\"1301921304\"" +
				",oauth_nonce=\"-1425143696163906497\"" +
				",oauth_version=\"1.0\"";
		OAuthConsumer consumer = new OAuthConsumer("admin", "test");
		boolean actual = SignedRequestVerifier.verifyLegacyUncorrected("http://localhost:8080/topics/?keywords=test",
				authorizationHeader, consumer, HttpMethod.POST, SignatureMethod.HMAC_SHA1);
		boolean expected = true;
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verify_A$String$OAuthConsumer$HttpMethod$SignatureMethod_POST() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_consumer_key=\"admin\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"iok6KjvmOQStq1y4SyovV1%2FghNI%3D\"" +
				",oauth_timestamp=\"1301921304\"" +
				",oauth_nonce=\"-1425143696163906497\"" +
				",oauth_version=\"1.0\"";
		OAuthConsumer consumer = new OAuthConsumer("admin", "test");
		boolean actual = SignedRequestVerifier.verify("http://localhost:8080/topics/", "keywords=test",
				authorizationHeader, consumer, HttpMethod.POST, SignatureMethod.HMAC_SHA1);
		boolean expected = true;
		assertEquals(expected, actual);
	}

	@Test
	public void verify_A$String$OAuthConsumer$OAuthAccessToken$HttpMethod$SignatureMethod() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_token=\"t\"" +
				",oauth_consumer_key=\"admin\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"IdFtVx2dp0IMA7Yh6ZjkZU53SNQ%3D\"" +
				",oauth_timestamp=\"1301923063\"" +
				",oauth_nonce=\"-5989898319858371717\"" +
				",oauth_version=\"1.0\"";
		OAuthConsumer consumer = new OAuthConsumer("admin", "test");
		OAuthAccessToken accessToken = new OAuthAccessToken("t", "ts");
		boolean actual = SignedRequestVerifier.verify("http://localhost:8080/topics/", "keywords=test",
				authorizationHeader, consumer, accessToken, HttpMethod.GET, SignatureMethod.HMAC_SHA1);
		boolean expected = true;
		assertEquals(expected, actual);
	}

	@Test
	public void verify_A$String$OAuthConsumer$HttpMethod$SignatureMethod_AdditionalParameters() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_consumer_key=\"consumer_key\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"lcBCh5KWQXYrL5bpue3rFiCsoUI%3D\"" +
				",oauth_timestamp=\"1272026745\"" +
				",oauth_nonce=\"nonce_value\"" +
				",oauth_version=\"1.0\"" +
				",xoauth_requestor_id=\"user%40example.com\"";
		OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
		boolean actual = SignedRequestVerifier.verify("http://sp.example.com/", "",
				authorizationHeader, consumer, HttpMethod.GET, SignatureMethod.HMAC_SHA1);
		boolean expected = true;
		assertEquals(expected, actual);
	}

	@Test
	public void verify_A$String$OAuthConsumer$OAuthAccessToken$HttpMethod$SignatureMethod_AdditionalParameters() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_token=\"token\"" +
				",oauth_consumer_key=\"consumer_key\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"lgIOndjAGK0aD6D4CxCDcF%2FcvUM%3D\"" +
				",oauth_timestamp=\"1272026745\"" +
				",oauth_nonce=\"nonce_value\"" +
				",oauth_version=\"1.0\"" +
				",xoauth_requestor_id=\"user%40example.com\"";
		OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
		OAuthAccessToken accessToken = new OAuthAccessToken("token", "token_secret");
		boolean actual = SignedRequestVerifier.verify("http://sp.example.com/", "",
				authorizationHeader, consumer, accessToken, HttpMethod.GET, SignatureMethod.HMAC_SHA1);
		boolean expected = true;
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verify_A$String$OAuthConsumer$OAuthAccessToken$HttpMethod$SignatureMethod_POST() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_token=\"token\"" +
				",oauth_consumer_key=\"consumer_key\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"lgIOndjAGK0aD6D4CxCDcF%2FcvUM%3D\"" +
				",oauth_timestamp=\"1272026745\"" +
				",oauth_nonce=\"nonce_value\"" +
				",oauth_version=\"1.0\"";
		OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
		OAuthAccessToken accessToken = new OAuthAccessToken("token", "token_secret");
		SignedRequestVerifier.verify("http://sp.example.com/", "",
				authorizationHeader, consumer, accessToken, HttpMethod.POST, SignatureMethod.HMAC_SHA1);
	}

	@Test
	public void verifyLegacyUncorrected_A$String$OAuthConsumer$OAuthAccessToken$HttpMethod$SignatureMethod() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_token=\"token\"" +
				",oauth_consumer_key=\"consumer_key\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"lgIOndjAGK0aD6D4CxCDcF%2FcvUM%3D\"" +
				",oauth_timestamp=\"1272026745\"" +
				",oauth_nonce=\"nonce_value\"" +
				",oauth_version=\"1.0\"";
		OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
		OAuthAccessToken accessToken = new OAuthAccessToken("token", "token_secret");
		SignedRequestVerifier.verifyLegacyUncorrected("http://sp.example.com/",
				authorizationHeader, consumer, accessToken, HttpMethod.POST, SignatureMethod.HMAC_SHA1);
	}

	@Test
	public void verifyPOST_A$String$OAuthConsumer$OSignatureMethod() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_token=\"token\"" +
				",oauth_consumer_key=\"consumer_key\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"rxRiM4B4SNzN66cdDkfEwe8Yf0I%3D\"" +
				",oauth_timestamp=\"1272026745\"" +
				",oauth_nonce=\"nonce_value\"" +
				",oauth_version=\"1.0\"" +
				",xoauth_requestor_id=\"user%40example.com\"";
		OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
		OAuthAccessToken accessToken = new OAuthAccessToken("token", "token_secret");
		Map<String, String> formParams = new HashMap<String, String>();
		formParams.put("xoauth_requestor_id", "user@example.com");
		boolean actual = SignedRequestVerifier.verifyPOST("http://sp.example.com/", "",
				authorizationHeader, consumer, accessToken, SignatureMethod.HMAC_SHA1, formParams);
		boolean expected = true;
		assertEquals(expected, actual);
	}

	@Test
	public void verifyPOST_A$String$OAuthConsumer$OAuthAccessToken$SignatureMethod() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_consumer_key=\"admin\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"83UFH2qn%2FQeuFjd%2BzSRCv1bou8E%3D\"" +
				",oauth_timestamp=\"1301921304\"" +
				",oauth_nonce=\"-1425143696163906497\"" +
				",oauth_version=\"1.0\"";
		OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
		Map<String, String> formParams = new HashMap<String, String>();
		formParams.put("xoauth_requestor_id", "user@example.com");
		boolean actual = SignedRequestVerifier.verifyPOST("http://sp.example.com/", "",
				authorizationHeader, consumer, SignatureMethod.HMAC_SHA1, formParams);
		boolean expected = true;
		assertEquals(expected, actual);
	}

	@Test
	public void verifyPOST_A$String$OAuthConsumer$OAuthAccessToken$SignatureMethod_empty() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_consumer_key=\"admin\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"DTdPlCeeaEWJctXBhcCNCofj2g4%3D\"" +
				",oauth_timestamp=\"1301921304\"" +
				",oauth_nonce=\"-1425143696163906497\"" +
				",oauth_version=\"1.0\"";
		OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
		Map<String, String> formParams = new HashMap<String, String>();
		formParams.put("xoauth_requestor_id", "");
		boolean actual = SignedRequestVerifier.verifyPOST("http://sp.example.com/", "",
				authorizationHeader, consumer, SignatureMethod.HMAC_SHA1, formParams);
		boolean expected = true;
		assertEquals(expected, actual);
	}

	@Test
	public void verifyPOST_A$String$OAuthConsumer$OAuthAccessToken$SignatureMethod_containsSharp() throws Exception {
		// given
		String authorizationHeader = "OAuth oauth_consumer_key=\"admin\"" +
				",oauth_signature_method=\"HMAC-SHA1\"" +
				",oauth_signature=\"GmTm9aLdpaKTDH3iXsFgDCjc42M%3D\"" +
				",oauth_timestamp=\"1301921304\"" +
				",oauth_nonce=\"-1425143696163906497\"" +
				",oauth_version=\"1.0\"";
		OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
		Map<String, String> formParams = new HashMap<String, String>();
		formParams.put("key", "%val");
		formParams.put("xoauth_requestor_id", "");
		boolean actual = SignedRequestVerifier.verifyPOST("http://sp.example.com/", "",
				authorizationHeader, consumer, SignatureMethod.HMAC_SHA1, formParams);
		boolean expected = true;
		assertEquals(expected, actual);
	}

}
