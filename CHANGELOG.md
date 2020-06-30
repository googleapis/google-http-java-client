# Changelog

## [1.36.0](https://www.github.com/googleapis/google-http-java-client/compare/v1.35.0...v1.36.0) (2020-06-30)


### Features

* add Android 19 compatible FileDataStoreFactory implementation ([#1070](https://www.github.com/googleapis/google-http-java-client/issues/1070)) ([1150acd](https://www.github.com/googleapis/google-http-java-client/commit/1150acd38aa3139eea4f2f718545c20d2493877e))


### Bug Fixes

* restore the thread's interrupted status after catching InterruptedException ([#1005](https://www.github.com/googleapis/google-http-java-client/issues/1005)) ([#1006](https://www.github.com/googleapis/google-http-java-client/issues/1006)) ([0a73a46](https://www.github.com/googleapis/google-http-java-client/commit/0a73a4628b6ec4420db6b9cdbcc68899f3807c5b))

## [1.35.0](https://www.github.com/googleapis/google-http-java-client/compare/v1.34.2...v1.35.0) (2020-04-27)


### Features

* add logic for verifying ES256 JsonWebSignatures ([#1033](https://www.github.com/googleapis/google-http-java-client/issues/1033)) ([bb4227f](https://www.github.com/googleapis/google-http-java-client/commit/bb4227f9daec44fc2976fa9947e2ff5ee07ed21a))


### Bug Fixes

* add linkage monitor plugin ([#1000](https://www.github.com/googleapis/google-http-java-client/issues/1000)) ([027c227](https://www.github.com/googleapis/google-http-java-client/commit/027c227e558164f77be204152fb47023850b543f))
* Correctly handling chunked response streams with gzip ([#990](https://www.github.com/googleapis/google-http-java-client/issues/990)) ([1ba2197](https://www.github.com/googleapis/google-http-java-client/commit/1ba219743e65c89bc3fdb196acc5d2042e01f542)), closes [#367](https://www.github.com/googleapis/google-http-java-client/issues/367)
* FileDataStoreFactory will throw IOException for any permissions errors ([#1012](https://www.github.com/googleapis/google-http-java-client/issues/1012)) ([fd33073](https://www.github.com/googleapis/google-http-java-client/commit/fd33073da3674997897d7a9057d1d0e9d42d7cd4))
* include request method and URL into HttpResponseException message ([#1002](https://www.github.com/googleapis/google-http-java-client/issues/1002)) ([15111a1](https://www.github.com/googleapis/google-http-java-client/commit/15111a1001d6f72cb92cd2d76aaed6f1229bc14a))
* incorrect check for Windows OS in FileDataStoreFactory ([#927](https://www.github.com/googleapis/google-http-java-client/issues/927)) ([8b4eabe](https://www.github.com/googleapis/google-http-java-client/commit/8b4eabe985794fc64ad6a4a53f8f96201cf73fb8))
* reuse reference instead of calling getter twice ([#983](https://www.github.com/googleapis/google-http-java-client/issues/983)) ([1f66222](https://www.github.com/googleapis/google-http-java-client/commit/1f662224d7bee6e27e8d66975fda39feae0c9359)), closes [#982](https://www.github.com/googleapis/google-http-java-client/issues/982)
* **android:** set minimum API level to 19 a.k.a. 4.4 Kit Kat ([#1016](https://www.github.com/googleapis/google-http-java-client/issues/1016)) ([b9a8023](https://www.github.com/googleapis/google-http-java-client/commit/b9a80232c9c8b16a3c3277458835f72e346f6b2c)), closes [#1015](https://www.github.com/googleapis/google-http-java-client/issues/1015)


### Documentation

* android 4.4 or later is required ([#1008](https://www.github.com/googleapis/google-http-java-client/issues/1008)) ([bcc41dd](https://www.github.com/googleapis/google-http-java-client/commit/bcc41dd615af41ae6fb58287931cbf9c2144a075))
* libraries-bom 4.0.1 ([#976](https://www.github.com/googleapis/google-http-java-client/issues/976)) ([fc21dc4](https://www.github.com/googleapis/google-http-java-client/commit/fc21dc412566ef60d23f1f82db5caf3cfd5d447b))
* libraries-bom 4.1.1 ([#984](https://www.github.com/googleapis/google-http-java-client/issues/984)) ([635c813](https://www.github.com/googleapis/google-http-java-client/commit/635c81352ae383b3abfe6d7c141d987a6944b3e9))
* libraries-bom 5.2.0 ([#1032](https://www.github.com/googleapis/google-http-java-client/issues/1032)) ([ca34202](https://www.github.com/googleapis/google-http-java-client/commit/ca34202bfa077adb70313b6c4562c7a5d904e064))
* require Android 4.4 ([#1007](https://www.github.com/googleapis/google-http-java-client/issues/1007)) ([f9d2bb0](https://www.github.com/googleapis/google-http-java-client/commit/f9d2bb030398fe09e3c47b84ea468603355e08e9))


### Dependencies

* httpclient 4.5.12 ([#991](https://www.github.com/googleapis/google-http-java-client/issues/991)) ([79bc1c7](https://www.github.com/googleapis/google-http-java-client/commit/79bc1c76ebd48d396a080ef715b9f07cd056b7ef))
* update to Guava 29 ([#1024](https://www.github.com/googleapis/google-http-java-client/issues/1024)) ([ca9520f](https://www.github.com/googleapis/google-http-java-client/commit/ca9520f2da4babc5bbd28c828da1deb7dbdc87e5))

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
