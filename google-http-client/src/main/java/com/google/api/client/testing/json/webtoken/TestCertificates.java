/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.testing.json.webtoken;

import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.api.client.util.Base64;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Lists;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * {@link Beta} <br>
 * Test certificates.
 *
 * <p>Contains a test certificate chain, the respective private keys and signed data.
 *
 * @since 1.19.1.
 */
@Beta
public class TestCertificates {

  /**
   * {@link Beta} <br>
   * Wrapper for a PEM encoded certificate providing utility routines.
   */
  @Beta
  public static class CertData {
    private String pem;

    public CertData(String pem) {
      this.pem = pem;
    }

    public Certificate getCertfificate() throws IOException, CertificateException {
      byte[] bytes = getDer();
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      return SecurityUtils.getX509CertificateFactory().generateCertificate(bis);
    }

    public byte[] getDer() throws IOException {
      return PemReader.readFirstSectionAndClose(new StringReader(pem), "CERTIFICATE")
          .getBase64DecodedBytes();
    }

    public String getBase64Der() throws IOException {
      return Base64.encodeBase64String(getDer());
    }

    public X509TrustManager getTrustManager() throws IOException, GeneralSecurityException {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null, null);
      keyStore.setCertificateEntry("ca", getCertfificate());
      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(keyStore);
      return (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
    }
  }

  /**
   * Test leaf certificate.
   *
   * <pre>
   * Issuer: CN=Root
   * Subject: C=US, ST=California, L=Mountain View, O=Google Inc., CN=foo.bar.com
   * </pre>
   */
  public static final CertData FOO_BAR_COM_CERT =
      new CertData(
          "-----BEGIN CERTIFICATE-----\n"
              + "MIIC6TCCAdECASowDQYJKoZIhvcNAQELBQAwDzENMAsGA1UEAwwEUm9vdDAeFw0x\n"
              + "NDExMTgxNjU0MDNaFw0zNDExMTMxNjU0MDNaMGYxCzAJBgNVBAYTAlVTMRMwEQYD\n"
              + "VQQIDApDYWxpZm9ybmlhMRYwFAYDVQQHDA1Nb3VudGFpbiBWaWV3MRQwEgYDVQQK\n"
              + "DAtHb29nbGUgSW5jLjEUMBIGA1UEAwwLZm9vLmJhci5jb20wggEiMA0GCSqGSIb3\n"
              + "DQEBAQUAA4IBDwAwggEKAoIBAQCzFVKJOkqTmyyjMHWBOrLdpYmc0EcvG3MohaV+\n"
              + "UJrVrI2SDykY8YWSkTKz9BKmF8HP/GjPPDs3184Cej9b1WeyvVB8Rj3guH3oL+sJ\n"
              + "T3u9V2y4zyo5xO6FWMBYEQ6X8DkGlYtTp5theYbRrXNELul4lF+LtHTCaAANRMkO\n"
              + "l0NEoLa6BRhOG68gFfIAxx5lT8REE9utvPuy+rCaBHnfHOPf8pn0LSvceBijSIFo\n"
              + "S3Y5crjPVjyiPAZUHWnHTFAilfHnpLBlGxpCylePQhMKrPcgvDoD9nd0LA6xYLF7\n"
              + "DPXXSa8FLO+fPV8CNJCAsFuq9Rlf2Tt3SjLtWRYuh5LuctP7AgMBAAEwDQYJKoZI\n"
              + "hvcNAQELBQADggEBAEsMABZl+8Rlk0hqBktsDurri4nF/07CnSBe/zUbTiYhMpr7\n"
              + "VRIDlHLoe5lslLilfXzvaymcMFeH1uBxNwhf7IO7WvIwQeUHSV+rHyNygTTieO0J\n"
              + "n8Hw+4SCohHAdMvD5uWEwn3Lv+W4y7OhaSbzlhVCVCnFLVKicBayUXHtdJXJICok\n"
              + "R4+h/WNM7g0iKThakZOyfb8h1phy7TMTVlPFKrcVDo5m9+GhtPC4PNjGLok6r/jx\n"
              + "9CIOCapIqi8fXJEOxKvilYeAYqfjWvhx00juEUBHrpCQ8wT4TA+LlI02cRz5rxW4\n"
              + "FQAz1NdoG9HZDZWa+NNFTZdAmtWPJMLd+8L8sl4=\n"
              + "-----END CERTIFICATE-----");

