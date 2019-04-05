package com.google.api.client.http.apache;

import com.google.common.collect.ImmutableList;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** A simple DNS resolver that round robins IPs. */
final class RoundRobinDnsResolver implements DnsResolver {

  private final AtomicLong selector = new AtomicLong(1);

  /** Resolves a host name to a single IP. */
  public InetAddress resolve(String host) throws UnknownHostException {

    List<String> availableIpList = resolveAll(host);
    return InetAddress.getByName(
        availableIpList.get((int) ((selector.getAndIncrement()) % availableIpList.size())));
  }

  private List<String> resolveAll(String host) {
    // The real implementation will look up and cache available IPs for a given HOST every TTL
    // seconds, e.g.
    // "curl https://dns.google.com/resolve?name=www.googleapis.com" .
    return ImmutableList.of("172.217.11.234", "172.217.12.10");
  }
}
