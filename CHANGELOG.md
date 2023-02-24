# Changelog

## [1.43.0](https://github.com/googleapis/google-http-java-client/compare/v1.42.3...v1.43.0) (2023-02-24)


### Features

* GsonFactory to have read leniency option via `GsonFactory.builder().setReadLeniency(true).build()` ([00d61b9](https://github.com/googleapis/google-http-java-client/commit/00d61b96dff050ec4b061bead047239b21a48764))
* Next release from main branch is 1.43.0 ([#1764](https://github.com/googleapis/google-http-java-client/issues/1764)) ([9fbae6c](https://github.com/googleapis/google-http-java-client/commit/9fbae6c0721cce7cb4a9042f8fed4823ce291e80))


### Dependencies

* Update dependency com.fasterxml.jackson.core:jackson-core to v2.14.0 ([#1774](https://github.com/googleapis/google-http-java-client/issues/1774)) ([dc41010](https://github.com/googleapis/google-http-java-client/commit/dc410107c98e06531021e5a44ac68ff7621dc47f))
* Update dependency com.fasterxml.jackson.core:jackson-core to v2.14.1 ([#1785](https://github.com/googleapis/google-http-java-client/issues/1785)) ([234e7b5](https://github.com/googleapis/google-http-java-client/commit/234e7b53a1fc2f3b8a8b7a80a4c9fa9118dcbc37))
* Update dependency com.fasterxml.jackson.core:jackson-core to v2.14.2 ([#1810](https://github.com/googleapis/google-http-java-client/issues/1810)) ([23094ff](https://github.com/googleapis/google-http-java-client/commit/23094ffa028acdee63ed868ea070d877f2c5ea95))
* Update dependency com.google.code.gson:gson to v2.10.1 ([#1799](https://github.com/googleapis/google-http-java-client/issues/1799)) ([a114c7e](https://github.com/googleapis/google-http-java-client/commit/a114c7ed815216dccf165fc8763a768892a58723))
* Update dependency com.google.errorprone:error_prone_annotations to v2.18.0 ([#1797](https://github.com/googleapis/google-http-java-client/issues/1797)) ([09f3607](https://github.com/googleapis/google-http-java-client/commit/09f360775001c035d4d26d29f9e28e5f47fb5bd5))
* Update dependency com.google.protobuf:protobuf-java to v3.21.12 ([#1789](https://github.com/googleapis/google-http-java-client/issues/1789)) ([03b5b32](https://github.com/googleapis/google-http-java-client/commit/03b5b321f20543c354447f52669f05a9d1bd00b1))
* Update dependency kr.motd.maven:os-maven-plugin to v1.7.1 ([#1777](https://github.com/googleapis/google-http-java-client/issues/1777)) ([3f318f4](https://github.com/googleapis/google-http-java-client/commit/3f318f44305d9b59aecbdd980abdad525ca47bf3))
* Update dependency org.apache.httpcomponents:httpclient to v4.5.14 ([#1790](https://github.com/googleapis/google-http-java-client/issues/1790)) ([0664e17](https://github.com/googleapis/google-http-java-client/commit/0664e1744e0885a1cb8787481ccfbab0de845fe9))
* Update dependency org.apache.httpcomponents:httpcore to v4.4.16 ([#1787](https://github.com/googleapis/google-http-java-client/issues/1787)) ([512aa23](https://github.com/googleapis/google-http-java-client/commit/512aa2398adf64b89e27b505de03b6e3f2a32875))
* Update project.appengine.version to v2.0.10 ([#1773](https://github.com/googleapis/google-http-java-client/issues/1773)) ([5ddb634](https://github.com/googleapis/google-http-java-client/commit/5ddb634887601bfad64ac482643f65c820b55fd4))

## [1.42.3](https://github.com/googleapis/google-http-java-client/compare/v1.42.2...v1.42.3) (2022-10-27)


### Bug Fixes

* Add @CanIgnoreReturnValue to avoid errorprone errors ([#1716](https://github.com/googleapis/google-http-java-client/issues/1716)) ([cba2f82](https://github.com/googleapis/google-http-java-client/commit/cba2f82b8ff7f4ca44616564accd67f95f08247a))


### Dependencies

* Update actions/checkout action to v3 ([#1719](https://github.com/googleapis/google-http-java-client/issues/1719)) ([6b9585b](https://github.com/googleapis/google-http-java-client/commit/6b9585b0539af6b4631d005a61bb2af60804453a))
* Update dependency cachetools to v5 ([#1732](https://github.com/googleapis/google-http-java-client/issues/1732)) ([7d153d3](https://github.com/googleapis/google-http-java-client/commit/7d153d3c5e92375bb933f6f12d3a2c5df391b34f))
* Update dependency certifi to v2022.9.24 ([#1734](https://github.com/googleapis/google-http-java-client/issues/1734)) ([3b345df](https://github.com/googleapis/google-http-java-client/commit/3b345df3be561bae1e2e4ac4229ab5b66e9b7176))
* Update dependency charset-normalizer to v2.1.1 ([#1738](https://github.com/googleapis/google-http-java-client/issues/1738)) ([a3cbf66](https://github.com/googleapis/google-http-java-client/commit/a3cbf66737a166942c3ac499cae85686fdecd512))
* Update dependency click to v8.1.3 ([#1739](https://github.com/googleapis/google-http-java-client/issues/1739)) ([0b2c204](https://github.com/googleapis/google-http-java-client/commit/0b2c204bb1e16575c82f165803af5f84d46c5c8a))
* Update dependency com.fasterxml.jackson.core:jackson-core to v2.13.4 ([#1718](https://github.com/googleapis/google-http-java-client/issues/1718)) ([394aa98](https://github.com/googleapis/google-http-java-client/commit/394aa98271b02ac62ed35d7040194e8f9c7f41ee))
* Update dependency com.google.code.gson:gson to v2.10 ([#1761](https://github.com/googleapis/google-http-java-client/issues/1761)) ([7d15ad6](https://github.com/googleapis/google-http-java-client/commit/7d15ad6a38e5338c42d972d6bacbd8849c35d851))
* Update dependency com.google.code.gson:gson to v2.9.1 ([#1700](https://github.com/googleapis/google-http-java-client/issues/1700)) ([5c17e2b](https://github.com/googleapis/google-http-java-client/commit/5c17e2ba56ec094a375f986f58867856ba3192cf))
* Update dependency com.google.errorprone:error_prone_annotations to v2.15.0 ([#1701](https://github.com/googleapis/google-http-java-client/issues/1701)) ([0a2e437](https://github.com/googleapis/google-http-java-client/commit/0a2e437017bec6ddf09cff99f535c012a43a5fd6))
* Update dependency com.google.errorprone:error_prone_annotations to v2.16 ([#1755](https://github.com/googleapis/google-http-java-client/issues/1755)) ([1126e53](https://github.com/googleapis/google-http-java-client/commit/1126e53cf6cbcd1170e5ae5a54da31d245115713))
* Update dependency com.google.protobuf:protobuf-java to v3.21.3 ([#1694](https://github.com/googleapis/google-http-java-client/issues/1694)) ([f86112d](https://github.com/googleapis/google-http-java-client/commit/f86112d90ce138dc5cbdca6ddcc50aec3e952740))
* Update dependency com.google.protobuf:protobuf-java to v3.21.4 ([#1698](https://github.com/googleapis/google-http-java-client/issues/1698)) ([fdabd56](https://github.com/googleapis/google-http-java-client/commit/fdabd5672c571c473351ac36248e365f7dd7dcf5))
* Update dependency com.google.protobuf:protobuf-java to v3.21.5 ([#1703](https://github.com/googleapis/google-http-java-client/issues/1703)) ([bdb8cbd](https://github.com/googleapis/google-http-java-client/commit/bdb8cbd83e7c77454e782a7c824e37ef1d011281))
* Update dependency com.google.protobuf:protobuf-java to v3.21.6 ([#1722](https://github.com/googleapis/google-http-java-client/issues/1722)) ([28ee333](https://github.com/googleapis/google-http-java-client/commit/28ee333576e3078a0ad888ee4cc2c664eb8a60e2))
* Update dependency com.google.protobuf:protobuf-java to v3.21.7 ([#1751](https://github.com/googleapis/google-http-java-client/issues/1751)) ([af16206](https://github.com/googleapis/google-http-java-client/commit/af1620620af90f29b12790166b21c9cbb7086ca6))
* Update dependency com.google.protobuf:protobuf-java to v3.21.8 ([#1756](https://github.com/googleapis/google-http-java-client/issues/1756)) ([9119d85](https://github.com/googleapis/google-http-java-client/commit/9119d85b2911747358684b6f8ef83374a44734d7))
* Update dependency com.google.protobuf:protobuf-java to v3.21.9 ([#1762](https://github.com/googleapis/google-http-java-client/issues/1762)) ([02581b8](https://github.com/googleapis/google-http-java-client/commit/02581b8d06d781f6349e6a6d963e20cf66769ef7))
* Update dependency gcp-releasetool to v1.8.8 ([#1735](https://github.com/googleapis/google-http-java-client/issues/1735)) ([f24c984](https://github.com/googleapis/google-http-java-client/commit/f24c98454f46081eb8c9af8809341ebd605b7915))
* Update dependency google-api-core to v2.10.1 ([#1740](https://github.com/googleapis/google-http-java-client/issues/1740)) ([eacf983](https://github.com/googleapis/google-http-java-client/commit/eacf9834fcaa807c891eb6f9bb7957f1830b0b72))
* Update dependency google-auth to v2.12.0 ([#1741](https://github.com/googleapis/google-http-java-client/issues/1741)) ([bfea196](https://github.com/googleapis/google-http-java-client/commit/bfea196499c8989e17c7f90ee025a6a840d75aeb))
* Update dependency google-cloud-core to v2.3.2 ([#1736](https://github.com/googleapis/google-http-java-client/issues/1736)) ([a333e1f](https://github.com/googleapis/google-http-java-client/commit/a333e1f2a2517bcfa51f945d65781fe8a0579676))
* Update dependency google-cloud-storage to v2.5.0 ([#1742](https://github.com/googleapis/google-http-java-client/issues/1742)) ([8335e66](https://github.com/googleapis/google-http-java-client/commit/8335e66f8d175d1669dd02c8ce9007cf6d26eaeb))
* Update dependency google-crc32c to v1.5.0 ([#1743](https://github.com/googleapis/google-http-java-client/issues/1743)) ([3fd3292](https://github.com/googleapis/google-http-java-client/commit/3fd32925fcd3464de74e02a4c7ead5f7469fed8e))
* Update dependency importlib-metadata to v4.12.0 ([#1746](https://github.com/googleapis/google-http-java-client/issues/1746)) ([4658601](https://github.com/googleapis/google-http-java-client/commit/465860164392085b5cfb8d355529565e3f53721a))
* Update dependency jeepney to v0.8.0 ([#1747](https://github.com/googleapis/google-http-java-client/issues/1747)) ([0866e4d](https://github.com/googleapis/google-http-java-client/commit/0866e4dbd882de6385df56ef47e03d56c2c102b1))
* Update dependency jinja2 to v3.1.2 ([#1748](https://github.com/googleapis/google-http-java-client/issues/1748)) ([1507e04](https://github.com/googleapis/google-http-java-client/commit/1507e04d99f6d160f7b0c070d63e2d42dab76c2c))
* Update dependency keyring to v23.9.3 ([#1749](https://github.com/googleapis/google-http-java-client/issues/1749)) ([55bcbd7](https://github.com/googleapis/google-http-java-client/commit/55bcbd7ede201e3a7ed9ee8b8c43510905fd61c5))
* Update dependency markupsafe to v2.1.1 ([#1744](https://github.com/googleapis/google-http-java-client/issues/1744)) ([a62cace](https://github.com/googleapis/google-http-java-client/commit/a62cace610211ca6e9470e5b8e77e42a005733f0))
* Update dependency org.apache.felix:maven-bundle-plugin to v5.1.7 ([#1688](https://github.com/googleapis/google-http-java-client/issues/1688)) ([8bea209](https://github.com/googleapis/google-http-java-client/commit/8bea209c7b23ffb5a57f683ae21889a87f9b7f55))
* Update dependency org.apache.felix:maven-bundle-plugin to v5.1.8 ([#1699](https://github.com/googleapis/google-http-java-client/issues/1699)) ([fa578e0](https://github.com/googleapis/google-http-java-client/commit/fa578e0f7ad6a6c45a0b9de54a936a16a8d345a7))
* Update dependency protobuf to v3.20.2 ([#1745](https://github.com/googleapis/google-http-java-client/issues/1745)) ([3b0fc85](https://github.com/googleapis/google-http-java-client/commit/3b0fc8567e55c26676524d81927feb7a6bd82a2f))
* Update dependency protobuf to v4 ([#1733](https://github.com/googleapis/google-http-java-client/issues/1733)) ([99457dd](https://github.com/googleapis/google-http-java-client/commit/99457dddbd56e7d284d99227990a5a74fdb6e2e9))
* Update dependency pyjwt to v2.5.0 ([#1728](https://github.com/googleapis/google-http-java-client/issues/1728)) ([c285b9a](https://github.com/googleapis/google-http-java-client/commit/c285b9a36bb8b07942f2b7d616b3653465fc2ae2))
* Update dependency requests to v2.28.1 ([#1729](https://github.com/googleapis/google-http-java-client/issues/1729)) ([ee9fc81](https://github.com/googleapis/google-http-java-client/commit/ee9fc81d759f2ebb8a36e0eb36c58f7f634b893f))
* Update dependency typing-extensions to v4.3.0 ([#1730](https://github.com/googleapis/google-http-java-client/issues/1730)) ([f8980a4](https://github.com/googleapis/google-http-java-client/commit/f8980a41fc77eabeba76326fee5553520a95861d))
* Update dependency zipp to v3.8.1 ([#1731](https://github.com/googleapis/google-http-java-client/issues/1731)) ([49477d4](https://github.com/googleapis/google-http-java-client/commit/49477d4207d07bb6dfb00666201f219a01d87d72))
* Update project.appengine.version to v2.0.6 ([#1704](https://github.com/googleapis/google-http-java-client/issues/1704)) ([b33a9c1](https://github.com/googleapis/google-http-java-client/commit/b33a9c173a74e631e9d7e04f51df4370f979da10))
* Update project.appengine.version to v2.0.7 ([#1711](https://github.com/googleapis/google-http-java-client/issues/1711)) ([523a260](https://github.com/googleapis/google-http-java-client/commit/523a2609bef4b2d4a539a327d353e26f61d9a2c2))
* Update project.appengine.version to v2.0.8 ([#1723](https://github.com/googleapis/google-http-java-client/issues/1723)) ([12a455c](https://github.com/googleapis/google-http-java-client/commit/12a455c38b4de3470033be61b06e2beafd911041))
* Update project.appengine.version to v2.0.9 ([#1753](https://github.com/googleapis/google-http-java-client/issues/1753)) ([d047334](https://github.com/googleapis/google-http-java-client/commit/d047334616c9a88b00b20e749d2033fc1a6ca6ca))

## [1.42.2](https://github.com/googleapis/google-http-java-client/compare/v1.42.1...v1.42.2) (2022-07-13)


### Bug Fixes

* enable longpaths support for windows test ([#1485](https://github.com/googleapis/google-http-java-client/issues/1485)) ([#1684](https://github.com/googleapis/google-http-java-client/issues/1684)) ([9d789f5](https://github.com/googleapis/google-http-java-client/commit/9d789f511b907c3970ed9845a4c092fe5458755d))

## [1.42.1](https://github.com/googleapis/google-http-java-client/compare/v1.42.0...v1.42.1) (2022-06-30)


### Dependencies

* update dependency com.google.protobuf:protobuf-java to v3.21.2 ([#1676](https://github.com/googleapis/google-http-java-client/issues/1676)) ([d7638ec](https://github.com/googleapis/google-http-java-client/commit/d7638ec8a3e626790f33f4fb04889fe4dfb31575))

## [1.42.0](https://github.com/googleapis/google-http-java-client/compare/v1.41.7...v1.42.0) (2022-06-09)


### Features

* add build scripts for native image testing in Java 17 ([#1440](https://github.com/googleapis/google-http-java-client/issues/1440)) ([#1666](https://github.com/googleapis/google-http-java-client/issues/1666)) ([05d4019](https://github.com/googleapis/google-http-java-client/commit/05d40193d40097e5a793154a0951f2577fc80f04))
* next release from main branch is 1.42.0 ([#1633](https://github.com/googleapis/google-http-java-client/issues/1633)) ([9acb1ab](https://github.com/googleapis/google-http-java-client/commit/9acb1abaa97392174dd35c5e0e68346f8f653b5b))


### Dependencies

* update dependency com.fasterxml.jackson.core:jackson-core to v2.13.3 ([#1665](https://github.com/googleapis/google-http-java-client/issues/1665)) ([e4f0959](https://github.com/googleapis/google-http-java-client/commit/e4f095997050047d9a6cc20f034f5ef744aefd44))
* update dependency com.google.errorprone:error_prone_annotations to v2.13.0 ([#1630](https://github.com/googleapis/google-http-java-client/issues/1630)) ([bf777b3](https://github.com/googleapis/google-http-java-client/commit/bf777b364c8aafec09c486dc965587eae90549df))
* update dependency com.google.errorprone:error_prone_annotations to v2.13.1 ([#1632](https://github.com/googleapis/google-http-java-client/issues/1632)) ([9e46cd8](https://github.com/googleapis/google-http-java-client/commit/9e46cd85ed1c14161f6473f926802bf281edc4ad))
* update dependency com.google.errorprone:error_prone_annotations to v2.14.0 ([#1667](https://github.com/googleapis/google-http-java-client/issues/1667)) ([3516e18](https://github.com/googleapis/google-http-java-client/commit/3516e185b811d1935eebce31ba65da4813f7e998))
* update dependency com.google.protobuf:protobuf-java to v3.20.1 ([#1639](https://github.com/googleapis/google-http-java-client/issues/1639)) ([90a99e2](https://github.com/googleapis/google-http-java-client/commit/90a99e27b053f5dc6078d6d8cd9bfe150237e2b4))
* update dependency com.google.protobuf:protobuf-java to v3.21.0 ([#1668](https://github.com/googleapis/google-http-java-client/issues/1668)) ([babbe94](https://github.com/googleapis/google-http-java-client/commit/babbe94104710db7b4b428756d7db6c069674ff1))
* update dependency com.google.protobuf:protobuf-java to v3.21.1 ([#1669](https://github.com/googleapis/google-http-java-client/issues/1669)) ([30ec091](https://github.com/googleapis/google-http-java-client/commit/30ec091faea7b5ec9f130cb3fdee396e9923a4b9))
* update dependency org.apache.felix:maven-bundle-plugin to v5.1.6 ([#1643](https://github.com/googleapis/google-http-java-client/issues/1643)) ([8547f5f](https://github.com/googleapis/google-http-java-client/commit/8547f5fff9b27782162b0b6f0db7445c02918a45))
* update project.appengine.version to v2.0.5 ([#1662](https://github.com/googleapis/google-http-java-client/issues/1662)) ([2c82c0d](https://github.com/googleapis/google-http-java-client/commit/2c82c0d4da1162cbc6950cdd6b2f4472b884db13))
* update project.opencensus.version to v0.31.1 ([#1644](https://github.com/googleapis/google-http-java-client/issues/1644)) ([3c65a07](https://github.com/googleapis/google-http-java-client/commit/3c65a07c14d2bf7aa6cce25122df85670955d459))

### [1.41.7](https://github.com/googleapis/google-http-java-client/compare/v1.41.6...v1.41.7) (2022-04-11)


### Dependencies

* revert dependency com.google.protobuf:protobuf-java to v3.19.4 ([#1626](https://github.com/googleapis/google-http-java-client/issues/1626)) ([076433f](https://github.com/googleapis/google-http-java-client/commit/076433f3c233a757f31d5fa39bb6cedbb43b8361))

### [1.41.6](https://github.com/googleapis/google-http-java-client/compare/v1.41.5...v1.41.6) (2022-04-06)


### Bug Fixes

* `Content-Encoding: gzip` along with `Transfer-Encoding: chunked` sometimes terminates early ([#1608](https://github.com/googleapis/google-http-java-client/issues/1608)) ([941da8b](https://github.com/googleapis/google-http-java-client/commit/941da8badf64068d11a53ac57a4ba35b2ad13490))


### Dependencies

* update dependency com.google.errorprone:error_prone_annotations to v2.12.1 ([#1622](https://github.com/googleapis/google-http-java-client/issues/1622)) ([4e1101d](https://github.com/googleapis/google-http-java-client/commit/4e1101d7674cb5715b88a00750cdd5286a9ae077))
* update dependency com.google.protobuf:protobuf-java to v3.20.0 ([#1621](https://github.com/googleapis/google-http-java-client/issues/1621)) ([640dc40](https://github.com/googleapis/google-http-java-client/commit/640dc4080249b65e5cabb7e1ae6cd9cd5b11bd8e))

### [1.41.5](https://github.com/googleapis/google-http-java-client/compare/v1.41.4...v1.41.5) (2022-03-21)


### Documentation

* **deps:** libraries-bom 24.4.0 release ([#1596](https://github.com/googleapis/google-http-java-client/issues/1596)) ([327fe12](https://github.com/googleapis/google-http-java-client/commit/327fe12a122ebb4022a2da55694217233a2badaf))


### Dependencies

* update actions/checkout action to v3 ([#1593](https://github.com/googleapis/google-http-java-client/issues/1593)) ([92002c0](https://github.com/googleapis/google-http-java-client/commit/92002c07d60b738657383e2484f56abc1cde6920))
* update dependency com.fasterxml.jackson.core:jackson-core to v2.13.2 ([#1598](https://github.com/googleapis/google-http-java-client/issues/1598)) ([41ac833](https://github.com/googleapis/google-http-java-client/commit/41ac833249e18cbbd304f825b12202e51bebec85))
* update project.appengine.version to v2 (major) ([#1597](https://github.com/googleapis/google-http-java-client/issues/1597)) ([c06cf95](https://github.com/googleapis/google-http-java-client/commit/c06cf95f9b1be77e2229c3b2f78ece0789eaec15))

### [1.41.4](https://github.com/googleapis/google-http-java-client/compare/v1.41.3...v1.41.4) (2022-02-11)


### Dependencies

* update dependency com.google.code.gson:gson to v2.9.0 ([#1582](https://github.com/googleapis/google-http-java-client/issues/1582)) ([8772778](https://github.com/googleapis/google-http-java-client/commit/877277821dad65545518b06123e6e7b9801147a1))

### [1.41.3](https://github.com/googleapis/google-http-java-client/compare/v1.41.2...v1.41.3) (2022-02-09)


### Dependencies

* update dependency com.google.protobuf:protobuf-java to v3.19.4 ([#1568](https://github.com/googleapis/google-http-java-client/issues/1568)) ([416e5d7](https://github.com/googleapis/google-http-java-client/commit/416e5d7146ad145e3d5140110144b5119c6126df))
* update dependency com.puppycrawl.tools:checkstyle to v9.3 ([#1569](https://github.com/googleapis/google-http-java-client/issues/1569)) ([9c7ade8](https://github.com/googleapis/google-http-java-client/commit/9c7ade85eceb2dc348e1f9aa0637d0509d634160))
* update project.opencensus.version to v0.31.0 ([#1563](https://github.com/googleapis/google-http-java-client/issues/1563)) ([0f9d2b7](https://github.com/googleapis/google-http-java-client/commit/0f9d2b77ae23ea143b5b8caaa21af6548ca92345))

### [1.41.2](https://github.com/googleapis/google-http-java-client/compare/v1.41.1...v1.41.2) (2022-01-27)


### Dependencies

* **java:** update actions/github-script action to v5 ([#1339](https://github.com/googleapis/google-http-java-client/issues/1339)) ([#1561](https://github.com/googleapis/google-http-java-client/issues/1561)) ([c5dbec1](https://github.com/googleapis/google-http-java-client/commit/c5dbec1bbfb5f26f952cb8d80f607327594ab7a8))
* update dependency com.google.errorprone:error_prone_annotations to v2.11.0 ([#1560](https://github.com/googleapis/google-http-java-client/issues/1560)) ([d9609b0](https://github.com/googleapis/google-http-java-client/commit/d9609b00089952d816deffa178640bfcae1f2c3a))

### [1.41.1](https://github.com/googleapis/google-http-java-client/compare/v1.41.0...v1.41.1) (2022-01-21)


### Dependencies

* update dependency com.fasterxml.jackson.core:jackson-core to v2.13.1 ([#1527](https://github.com/googleapis/google-http-java-client/issues/1527)) ([7750398](https://github.com/googleapis/google-http-java-client/commit/7750398d6f4d6e447bfe078092f5cb146f747e50))
* update dependency com.google.protobuf:protobuf-java to v3.19.3 ([#1549](https://github.com/googleapis/google-http-java-client/issues/1549)) ([50c0765](https://github.com/googleapis/google-http-java-client/commit/50c0765f1eadbf7aef2dccf5f78ab62e2533c6f6))
* update dependency com.puppycrawl.tools:checkstyle to v9.2.1 ([#1532](https://github.com/googleapis/google-http-java-client/issues/1532)) ([e13eebd](https://github.com/googleapis/google-http-java-client/commit/e13eebd288afbde3aa7bdc0229c2d0db90ebbd4c))
* update dependency kr.motd.maven:os-maven-plugin to v1.7.0 ([#1547](https://github.com/googleapis/google-http-java-client/issues/1547)) ([8df0dbe](https://github.com/googleapis/google-http-java-client/commit/8df0dbe53521e918985e8f4882392cd2e0a0a1c3))
* update dependency org.apache.felix:maven-bundle-plugin to v5 ([#1548](https://github.com/googleapis/google-http-java-client/issues/1548)) ([ac10b6c](https://github.com/googleapis/google-http-java-client/commit/ac10b6c9fbe4986b8bf130d9f83ae77e84d74e5f))
* update project.appengine.version to v1.9.94 ([#1557](https://github.com/googleapis/google-http-java-client/issues/1557)) ([05c78f4](https://github.com/googleapis/google-http-java-client/commit/05c78f4bee92cc501aa084ad970ed6ac9c0e0444))
* update project.opencensus.version to v0.30.0 ([#1526](https://github.com/googleapis/google-http-java-client/issues/1526)) ([318e54a](https://github.com/googleapis/google-http-java-client/commit/318e54ae9be6bfeb4f5af0af0cb954031d95d1f9))

## [1.41.0](https://www.github.com/googleapis/google-http-java-client/compare/v1.40.1...v1.41.0) (2022-01-05)


### Features

* add AttemptCount to HttpResponseException ([#1505](https://www.github.com/googleapis/google-http-java-client/issues/1505)) ([ea0f6c0](https://www.github.com/googleapis/google-http-java-client/commit/ea0f6c0f58e8abffae1362feb344a9309d6d814e))
* next release from main branch is 1.41.0 ([#1478](https://www.github.com/googleapis/google-http-java-client/issues/1478)) ([3ad4831](https://www.github.com/googleapis/google-http-java-client/commit/3ad4831da00579f534ff7eb7de3a0386068902ba))


### Bug Fixes

* **java:** add -ntp flag to native image testing command ([#1299](https://www.github.com/googleapis/google-http-java-client/issues/1299)) ([#1522](https://www.github.com/googleapis/google-http-java-client/issues/1522)) ([39f63c3](https://www.github.com/googleapis/google-http-java-client/commit/39f63c3ea255fe256391567e66ada7b4122b16f6))
* **java:** java 17 dependency arguments ([#1266](https://www.github.com/googleapis/google-http-java-client/issues/1266)) ([#1489](https://www.github.com/googleapis/google-http-java-client/issues/1489)) ([4a26e18](https://www.github.com/googleapis/google-http-java-client/commit/4a26e1881075a4f361ec746c2444111c911a8d9f))


### Dependencies

* update dependency com.coveo:fmt-maven-plugin to v2.12 ([#1487](https://www.github.com/googleapis/google-http-java-client/issues/1487)) ([8b1b8f2](https://www.github.com/googleapis/google-http-java-client/commit/8b1b8f280774115d0521e0f5eada6dd0ef995ca2))
* update dependency com.google.code.gson:gson to v2.8.9 ([#1492](https://www.github.com/googleapis/google-http-java-client/issues/1492)) ([6615933](https://www.github.com/googleapis/google-http-java-client/commit/6615933e3162969f16d8a0d887afe9f4011e9e5c))
* update dependency com.google.errorprone:error_prone_annotations to v2.10.0 ([#1498](https://www.github.com/googleapis/google-http-java-client/issues/1498)) ([a6a73c2](https://www.github.com/googleapis/google-http-java-client/commit/a6a73c25104aa2074b0a2bcf021513f943c727d4))
* update dependency com.google.protobuf:protobuf-java to v3.19.1 ([#1488](https://www.github.com/googleapis/google-http-java-client/issues/1488)) ([24e6c51](https://www.github.com/googleapis/google-http-java-client/commit/24e6c51112e42f12701b5213a4c5f96466d3f7e2))
* update dependency com.google.protobuf:protobuf-java to v3.19.2 ([#1539](https://www.github.com/googleapis/google-http-java-client/issues/1539)) ([772370a](https://www.github.com/googleapis/google-http-java-client/commit/772370aad7269d30971a38b4471e534d1af9c45a))
* update dependency com.puppycrawl.tools:checkstyle to v9.1 ([#1493](https://www.github.com/googleapis/google-http-java-client/issues/1493)) ([87b980b](https://www.github.com/googleapis/google-http-java-client/commit/87b980b72f7764aae2a1c5f38d321b25ed7471c4))
* update dependency com.puppycrawl.tools:checkstyle to v9.2 ([#1510](https://www.github.com/googleapis/google-http-java-client/issues/1510)) ([0922b67](https://www.github.com/googleapis/google-http-java-client/commit/0922b670e4949ca45b2b25a2d89eea2818349a35))
* update dependency org.apache.httpcomponents:httpcore to v4.4.15 ([#1523](https://www.github.com/googleapis/google-http-java-client/issues/1523)) ([6148d97](https://www.github.com/googleapis/google-http-java-client/commit/6148d9732a7bd745064d68706de75707a9acbb8f))
* update project.appengine.version to v1.9.92 ([#1495](https://www.github.com/googleapis/google-http-java-client/issues/1495)) ([43c3b11](https://www.github.com/googleapis/google-http-java-client/commit/43c3b116a173d639a1214121e21ffea2fc32935c))
* update project.appengine.version to v1.9.93 ([#1516](https://www.github.com/googleapis/google-http-java-client/issues/1516)) ([2fa47c6](https://www.github.com/googleapis/google-http-java-client/commit/2fa47c63e5422bf88fe1320e97e0f61265792d8a))

### [1.40.1](https://www.github.com/googleapis/google-http-java-client/compare/v1.40.0...v1.40.1) (2021-10-07)


### Bug Fixes

* add used packages to OSGI manifest again ([#1439](https://www.github.com/googleapis/google-http-java-client/issues/1439)) ([#1440](https://www.github.com/googleapis/google-http-java-client/issues/1440)) ([59fc8b0](https://www.github.com/googleapis/google-http-java-client/commit/59fc8b03e5518864c60ce4dd47664e8935da343b))
* update NetHttpRequest to prevent silent retry of DELETE requests ([#1472](https://www.github.com/googleapis/google-http-java-client/issues/1472)) ([57ef11a](https://www.github.com/googleapis/google-http-java-client/commit/57ef11a0e1904bb932e5493a30f0a2ca2a2798cf)), closes [#1471](https://www.github.com/googleapis/google-http-java-client/issues/1471)


### Dependencies

* update dependency com.fasterxml.jackson.core:jackson-core to v2.12.5 ([#1437](https://www.github.com/googleapis/google-http-java-client/issues/1437)) ([0ce8467](https://www.github.com/googleapis/google-http-java-client/commit/0ce84676bfbe4cc8e237d5e33dfaa532b13e798c))
* update dependency com.fasterxml.jackson.core:jackson-core to v2.13.0 ([#1469](https://www.github.com/googleapis/google-http-java-client/issues/1469)) ([7d9a042](https://www.github.com/googleapis/google-http-java-client/commit/7d9a042110b8879b592d7e80bd73e77c7a84d8b7))
* update dependency com.google.protobuf:protobuf-java to v3.18.0 ([#1454](https://www.github.com/googleapis/google-http-java-client/issues/1454)) ([cc63e41](https://www.github.com/googleapis/google-http-java-client/commit/cc63e41fac8295c7fea751191a6fe9537c1f70e3))
* update dependency com.google.protobuf:protobuf-java to v3.18.1 ([#1470](https://www.github.com/googleapis/google-http-java-client/issues/1470)) ([c36637a](https://www.github.com/googleapis/google-http-java-client/commit/c36637acbca536992349970664026cf145ad8964))
* update dependency com.puppycrawl.tools:checkstyle to v9 ([#1441](https://www.github.com/googleapis/google-http-java-client/issues/1441)) ([a95cd97](https://www.github.com/googleapis/google-http-java-client/commit/a95cd9717fc8accd80252b12357971cb71887d90))
* update project.appengine.version to v1.9.91 ([#1287](https://www.github.com/googleapis/google-http-java-client/issues/1287)) ([09ebf8d](https://www.github.com/googleapis/google-http-java-client/commit/09ebf8d7e3860f2b94a6fea0ef134c93904d4ed1))

## [1.40.0](https://www.github.com/googleapis/google-http-java-client/compare/v1.39.2...v1.40.0) (2021-08-20)


### Features

* add `gcf-owl-bot[bot]` to `ignoreAuthors` ([#1380](https://www.github.com/googleapis/google-http-java-client/issues/1380)) ([e69275e](https://www.github.com/googleapis/google-http-java-client/commit/e69275ecaa4d85372ebc253dd415a02ba63075be))


### Bug Fixes

* GSON parser now throws IOException on invalid JSON input ([#1355](https://www.github.com/googleapis/google-http-java-client/issues/1355)) ([0a505a7](https://www.github.com/googleapis/google-http-java-client/commit/0a505a7ce012efcce14af94aa130d0eab2ac89b6))
* Add shopt -s nullglob to dependencies script ([#1412](https://www.github.com/googleapis/google-http-java-client/issues/1412)) ([933b0bd](https://www.github.com/googleapis/google-http-java-client/commit/933b0bd386f413bd960f81c706edae81d9dc030a))
* default charset to UTF-8 for text/csv if not specified ([#1423](https://www.github.com/googleapis/google-http-java-client/issues/1423)) ([26f3da4](https://www.github.com/googleapis/google-http-java-client/commit/26f3da4b6426625d0d88afdad525dbf99c65bc8b))
* make depencence on javax.annotation optional ([#1323](https://www.github.com/googleapis/google-http-java-client/issues/1323)) ([#1405](https://www.github.com/googleapis/google-http-java-client/issues/1405)) ([4ccad0e](https://www.github.com/googleapis/google-http-java-client/commit/4ccad0e9f37adaf5adac469e8dec478eb424a410))
* release scripts from issuing overlapping phases ([#1344](https://www.github.com/googleapis/google-http-java-client/issues/1344)) ([539407e](https://www.github.com/googleapis/google-http-java-client/commit/539407ef7133df7f5b1e0f371c673dbc75e79ff2))
* test error responses such as 403 ([#1345](https://www.github.com/googleapis/google-http-java-client/issues/1345)) ([a83c43f](https://www.github.com/googleapis/google-http-java-client/commit/a83c43fa86966ca1be625086a211211e3861f7b1))
* typo ([#1342](https://www.github.com/googleapis/google-http-java-client/issues/1342)) ([2bbc0e4](https://www.github.com/googleapis/google-http-java-client/commit/2bbc0e4b77ab2c9956b0a65af0e927d5052a7752))
* Update dependencies.sh to not break on mac ([933b0bd](https://www.github.com/googleapis/google-http-java-client/commit/933b0bd386f413bd960f81c706edae81d9dc030a))
* Use BufferedInputStream to inspect HttpResponse error ([#1411](https://www.github.com/googleapis/google-http-java-client/issues/1411)) ([33acb86](https://www.github.com/googleapis/google-http-java-client/commit/33acb8621d6e8dc088cf3bd3324a3db25dafb185))


### Documentation

* bom 20.3.0 ([#1368](https://www.github.com/googleapis/google-http-java-client/issues/1368)) ([0d8d2fe](https://www.github.com/googleapis/google-http-java-client/commit/0d8d2fee8750bcaa79f2c8ee106f17b89de81e58))
* libraries-bom 20.1.0 ([#1347](https://www.github.com/googleapis/google-http-java-client/issues/1347)) ([2570889](https://www.github.com/googleapis/google-http-java-client/commit/2570889e95c7c3bf26d5666dc69a7bb09efd7655))
* libraries-bom 20.5.0 ([#1388](https://www.github.com/googleapis/google-http-java-client/issues/1388)) ([38dc3f6](https://www.github.com/googleapis/google-http-java-client/commit/38dc3f64d24868f90bfc9728ace0ce6aaeb2940a))
* libraries-bom 20.9.0 ([#1416](https://www.github.com/googleapis/google-http-java-client/issues/1416)) ([c6aba10](https://www.github.com/googleapis/google-http-java-client/commit/c6aba10ea9a5c5acc9d07317c5b983309b45e2eb))


### Dependencies

* update dependency com.fasterxml.jackson.core:jackson-core to v2.12.3 ([#1340](https://www.github.com/googleapis/google-http-java-client/issues/1340)) ([81e479a](https://www.github.com/googleapis/google-http-java-client/commit/81e479ac59797ad49e503eb2d41ff17c9cb77d7b))
* update dependency com.fasterxml.jackson.core:jackson-core to v2.12.4 ([#1406](https://www.github.com/googleapis/google-http-java-client/issues/1406)) ([fa07715](https://www.github.com/googleapis/google-http-java-client/commit/fa07715f528f74e0ef1c5737c6730c505746a7ad))
* update dependency com.google.code.gson:gson to v2.8.7 ([#1386](https://www.github.com/googleapis/google-http-java-client/issues/1386)) ([550abc1](https://www.github.com/googleapis/google-http-java-client/commit/550abc1e9f3209ec87b20f81c9e0ecdb27aedb7c))
* update dependency com.google.code.gson:gson to v2.8.8 ([#1430](https://www.github.com/googleapis/google-http-java-client/issues/1430)) ([ae4b0db](https://www.github.com/googleapis/google-http-java-client/commit/ae4b0dbbcf2535e660c70dd9ac0ea20d7f040181))
* update dependency com.google.errorprone:error_prone_annotations to v2.7.1 ([#1378](https://www.github.com/googleapis/google-http-java-client/issues/1378)) ([83b1642](https://www.github.com/googleapis/google-http-java-client/commit/83b164245d4e3298c7cee5b10ab7917f6c85e7b1))
* update dependency com.google.errorprone:error_prone_annotations to v2.8.0 ([#1414](https://www.github.com/googleapis/google-http-java-client/issues/1414)) ([1508657](https://www.github.com/googleapis/google-http-java-client/commit/1508657d27b41babb530a914bd2708c567ac08ef))
* update dependency com.google.errorprone:error_prone_annotations to v2.8.1 ([#1420](https://www.github.com/googleapis/google-http-java-client/issues/1420)) ([1f8be1c](https://www.github.com/googleapis/google-http-java-client/commit/1f8be1c222d7f3fd165abe57387d2f8d9e63d82f))
* update dependency com.google.errorprone:error_prone_annotations to v2.9.0 ([#1429](https://www.github.com/googleapis/google-http-java-client/issues/1429)) ([834ade3](https://www.github.com/googleapis/google-http-java-client/commit/834ade362070c9c93f9eb8a08df3308df46d51f2))
* update dependency com.google.protobuf:protobuf-java to v3.16.0 ([#1366](https://www.github.com/googleapis/google-http-java-client/issues/1366)) ([3148f5d](https://www.github.com/googleapis/google-http-java-client/commit/3148f5daab8598957e05849eaec2eab0b634321d))
* update dependency com.google.protobuf:protobuf-java to v3.17.0 ([#1373](https://www.github.com/googleapis/google-http-java-client/issues/1373)) ([d147628](https://www.github.com/googleapis/google-http-java-client/commit/d147628742bbd327a405e87b1645d1d4bf1f7610))
* update dependency com.google.protobuf:protobuf-java to v3.17.1 ([#1384](https://www.github.com/googleapis/google-http-java-client/issues/1384)) ([c22a0e0](https://www.github.com/googleapis/google-http-java-client/commit/c22a0e0e1c1a4a6e8c93b38db519b49eba4e2f14))
* update dependency com.google.protobuf:protobuf-java to v3.17.2 ([#1390](https://www.github.com/googleapis/google-http-java-client/issues/1390)) ([b34349f](https://www.github.com/googleapis/google-http-java-client/commit/b34349f5d303f15b28c69a995763f3842738177c))
* update dependency com.google.protobuf:protobuf-java to v3.17.3 ([#1394](https://www.github.com/googleapis/google-http-java-client/issues/1394)) ([4e3b3c3](https://www.github.com/googleapis/google-http-java-client/commit/4e3b3c3cebeb8439e729a9f99b58e5fc5e13e2cf))

### [1.39.2](https://www.github.com/googleapis/google-http-java-client/compare/v1.39.1...v1.39.2) (2021-04-09)


### Dependencies

* update dependency com.google.errorprone:error_prone_annotations to v2.6.0 ([#1327](https://www.github.com/googleapis/google-http-java-client/issues/1327)) ([3feef0c](https://www.github.com/googleapis/google-http-java-client/commit/3feef0ccd2ca298bdf136da14b4e4b864df423db))
* update dependency com.google.protobuf:protobuf-java to v3.15.7 ([#1329](https://www.github.com/googleapis/google-http-java-client/issues/1329)) ([afbbb3f](https://www.github.com/googleapis/google-http-java-client/commit/afbbb3fe441a41e8f0d4ecdb3f46b798c708a46b))
* update dependency com.google.protobuf:protobuf-java to v3.15.8 ([#1334](https://www.github.com/googleapis/google-http-java-client/issues/1334)) ([e10565a](https://www.github.com/googleapis/google-http-java-client/commit/e10565af31e531f7a1fbd8bbac0a9a69fbef5a80))
* update Guava patch ([#1333](https://www.github.com/googleapis/google-http-java-client/issues/1333)) ([854942a](https://www.github.com/googleapis/google-http-java-client/commit/854942aff7302be77e6f62f9cf7b5dc5e1928c90))

### [1.39.1](https://www.github.com/googleapis/google-http-java-client/compare/v1.39.0...v1.39.1) (2021-03-15)


### Bug Fixes

* default application/json charset to utf-8 ([#1305](https://www.github.com/googleapis/google-http-java-client/issues/1305)) ([c4dfb48](https://www.github.com/googleapis/google-http-java-client/commit/c4dfb48cb8248564b19efdf1a4272eb6fafe3138)), closes [#1102](https://www.github.com/googleapis/google-http-java-client/issues/1102)
* when disconnecting, close the underlying connection before the response InputStream ([#1315](https://www.github.com/googleapis/google-http-java-client/issues/1315)) ([f84ed59](https://www.github.com/googleapis/google-http-java-client/commit/f84ed5964f376ada5eb724a3d1f3ac526d31d9c5)), closes [#1303](https://www.github.com/googleapis/google-http-java-client/issues/1303)


### Documentation

* 19.0.0 libraries-bom ([#1312](https://www.github.com/googleapis/google-http-java-client/issues/1312)) ([62be21b](https://www.github.com/googleapis/google-http-java-client/commit/62be21b84a5394455d828b0f97f9e53352b8aa18))
* update version ([#1296](https://www.github.com/googleapis/google-http-java-client/issues/1296)) ([f17755c](https://www.github.com/googleapis/google-http-java-client/commit/f17755cf5e8ccbf441131ebb13fe60028fb63850))


### Dependencies

* update dependency com.fasterxml.jackson.core:jackson-core to v2.12.2 ([#1309](https://www.github.com/googleapis/google-http-java-client/issues/1309)) ([aa7d703](https://www.github.com/googleapis/google-http-java-client/commit/aa7d703d94e5e34d849bc753cfe8bd332ff80443))
* update dependency com.google.protobuf:protobuf-java to v3.15.3 ([#1301](https://www.github.com/googleapis/google-http-java-client/issues/1301)) ([1db338b](https://www.github.com/googleapis/google-http-java-client/commit/1db338b8b98465e03e93013b40fd8d821ac245c8))
* update dependency com.google.protobuf:protobuf-java to v3.15.6 ([#1310](https://www.github.com/googleapis/google-http-java-client/issues/1310)) ([9cb50e4](https://www.github.com/googleapis/google-http-java-client/commit/9cb50e49e1cfc196b915465bb6ecbd90fb6d04d7))

## [1.39.0](https://www.github.com/googleapis/google-http-java-client/compare/v1.38.1...v1.39.0) (2021-02-24)


### Features

* add http.status_code attribute to all Spans that have at least a low level http response ([#986](https://www.github.com/googleapis/google-http-java-client/issues/986)) ([fb02042](https://www.github.com/googleapis/google-http-java-client/commit/fb02042ac216379820950879cea45d06eec5278c))


### Bug Fixes

* deprecate obsolete utility methods ([#1231](https://www.github.com/googleapis/google-http-java-client/issues/1231)) ([8f95371](https://www.github.com/googleapis/google-http-java-client/commit/8f95371cf5681fbc67bd598d74089f38742a1177))
* fix buildRequest setUrl order ([#1255](https://www.github.com/googleapis/google-http-java-client/issues/1255)) ([97ffee1](https://www.github.com/googleapis/google-http-java-client/commit/97ffee1a68af6637dd5d53fcd70e2ce02c9c9604))
* refactor to use StandardCharsets ([#1243](https://www.github.com/googleapis/google-http-java-client/issues/1243)) ([03ec798](https://www.github.com/googleapis/google-http-java-client/commit/03ec798d7637ff454614415be7b324cd8dc7c77c))
* remove old broken link ([#1275](https://www.github.com/googleapis/google-http-java-client/issues/1275)) ([12f80e0](https://www.github.com/googleapis/google-http-java-client/commit/12f80e09e71a41b967db548ab93cab2e3f4e549c)), closes [#1278](https://www.github.com/googleapis/google-http-java-client/issues/1278)
* remove unused logger ([#1228](https://www.github.com/googleapis/google-http-java-client/issues/1228)) ([779d383](https://www.github.com/googleapis/google-http-java-client/commit/779d3832ffce741b7c4055a14855ce8755695fce))


### Documentation

* Jackson is unable to maintain stable Javadocs ([#1265](https://www.github.com/googleapis/google-http-java-client/issues/1265)) ([9e8fcff](https://www.github.com/googleapis/google-http-java-client/commit/9e8fcfffc6d92505528aff0a89c169bf3e812c41))


### Dependencies

* update dependency com.google.protobuf:protobuf-java to v3.15.1 ([#1270](https://www.github.com/googleapis/google-http-java-client/issues/1270)) ([213726a](https://www.github.com/googleapis/google-http-java-client/commit/213726a0b65f35fdc65713027833d22b553bbc20))
* update dependency com.google.protobuf:protobuf-java to v3.15.2 ([#1284](https://www.github.com/googleapis/google-http-java-client/issues/1284)) ([dfa06bc](https://www.github.com/googleapis/google-http-java-client/commit/dfa06bca432f644a7146e3987555f19c5d1be7c5))
* update OpenCensus to 0.28.0 for consistency with gRPC ([#1242](https://www.github.com/googleapis/google-http-java-client/issues/1242)) ([b810d53](https://www.github.com/googleapis/google-http-java-client/commit/b810d53c8f63380c1b4f398408cfb47c6ab134cc))
* version manage error_prone_annotations to 2.5.1 ([#1268](https://www.github.com/googleapis/google-http-java-client/issues/1268)) ([6a95f6f](https://www.github.com/googleapis/google-http-java-client/commit/6a95f6f2494a9dafd968d212b15c9b329416864f))

### [1.38.1](https://www.github.com/googleapis/google-http-java-client/compare/v1.38.0...v1.38.1) (2021-01-12)


### Bug Fixes

* address some deprecation warnings in Java 9+ ([#1215](https://www.github.com/googleapis/google-http-java-client/issues/1215)) ([9f53a67](https://www.github.com/googleapis/google-http-java-client/commit/9f53a6788e20bbded1b5937a5e8fe19ace31beaa))
* deprecate JacksonFactory in favor of GsonFactory to align with security team advice ([#1216](https://www.github.com/googleapis/google-http-java-client/issues/1216)) ([6b9b6c5](https://www.github.com/googleapis/google-http-java-client/commit/6b9b6c57734c4917394d0e256e745d69b61b5517))
* JSON spec mandates UTF-8 ([#1220](https://www.github.com/googleapis/google-http-java-client/issues/1220)) ([adb2ea4](https://www.github.com/googleapis/google-http-java-client/commit/adb2ea41c4eee61174ec6e588dec576fc53169f6))


### Documentation

* BOM 15.0.0 ([#1177](https://www.github.com/googleapis/google-http-java-client/issues/1177)) ([125a697](https://www.github.com/googleapis/google-http-java-client/commit/125a697c5cb5535894e46fd59e73663c50f3a6fa))


### Dependencies

* update guava to 30.1-android ([#1199](https://www.github.com/googleapis/google-http-java-client/issues/1199)) ([7922dc0](https://www.github.com/googleapis/google-http-java-client/commit/7922dc0517bd82669a18b81af38e5ba211bc2e0b))

## [1.38.0](https://www.github.com/googleapis/google-http-java-client/compare/v1.37.0...v1.38.0) (2020-11-02)


### Features

* add isMtls property to ApacheHttpTransport ([#1168](https://www.github.com/googleapis/google-http-java-client/issues/1168)) ([c416e20](https://www.github.com/googleapis/google-http-java-client/commit/c416e201c92a5c5fc1b1c59c5dd63e8ec1463f5f))
* add mtls support for NetHttpTransport ([#1147](https://www.github.com/googleapis/google-http-java-client/issues/1147)) ([51762f2](https://www.github.com/googleapis/google-http-java-client/commit/51762f221ec8ab38da03149c8012e63aec0433dc))


### Dependencies

* guava 30.0-android ([#1151](https://www.github.com/googleapis/google-http-java-client/issues/1151)) ([969dbbf](https://www.github.com/googleapis/google-http-java-client/commit/969dbbf127708aff16309f82538aca6f0a651638))


### Documentation

* libraries-bom 13.4.0 ([#1170](https://www.github.com/googleapis/google-http-java-client/issues/1170)) ([6818a02](https://www.github.com/googleapis/google-http-java-client/commit/6818a02a15e1bef8e9f5ea56a4ecc2b8d0646f9b))

## [1.37.0](https://www.github.com/googleapis/google-http-java-client/compare/v1.36.0...v1.37.0) (2020-10-13)


### Features

* add flag to allow UrlEncodedContent to use UriPath escaping ([#1100](https://www.github.com/googleapis/google-http-java-client/issues/1100)) ([9ab7016](https://www.github.com/googleapis/google-http-java-client/commit/9ab7016032327f6fb0f91970dfbd511b029dd949)), closes [#1098](https://www.github.com/googleapis/google-http-java-client/issues/1098)


### Bug Fixes

* make google-http-client.properties file shading friendly ([#1046](https://www.github.com/googleapis/google-http-java-client/issues/1046)) ([860bb05](https://www.github.com/googleapis/google-http-java-client/commit/860bb0541bcd7fc516cad14dd0d52481c7c7b414))


### Dependencies

* update protobuf-java to 3.13.0 ([#1093](https://www.github.com/googleapis/google-http-java-client/issues/1093)) ([b7e9663](https://www.github.com/googleapis/google-http-java-client/commit/b7e96632234e944e0e476dedfc822333716756bb))


### Documentation

* libraries-bom 12.0.0 ([#1136](https://www.github.com/googleapis/google-http-java-client/issues/1136)) ([450fcb2](https://www.github.com/googleapis/google-http-java-client/commit/450fcb2293cf3fa7c788cf0cc8ae48e865ae8de8))

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
