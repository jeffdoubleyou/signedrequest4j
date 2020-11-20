package com.github.jeffdoubleyou.signedrequest4j.snippet;

import com.github.jeffdoubleyou.signedrequest4j.HttpResponse;
import com.github.jeffdoubleyou.signedrequest4j.OAuthAccessToken;
import com.github.jeffdoubleyou.signedrequest4j.OAuthConsumer;
import com.github.jeffdoubleyou.signedrequest4j.SignedRequest;
import com.github.jeffdoubleyou.signedrequest4j.SignedRequestFactory;

import java.util.HashMap;
import java.util.Map;

public class ThreeLeggedSimplePOSTRequestSnippet {

	public static void main(String[] args) throws Exception {

		OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");
		OAuthAccessToken accessToken = new OAuthAccessToken("token", "token_secret");

		SignedRequest signedRequest = SignedRequestFactory.create(consumer, accessToken);

		Map<String, Object> requestParameters = new HashMap<String, Object>();
		requestParameters.put("something", "updated");

		HttpResponse response = signedRequest.doPost("https://github.com/jeffdoubleyou/signedrequest4j", requestParameters, "UTF-8");
		System.out.println(response.getStatusCode());
		System.out.println(response.getHeaders());
		System.out.println(response.getTextBody());
	}

}