  /** Private key for {@code FOO_BAR_COM_CERT}. */
  public static final String FOO_BAR_COM_KEY =
      "-----BEGIN PRIVATE KEY-----\n"
          + "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCzFVKJOkqTmyyj\n"
          + "MHWBOrLdpYmc0EcvG3MohaV+UJrVrI2SDykY8YWSkTKz9BKmF8HP/GjPPDs3184C\n"
          + "ej9b1WeyvVB8Rj3guH3oL+sJT3u9V2y4zyo5xO6FWMBYEQ6X8DkGlYtTp5theYbR\n"
          + "rXNELul4lF+LtHTCaAANRMkOl0NEoLa6BRhOG68gFfIAxx5lT8REE9utvPuy+rCa\n"
          + "BHnfHOPf8pn0LSvceBijSIFoS3Y5crjPVjyiPAZUHWnHTFAilfHnpLBlGxpCyleP\n"
          + "QhMKrPcgvDoD9nd0LA6xYLF7DPXXSa8FLO+fPV8CNJCAsFuq9Rlf2Tt3SjLtWRYu\n"
          + "h5LuctP7AgMBAAECggEBAJZQomue6vQEfq4nQaoL/BCBHwXp6KYIs1ti+msQ+zW4\n"
          + "1Ueww/001LoWd+mGR5T0QfDy24J++vG/iSKZO884TAdCUmlNiCi0krIubmjtN17R\n"
          + "H+frs3Sz8MUqnqANCSPNNgBpy32XJJvnppserK6hdcSJPb2E5bA8HTcF8oD1xDe4\n"
          + "CgPK9PKL2PxrR0ofs09RLGTSdh2+rPWvvefk1x1uBfg+wHRlfvqMSKpZ3SDabjhy\n"
          + "PgB21D86SlF5L1AfeqSTfQvmwMLtOpJCVjLK2WZmvdoY7kbwE416AMLxX4tw2a/l\n"
          + "vzVyo2T/B0Wc3be+5m2o96TctHRH1yEK4huEOJojvBECgYEA5RloFMOnYNMZdoEf\n"
          + "yl6TAPEmFD7vCYHXpSlQdFFKu988CNF5+grn9kjC7rF+JxPEYUsnNA11TFzFEfki\n"
          + "Lu0uXirJH+0gQzEp/qGd2SjDANCk+kORjeOOmefbxziG/Y74rnJ1A7gZjL8Abrie\n"
          + "K0mTfOk9DcgqX96PP4HXgX3+XYkCgYEAyBx28UNZoL3Dy8iquTV0VmXCOq2c5+aW\n"
          + "3YS2BKP9rAPy5mWWy6PR28yduomuUu04GxHYf2yw0+0UxpPyWu8TdQHJKjLX4On7\n"
          + "L+ZholvXpyqs51btsbBiRK022akh/MPnqdD9zt/RS2b1QM4yfEWN8kVE3zsMWxMP\n"
          + "gBf9EH4taGMCgYBfsD3ttk65vVI8UfBiSSAjW5WpDSQwF2BnpprpCm8pizL7B+tn\n"
          + "iZibIIbyxYXIcpQqgwZL0nc0vua8/A7QBNbCFCLPR+6awfUlWoGgi0rvkzXlJcWs\n"
          + "uuf71oDQdAbF7yplSn8fX4ykYb6fgFLoB6InoQ+UKw+v3Th9sRC/EE3m6QKBgDBN\n"
          + "RpyHwDufcoJe5m6cK3+rQk29mFEVhLblkLXgC5wYu+nG/bYbzcz7P9tF3nEf11oZ\n"
          + "XaOsTaZp5IjmLyqp6I1mp/LqoNcmQz5Vop15A73S/Dc+8VLhm2auVL4HKDAF7YY8\n"
          + "7vafabqEmJBS9Tav50piU/R6IUpeeHBX2frAKh+3AoGAPTLxTMMEbhZGJFs8GRP9\n"
          + "fFyWZeEkf3tgUK19tAAOk3TX+O0TNvD8UouXq7Z/EUaE1mYhKPf5LbI6nbYEVll4\n"
          + "mWLGd+o8FNFp6E5083O3Tgf0BI4l+sKnwpP/Sqg9BDGARTPS5taeX0SWtQ+HPYGC\n"
          + "4e5m59uhN7t8tHtDVcK0/Pk=\n"
          + "-----END PRIVATE KEY-----";

