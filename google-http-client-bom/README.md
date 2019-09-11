# Google HTTP Client Library Bill of Materials

The `google-http-client-bom` modules is a pom that can be used to import consistent 
versions of `google-http-client` components plus its dependencies.

To use it in Maven, add the following to your `pom.xml`:

[//]: # ({x-version-update-start:google-http-client-bom:released})
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.google.http-client</groupId>
      <artifactId>google-http-client-bom</artifactId>
      <version>1.32.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
[//]: # ({x-version-update-end})

## License

Apache 2.0 - See [LICENSE] for more information.

[LICENSE]: https://github.com/googleapis/google-http-java-client/blob/master/LICENSE
