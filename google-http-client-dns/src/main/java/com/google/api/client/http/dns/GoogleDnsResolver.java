package com.google.api.client.http.dns;

import com.google.api.client.http.DnsResolver;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.gson.Gson;
import java.util.Random;

/**
 * A DNS resolver that resolves IPs using dns.google.com . If the DNS return multiple IPs samples
 * one address with equal probability.
 */
final class GoogleDnsResolver implements DnsResolver {

  private final Gson gson = new Gson();
  private final Random random = new Random();
  private final LoadingCache<String, ImmutableList<InetAddress>> cache;

  public GoogleDnsResolver() {
    cache =
        CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(
                new CacheLoader<String, ImmutableList<InetAddress>>() {
                  public ImmutableList<InetAddress> load(String host) throws Exception {
                    ImmutableList<InetAddress> result = resolveAll(host);
                    return result;
                  }
                });
  }

  /** Resolves a host name to a single IP. */
  @Override
  public InetAddress resolve(String host) throws UnknownHostException {
    try {
      ImmutableList<InetAddress> ips = cache.get(host);
      return ips.get(ips.size() == 0 ? 0 : random.nextInt(ips.size()));
    } catch (ExecutionException e) {
      if (e.getCause() instanceof UnknownHostException) {
        throw (UnknownHostException) e.getCause();
      }
      throw new RuntimeException(e);
    }
  }

  private ImmutableList<InetAddress> resolveAll(String host) throws Exception {
    ImmutableList.Builder<InetAddress> result = ImmutableList.<InetAddress>builder();
    HttpRequestFactory requestFactory = new ApacheHttpTransport().createRequestFactory();
    HttpRequest request =
        requestFactory.buildGetRequest(
            new GenericUrl("https://dns.google.com/resolve?name=" + host));
    GoogleDnsResponse googleDnsResponse =
        gson.fromJson(request.execute().parseAsString().toLowerCase(), GoogleDnsResponse.class);
    for (GoogleDnsAnswer googleDnsAnswer : googleDnsResponse.answer) {
      if (googleDnsAnswer.type == 1) {
        result.add(InetAddress.getByName(googleDnsAnswer.data));
      }
    }

    return result.build();
  }

  static class GoogleDnsResponse {
    GoogleDnsAnswer[] answer;

    GoogleDnsResponse() {}
  }

  static class GoogleDnsAnswer {
    String data;
    int type;

    GoogleDnsAnswer() {}
  }
}
