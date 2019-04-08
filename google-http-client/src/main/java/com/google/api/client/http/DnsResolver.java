package com.google.api.client.http;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** A strategy for resolving DNS to IP. */
public interface DnsResolver {

  /** Resolves a host name to a single IP. */
  InetAddress resolve(String host) throws UnknownHostException;
}
