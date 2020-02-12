# Changelog

### [1.34.2](https://www.github.com/googleapis/google-http-java-client/compare/v1.34.1...v1.34.2) (2020-02-12)


### Bug Fixes

* use %20 to escpae spaces in URI templates ([#973](https://www.github.com/googleapis/google-http-java-client/issues/973)) ([60ba4ea](https://www.github.com/googleapis/google-http-java-client/commit/60ba4ea771d8ad0a98eddca10a77c5241187d28c))


### Documentation

* bom 4.0.0 ([#970](https://www.github.com/googleapis/google-http-java-client/issues/970)) ([198453b](https://www.github.com/googleapis/google-http-java-client/commit/198453b8b9e0765439ac430deaf10ef9df084665))

### [1.34.1](https://www.github.com/googleapis/google-http-java-client/compare/v1.34.0...v1.34.1) (2020-01-26)


### Bug Fixes

* include '+' in SAFEPATHCHARS_URLENCODER ([#955](https://www.github.com/googleapis/google-http-java-client/issues/955)) ([9384459](https://www.github.com/googleapis/google-http-java-client/commit/9384459015b37e1671aebadc4b8c25dc9e1e033f))
* use random UUID for multipart boundary delimiter ([#916](https://www.github.com/googleapis/google-http-java-client/issues/916)) ([91c20a3](https://www.github.com/googleapis/google-http-java-client/commit/91c20a3dfb654e85104b1c09a0b2befbae356c19))


### Dependencies

* remove unnecessary MySQL dependency ([#943](https://www.github.com/googleapis/google-http-java-client/issues/943)) ([14736ca](https://www.github.com/googleapis/google-http-java-client/commit/14736cab3dc060ea5b60522ea587cfaf66f29699))
* update dependency mysql:mysql-connector-java to v8.0.19 ([#940](https://www.github.com/googleapis/google-http-java-client/issues/940)) ([e76368e](https://www.github.com/googleapis/google-http-java-client/commit/e76368ef9479a3bf06f7c7cb878d4e8e241bb58c))
* update dependency org.apache.httpcomponents:httpcore to v4.4.13 ([#941](https://www.github.com/googleapis/google-http-java-client/issues/941)) ([fd904d2](https://www.github.com/googleapis/google-http-java-client/commit/fd904d26d67b06fac807d38f8fe4141891ef0330))


### Documentation

* fix various paragraph issues in javadoc ([#867](https://www.github.com/googleapis/google-http-java-client/issues/867)) ([029bbbf](https://www.github.com/googleapis/google-http-java-client/commit/029bbbfb5ddfefe64e64ecca4b1413ae1c93ddd8))
* libraries-bom 3.3.0 ([#921](https://www.github.com/googleapis/google-http-java-client/issues/921)) ([7e0b952](https://www.github.com/googleapis/google-http-java-client/commit/7e0b952a0d9c84ac43dff43914567c98f3e81f66))

## [1.34.0](https://www.github.com/googleapis/google-http-java-client/compare/v1.33.0...v1.34.0) (2019-12-17)


### Features

* add option to pass redirect Location: header value as-is without encoding, decoding, or escaping ([#871](https://www.github.com/googleapis/google-http-java-client/issues/871)) ([2c4f49e](https://www.github.com/googleapis/google-http-java-client/commit/2c4f49e0e5f9c6b8f21f35edae373eaada87119b))
* decode uri path components correctly ([#913](https://www.github.com/googleapis/google-http-java-client/issues/913)) ([7d4a048](https://www.github.com/googleapis/google-http-java-client/commit/7d4a048233d0d3e7c0266b7faaac9f61141aeef9)), closes [#398](https://www.github.com/googleapis/google-http-java-client/issues/398)
* support chunked transfer encoding ([#910](https://www.github.com/googleapis/google-http-java-client/issues/910)) ([b8d6abe](https://www.github.com/googleapis/google-http-java-client/commit/b8d6abe0367bd497b68831263753ad262914aa97)), closes [#648](https://www.github.com/googleapis/google-http-java-client/issues/648)


### Bug Fixes

* redirect on 308 (Permanent Redirect) too ([#876](https://www.github.com/googleapis/google-http-java-client/issues/876)) ([501ede8](https://www.github.com/googleapis/google-http-java-client/commit/501ede83ef332207f0ed67c3d7120b20a1416cec))
* set mediaType to null if contentType cannot be parsed ([#911](https://www.github.com/googleapis/google-http-java-client/issues/911)) ([7ea53eb](https://www.github.com/googleapis/google-http-java-client/commit/7ea53ebdb641a9611cbf5736c55f08a83606101e))
* update HttpRequest#getVersion to use stable logic ([#919](https://www.github.com/googleapis/google-http-java-client/issues/919)) ([853ab4b](https://www.github.com/googleapis/google-http-java-client/commit/853ab4ba1bd81420f7b236c2c8f40c4a253a482e)), closes [#892](https://www.github.com/googleapis/google-http-java-client/issues/892)

## [1.32.2](https://www.github.com/googleapis/google-http-java-client/compare/v1.32.1...v1.32.2) (2019-10-29)


### Bug Fixes

* wrap GZIPInputStream for connection reuse ([#840](https://www.github.com/googleapis/google-http-java-client/issues/840)) ([087a428](https://www.github.com/googleapis/google-http-java-client/commit/087a428390a334bd761a8a3d66475aa4dde72ed1)), closes [#749](https://www.github.com/googleapis/google-http-java-client/issues/749) [#367](https://www.github.com/googleapis/google-http-java-client/issues/367)
* HttpResponse GZip content encoding equality change ([#843](https://www.github.com/googleapis/google-http-java-client/issues/843)) ([9c73e1d](https://www.github.com/googleapis/google-http-java-client/commit/9c73e1db7ab371c57ff6246fa39fa514051ef99c)), closes [#842](https://www.github.com/googleapis/google-http-java-client/issues/842) [#842](https://www.github.com/googleapis/google-http-java-client/issues/842) [#842](https://www.github.com/googleapis/google-http-java-client/issues/842) [#842](https://www.github.com/googleapis/google-http-java-client/issues/842) [#842](https://www.github.com/googleapis/google-http-java-client/issues/842)
* use platform default TCP buffer sizes ([#855](https://www.github.com/googleapis/google-http-java-client/issues/855)) ([238f4c5](https://www.github.com/googleapis/google-http-java-client/commit/238f4c52086defc5a055f2e8d91e7450454d5792))



### Documentation

* fix HttpResponseException Markup ([#829](https://www.github.com/googleapis/google-http-java-client/issues/829)) ([99d64e0](https://www.github.com/googleapis/google-http-java-client/commit/99d64e0d88bdcc3b00d54ee9370e052e5f949680))
* include HTTP Transport page in navigation, add support page ([#854](https://www.github.com/googleapis/google-http-java-client/issues/854)) ([57fd1d8](https://www.github.com/googleapis/google-http-java-client/commit/57fd1d859dad486b37b4b4c4ccda5c7f8fa1b356))
* remove theme details ([#859](https://www.github.com/googleapis/google-http-java-client/issues/859)) ([eee85cd](https://www.github.com/googleapis/google-http-java-client/commit/eee85cd8aaaacd6e38271841a6eafe27a0c9d6ec))
* update libraries-bom to 2.7.1 in setup ([#857](https://www.github.com/googleapis/google-http-java-client/issues/857)) ([cc2ea16](https://www.github.com/googleapis/google-http-java-client/commit/cc2ea1697aceb5d3693b02fa87b0f8379f5d7a2b))
* use libraries-bom 2.6.0 in setup instructions ([#847](https://www.github.com/googleapis/google-http-java-client/issues/847)) ([5253c6c](https://www.github.com/googleapis/google-http-java-client/commit/5253c6c5e2b2312206000fd887fe6f0d89a26570))


### Dependencies

* update dependency com.fasterxml.jackson.core:jackson-core to v2.10.0 ([#831](https://www.github.com/googleapis/google-http-java-client/issues/831)) ([ffb1a85](https://www.github.com/googleapis/google-http-java-client/commit/ffb1a857a31948472b2b62ff4f47905fa60fe1e2))
* update dependency com.fasterxml.jackson.core:jackson-core to v2.9.10 ([#828](https://www.github.com/googleapis/google-http-java-client/issues/828)) ([15ba3c3](https://www.github.com/googleapis/google-http-java-client/commit/15ba3c3f7cee9e2e5362d69c1278f45531e56581))
* update dependency com.google.code.gson:gson to v2.8.6 ([#833](https://www.github.com/googleapis/google-http-java-client/issues/833)) ([6c50997](https://www.github.com/googleapis/google-http-java-client/commit/6c50997361fee875d6b7e6db90e70d41622fc04c))
* update dependency mysql:mysql-connector-java to v8.0.18 ([#839](https://www.github.com/googleapis/google-http-java-client/issues/839)) ([1522eb5](https://www.github.com/googleapis/google-http-java-client/commit/1522eb5c011b4f20199e2ec8cb5ec58d10cc399a))

### [1.32.1](https://www.github.com/googleapis/google-http-java-client/compare/v1.32.0...v1.32.1) (2019-09-20)


### Dependencies

* update dependency com.google.protobuf:protobuf-java to v3.10.0 ([#824](https://www.github.com/googleapis/google-http-java-client/issues/824)) ([c51b62f](https://www.github.com/googleapis/google-http-java-client/commit/c51b62f))
* update guava to 28.1-android ([#817](https://www.github.com/googleapis/google-http-java-client/issues/817)) ([e05b6a8](https://www.github.com/googleapis/google-http-java-client/commit/e05b6a8))