  /**
   * Test CA Certificate.
   *
   * <pre>
   * Issuer: CN=Root
   * Subject: CN=Root
   * </pre>
   */
  public static final CertData CA_CERT =
      new CertData(
          "-----BEGIN CERTIFICATE-----\n"
              + "MIIC8TCCAdmgAwIBAgIJAMNI15HrGylkMA0GCSqGSIb3DQEBCwUAMA8xDTALBgNV\n"
              + "BAMMBFJvb3QwHhcNMTQxMTE4MTY1NDAzWhcNMzQxMTEzMTY1NDAzWjAPMQ0wCwYD\n"
              + "VQQDDARSb290MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzeUNc4bS\n"
              + "WHhOTU+5MQ/lOmmjQWpfBi+FJuxvoeOmQwi6frPKKsaKKYGfCTPlKE0dmrEP95bn\n"
              + "i/qL5xApP17orjUe6KRtJAwFNI5EZadIfjbh/q+85C1Cp2BS2YmuZQzXZHP63yyB\n"
              + "p05YcbMKwCBHXaAgYbmTTk+4+1pjNpHP6YiF2gCPvSfzokGyhbvBqnPbnTdI9w6f\n"
              + "jNBYAbr/uBOTU0vK4ktzlWk5lvsm51e8vsLSqWhoHADq0AriAelU4SHsSACkRUQS\n"
              + "xWV0K5hzTv4ecvCbG9dskiDCwWg+uTRSoAFeZOhONL000q7Vey3DZTcLl8/O4NQV\n"
              + "aZR5iAgVWlWcswIDAQABo1AwTjAdBgNVHQ4EFgQUsimlIRDcJR0ofR7oM8KwHFOH\n"
              + "+sIwHwYDVR0jBBgwFoAUsimlIRDcJR0ofR7oM8KwHFOH+sIwDAYDVR0TBAUwAwEB\n"
              + "/zANBgkqhkiG9w0BAQsFAAOCAQEAWQl8SmbQoBV3tjOJ8zMlcN0xOPpSSNbx0g7E\n"
              + "L/dQgJpet0McW62RHlgQAOKbS3PReo2nsRB/ZRyYDu4i13ZHZ8bMsGOES4BQpz13\n"
              + "mtmXg9RhsXqL0eDYfBcjjtlruUbxhnALp4VN1zVdyWAPCj0eu3MxpgMWcyn50Qmi\n"
              + "JSj/Equ/lLhve/wKvjG5WhnV8uRKRuFbFct0DHAHMnZqFHcGS5So0cYnSfK5fbBR\n"
              + "NelGflhpbbPp0V0aXiqinqD0Ye3OaZdFq+2rP1oC/a5/Ou4LspY3b5oD9rENdy7b\n"
              + "q0KewPFtgPvUkJrJ3TzbiwvpghZ7zG26bnJ5I7uc4y1VujqaOA==\n"
              + "-----END CERTIFICATE-----");

