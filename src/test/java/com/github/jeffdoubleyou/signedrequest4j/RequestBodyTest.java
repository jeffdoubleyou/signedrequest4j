package com.github.jeffdoubleyou.signedrequest4j;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class RequestBodyTest {

	@Test
	public void type() throws Exception {
		assertThat(RequestBody.class, notNullValue());
	}

	@Test
	public void instantiation() throws Exception {
		byte[] body = null;
		String contentType = null;
		RequestBody target = new RequestBody(body, contentType);
		assertThat(target, notNullValue());
	}

}
