package com.google.api.client.http.apache;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import junit.framework.TestCase;

/** Tests {@link ApacheHttpTransport} using custom DNS resolution. */
public class ApacheHttpClientWithCustomDnsResolutionTest extends TestCase {
  public void testConnectWithRoundRobinDnsResolution() throws Exception {

    // Customize DNS resolution.
    DnsResolver dnsResolver = new RoundRobinDnsResolver();

    // Everything else is "as usual".
    HttpRequestFactory requestFactory =
        new ApacheHttpTransport(ApacheHttpTransport.newDefaultHttpClient(), dnsResolver)
            .createRequestFactory();
    HttpRequest request =
        requestFactory.buildGetRequest(new GenericUrl("https://www.googleapis.com/auth/calendar"));
    String rawResponse = request.execute().parseAsString();

    assertEquals(rawResponse, "calendar");
  }
}
