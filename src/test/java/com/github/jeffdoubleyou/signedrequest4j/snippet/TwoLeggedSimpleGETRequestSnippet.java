package com.github.jeffdoubleyou.signedrequest4j.snippet;

import com.github.jeffdoubleyou.signedrequest4j.HttpResponse;
import com.github.jeffdoubleyou.signedrequest4j.OAuthConsumer;
import com.github.jeffdoubleyou.signedrequest4j.SignedRequest;
import com.github.jeffdoubleyou.signedrequest4j.SignedRequestFactory;

public class TwoLeggedSimpleGETRequestSnippet {

	public static void main(String[] args) throws Exception {

		OAuthConsumer consumer = new OAuthConsumer("consumer_key", "consumer_secret");

		SignedRequest signedRequest = SignedRequestFactory.create(consumer);

		HttpResponse response = signedRequest.doGet("https://github.com/jeffdoubleyou/signedrequest4j", "UTF-8");
		System.out.println(response.getStatusCode());
		System.out.println(response.getHeaders());
		System.out.println(response.getTextBody());

	}

}