  /** Private key for {@code CA_CERT}. */
  public static final String CA_KEY =
      "-----BEGIN PRIVATE KEY-----\n"
          + "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDN5Q1zhtJYeE5N\n"
          + "T7kxD+U6aaNBal8GL4Um7G+h46ZDCLp+s8oqxoopgZ8JM+UoTR2asQ/3lueL+ovn\n"
          + "ECk/XuiuNR7opG0kDAU0jkRlp0h+NuH+r7zkLUKnYFLZia5lDNdkc/rfLIGnTlhx\n"
          + "swrAIEddoCBhuZNOT7j7WmM2kc/piIXaAI+9J/OiQbKFu8Gqc9udN0j3Dp+M0FgB\n"
          + "uv+4E5NTS8riS3OVaTmW+ybnV7y+wtKpaGgcAOrQCuIB6VThIexIAKRFRBLFZXQr\n"
          + "mHNO/h5y8Jsb12ySIMLBaD65NFKgAV5k6E40vTTSrtV7LcNlNwuXz87g1BVplHmI\n"
          + "CBVaVZyzAgMBAAECggEANfRuP/X2rURpkIzxxM+bjGEebQgI+r/9LqQK5OuZKDvj\n"
          + "U0yeD/OTRSk4mdrFlHgQ5/a6bnFXIDF59AUiKf8fDnfRL7nW9/lGa+1UMydRMfID\n"
          + "6w/2efz6WI4/Z85SqxxgXWyfM1igaU14k+MNUCelS/2oPrO4zG7L1OJs2WIAj/vE\n"
          + "HnndSBa3rvTXmY37JclkChFokG0svuZMmaXWG1JI6JziSsvO4YZAYvZ10yCvbFzZ\n"
          + "iczMCyyGhRcUeG3wbVDK0lPp5f1jKtyfuQtR2uFhdRHUk2+cMY6s/o3hgdW5b/z9\n"
          + "Yddyw28tC6/uECHJs8dsmNM4hPc+n2+wCVwB9HbSMQKBgQD3V640Tv5UWiHM4lGq\n"
          + "pSdUViNsLgDLmNplWLB0aRbBgTsJLGlzI1sGqSEydlZORYZT4GBdLmTJdumBGBAn\n"
          + "4FxfyyAVjjn8WjYo9ocyMrIGLFKF3EvSyx4opsOX6QOyuyzdDhzt+BkY66Zb0Bgl\n"
          + "lzUQ4S6hhvvEQc5COiNmTuDT/QKBgQDVGfpp8yBamTyRgGQWTwRqIQuJC2QHOrhV\n"
          + "OKQ7NwMyMObyML0ZQm2SCu+Oo0qsMxz8Ix6sNtnJfxZxpUYCLG3HWc6EfaGT1hDR\n"
          + "EgWsdl9J/xP/KwgSzHuSqZTCuNQRTg/XbNfjXnMHy8UaTBL+0jHLAnmvczBrSnEM\n"
          + "r8RgkjoabwKBgQCkuklz3vQ1O33tVQEs1Cc4XNHkl1LCRb+V5ZZHQUH9h9LIjkKA\n"
          + "gxh5fCR21icuo9ENhY7IIEDRiBeFeYAw/pSm28I3eOyXa4FMkLuDrA2yXMxtCEWb\n"
          + "Utl4G3CCeJaU72G2q1KLDkOwvCikVxft2SFnZ4FF5H9CuszigJPY7EmCBQKBgD+/\n"
          + "fra1IWeY0ZKhOs+loadx7TZ47tpuyXfM8uw337/i+yNWSytEQOzgUptz48GxpKkU\n"
          + "hHd2DR6G4xrqGxBJZCmvhuUBhBVqgytX3dSisIy9PqkloUumWg0cp8C8c8wdcwW5\n"
          + "rLd6qKSbY4IjYcdS78xQGEDRD5n48eqepftRowoHAoGBAMdJ5/QwIymaTBhblYiL\n"
          + "nvzZZ6kvxqId+JF93skZJ4NdQ346CVcWWbjTwO/oaJ9ri3MsWY18t4uSIYeYyaCa\n"
          + "5dqQo3nObq2jqxFby92GWSNrwape2FvRGzJ7hnr44EkxUlQPeICod84RI/1mdOM8\n"
          + "E+VTo/KjRA8P2ogks9bltd6f\n"
          + "-----END PRIVATE KEY-----";

  /**
   * CA certificate signed with a bogus key.
   *
   * <pre>
   * Issuer: CN=Root
   * Subject: CN=Root
   * </pre>
   */
  public static final CertData BOGUS_CA_CERT =
      new CertData(
          "-----BEGIN CERTIFICATE-----\n"
              + "MIIC8TCCAdmgAwIBAgIJAP2af/EIgk6oMA0GCSqGSIb3DQEBCwUAMA8xDTALBgNV\n"
              + "BAMMBFJvb3QwHhcNMTQxMTE5MTEwNDMyWhcNMzQxMTE0MTEwNDMyWjAPMQ0wCwYD\n"
              + "VQQDDARSb290MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtPB0EtUf\n"
              + "aVS9LRljaL4NTYp0tJooMrRTI4ht4ixIv7m6XTSbxVjOtY0228ZPWeUE/3wduezW\n"
              + "1rWNU4Uh4ezW0rw9CmW6m2zsMjjGwjY4A5ctMRDlgQtxzfHSPWPtTixtBr3YpdcP\n"
              + "mg9xVIYvSHZ+fA3x5dRFRxdNidVrndVINzUoaoD9hZ/sgCKg9c2hdDSO9prrTpXD\n"
              + "yatgLZ8LsFJO94HrkfFsQqquwxxvpixyWtjWUpnO28jnbDRC0ADOp/WZQ8exOP+a\n"
              + "XUcrHdIsC0RcB6csnM6EarfwEm1jnBwDi37Rxk2BFiBYyzEbCrn7M6QY/DQrZJbw\n"
              + "9gzSIvT2+5OvawIDAQABo1AwTjAdBgNVHQ4EFgQUYo97/In/SDI+pKRTSrSVhPyq\n"
              + "5UQwHwYDVR0jBBgwFoAUYo97/In/SDI+pKRTSrSVhPyq5UQwDAYDVR0TBAUwAwEB\n"
              + "/zANBgkqhkiG9w0BAQsFAAOCAQEABuUZ+sF4QD8H+PHvJLz+3+puXYvvE2IpcC65\n"
              + "RQznp5iq5Rs4oGJvYwyD1bVUbCNz1IoyB9Lfo5QmSuyV1JybalBZ9FCDzZunBT3O\n"
              + "4Tr6KfziVPHat3vYMNzzJY/IU3u6uLDmqm1J6qoSBkq4yL1AaHFon2j9gT3FXvVk\n"
              + "7f1DjztAplWQBC4ScepJbiIRJkLxThDmM2g1xKUtZ6LlPL5J5CmXutzWbV5YS1eo\n"
              + "uVrDRTmXr4wLzpcURWWB2gbPc0l7+1TfvTydVEp7YqN1EhvNmvsejiQCy+4Cq/D1\n"
              + "m4rBV4SLLaHstTQNqcK1djxy2FbpYD7j5Himdc0oUeYif9gZ9g==\n"
              + "-----END CERTIFICATE-----\n");

  /**
   * A test JWS signature.
   *
   * <p>The signed JSON is the following message:
   *
   * <pre>
   * {"foo":"bar"}
   * </pre>
   *
   * <p>The message is signed using {@code FOO_BAR_COM_KEY}.
   */
  public static final String JWS_SIGNATURE =
      "eyJhbGciOiJSUzI1NiIsIng1YyI6WyJNSUlDNlRDQ0FkRUNBU293RFFZSktvWklo"
          + "dmNOQVFFTEJRQXdEekVOTUFzR0ExVUVBd3dFVW05dmREQWVGdzB4TkRFeE1UZ3hO"
          + "alUwTUROYUZ3MHpOREV4TVRNeE5qVTBNRE5hTUdZeEN6QUpCZ05WQkFZVEFsVlRN"
          + "Uk13RVFZRFZRUUlEQXBEWVd4cFptOXlibWxoTVJZd0ZBWURWUVFIREExTmIzVnVk"
          + "R0ZwYmlCV2FXVjNNUlF3RWdZRFZRUUtEQXRIYjI5bmJHVWdTVzVqTGpFVU1CSUdB"
          + "MVVFQXd3TFptOXZMbUpoY2k1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFB"
          + "NElCRHdBd2dnRUtBb0lCQVFDekZWS0pPa3FUbXl5ak1IV0JPckxkcFltYzBFY3ZH"
          + "M01vaGFWK1VKclZySTJTRHlrWThZV1NrVEt6OUJLbUY4SFAvR2pQUERzMzE4NENl"
          + "ajliMVdleXZWQjhSajNndUgzb0wrc0pUM3U5VjJ5NHp5bzV4TzZGV01CWUVRNlg4"
          + "RGtHbFl0VHA1dGhlWWJSclhORUx1bDRsRitMdEhUQ2FBQU5STWtPbDBORW9MYTZC"
          + "UmhPRzY4Z0ZmSUF4eDVsVDhSRUU5dXR2UHV5K3JDYUJIbmZIT1BmOHBuMExTdmNl"
          + "QmlqU0lGb1MzWTVjcmpQVmp5aVBBWlVIV25IVEZBaWxmSG5wTEJsR3hwQ3lsZVBR"
          + "aE1LclBjZ3ZEb0Q5bmQwTEE2eFlMRjdEUFhYU2E4RkxPK2ZQVjhDTkpDQXNGdXE5"
          + "UmxmMlR0M1NqTHRXUll1aDVMdWN0UDdBZ01CQUFFd0RRWUpLb1pJaHZjTkFRRUxC"
          + "UUFEZ2dFQkFFc01BQlpsKzhSbGswaHFCa3RzRHVycmk0bkYvMDdDblNCZS96VWJU"
          + "aVloTXByN1ZSSURsSExvZTVsc2xMaWxmWHp2YXltY01GZUgxdUJ4TndoZjdJTzdX"
          + "dkl3UWVVSFNWK3JIeU55Z1RUaWVPMEpuOEh3KzRTQ29oSEFkTXZENXVXRXduM0x2"
          + "K1c0eTdPaGFTYnpsaFZDVkNuRkxWS2ljQmF5VVhIdGRKWEpJQ29rUjQraC9XTk03"
          + "ZzBpS1RoYWtaT3lmYjhoMXBoeTdUTVRWbFBGS3JjVkRvNW05K0dodFBDNFBOakdM"
          + "b2s2ci9qeDlDSU9DYXBJcWk4ZlhKRU94S3ZpbFllQVlxZmpXdmh4MDBqdUVVQkhy"
          + "cENROHdUNFRBK0xsSTAyY1J6NXJ4VzRGUUF6MU5kb0c5SFpEWldhK05ORlRaZEFt"
          + "dFdQSk1MZCs4TDhzbDQ9IiwiTUlJQzhUQ0NBZG1nQXdJQkFnSUpBTU5JMTVIckd5"
          + "bGtNQTBHQ1NxR1NJYjNEUUVCQ3dVQU1BOHhEVEFMQmdOVkJBTU1CRkp2YjNRd0ho"
          + "Y05NVFF4TVRFNE1UWTFOREF6V2hjTk16UXhNVEV6TVRZMU5EQXpXakFQTVEwd0N3"
          + "WURWUVFEREFSU2IyOTBNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1J"
          + "SUJDZ0tDQVFFQXplVU5jNGJTV0hoT1RVKzVNUS9sT21talFXcGZCaStGSnV4dm9l"
          + "T21Rd2k2ZnJQS0tzYUtLWUdmQ1RQbEtFMGRtckVQOTVibmkvcUw1eEFwUDE3b3Jq"
          + "VWU2S1J0SkF3Rk5JNUVaYWRJZmpiaC9xKzg1QzFDcDJCUzJZbXVaUXpYWkhQNjN5"
          + "eUJwMDVZY2JNS3dDQkhYYUFnWWJtVFRrKzQrMXBqTnBIUDZZaUYyZ0NQdlNmem9r"
          + "R3loYnZCcW5QYm5UZEk5dzZmak5CWUFici91Qk9UVTB2SzRrdHpsV2s1bHZzbTUx"
          + "ZTh2c0xTcVdob0hBRHEwQXJpQWVsVTRTSHNTQUNrUlVRU3hXVjBLNWh6VHY0ZWN2"
          + "Q2JHOWRza2lEQ3dXZyt1VFJTb0FGZVpPaE9OTDAwMHE3VmV5M0RaVGNMbDgvTzRO"
          + "UVZhWlI1aUFnVldsV2Nzd0lEQVFBQm8xQXdUakFkQmdOVkhRNEVGZ1FVc2ltbElS"
          + "RGNKUjBvZlI3b004S3dIRk9IK3NJd0h3WURWUjBqQkJnd0ZvQVVzaW1sSVJEY0pS"
          + "MG9mUjdvTThLd0hGT0grc0l3REFZRFZSMFRCQVV3QXdFQi96QU5CZ2txaGtpRzl3"
          + "MEJBUXNGQUFPQ0FRRUFXUWw4U21iUW9CVjN0ak9KOHpNbGNOMHhPUHBTU05ieDBn"
          + "N0VML2RRZ0pwZXQwTWNXNjJSSGxnUUFPS2JTM1BSZW8ybnNSQi9aUnlZRHU0aTEz"
          + "WkhaOGJNc0dPRVM0QlFwejEzbXRtWGc5UmhzWHFMMGVEWWZCY2pqdGxydVVieGhu"
          + "QUxwNFZOMXpWZHlXQVBDajBldTNNeHBnTVdjeW41MFFtaUpTai9FcXUvbExodmUv"
          + "d0t2akc1V2huVjh1UktSdUZiRmN0MERIQUhNblpxRkhjR1M1U28wY1luU2ZLNWZi"
          + "QlJOZWxHZmxocGJiUHAwVjBhWGlxaW5xRDBZZTNPYVpkRnErMnJQMW9DL2E1L091"
          + "NExzcFkzYjVvRDlyRU5keTdicTBLZXdQRnRnUHZVa0pySjNUemJpd3ZwZ2haN3pH"
          + "MjZibko1STd1YzR5MVZ1anFhT0E9PSJdfQ.eyJmb28iOiJiYXIifQ.eWzIsJF4PE"
          + "xQap9HK6Vlz8DGlgGwoiLCtyOEK0Bfu_yHTAZeApn5rh6Uzfx06Gv6eHdM34YL_t"
          + "gLRb4bjuZVA8xvQ9uHNs8UtpBIOiUcagzvtKyyfCofk5U5sNb54GgVVYxa6p4A1O"
          + "bdJv1jjlUOnzR8keX5LsAM4Ia7xeqiFh0GER4l0ulVChy_bSn0IeNiKFW7HKcxtc"
          + "GO_zZTtlv4HiifuyPSk_ar2IDX1w599KXniVcWkQ_W1zcp5YuPDw8mIQDVCH2uQY"
          + "7qs2ejdZj5LIgIz4CbQ0wg53rlwE7DDQM6MNUgZLnzNmMSMfFrpE7_PQyxe2qJCs"
          + "ucHODzEHX4Tg";

  private static JsonWebSignature jsonWebSignature = null;

  public static JsonWebSignature getJsonWebSignature() throws IOException {
    if (jsonWebSignature == null) {
      JsonWebSignature.Header header = new JsonWebSignature.Header();
      header.setAlgorithm("RS256");
      List<String> certificates = Lists.newArrayList();
      certificates.add(FOO_BAR_COM_CERT.getBase64Der());
      certificates.add(CA_CERT.getBase64Der());
      header.setX509Certificates(certificates);
      JsonWebToken.Payload payload = new JsonWebToken.Payload();
      payload.set("foo", "bar");
      int firstDot = JWS_SIGNATURE.indexOf('.');
      int secondDot = JWS_SIGNATURE.indexOf('.', firstDot + 1);
      byte[] signatureBytes = Base64.decodeBase64(JWS_SIGNATURE.substring(secondDot + 1));
      byte[] signedContentBytes = StringUtils.getBytesUtf8(JWS_SIGNATURE.substring(0, secondDot));
      JsonWebSignature signature =
          new JsonWebSignature(header, payload, signatureBytes, signedContentBytes);
      jsonWebSignature = signature;
    }
    return jsonWebSignature;
  }
}
