# XaihtKyber

[![Java 17](https://img.shields.io/badge/Java-17-0b5fff?style=for-the-badge)](https://www.oracle.com/java/)
[![Maven WAR](https://img.shields.io/badge/Maven-WAR-b14e2d?style=for-the-badge)](https://maven.apache.org/)
[![Post-Quantum](https://img.shields.io/badge/Post--Quantum-Kyber-1a7f4b?style=for-the-badge)](#overview)

> A Maven WAR web application that exposes CRYSTALS-Kyber workflows for key generation, hybrid buffer encryption/decryption, and receiver-authenticated integrity attestation.

## Table Of Contents

- [Overview](#overview)
- [Why This Application Exists](#why-this-application-exists)
- [What It Is Built With](#what-it-is-built-with)
- [How The Application Is Structured](#how-the-application-is-structured)
- [Internal Design](#internal-design)
- [Security Features Implemented In The Web Layer](#security-features-implemented-in-the-web-layer)
- [Supported Pages And Endpoints](#supported-pages-and-endpoints)
- [Requirements](#requirements)
- [Build And Verify](#build-and-verify)
- [Run And Deploy](#run-and-deploy)
- [How To Use The Application](#how-to-use-the-application)
- [Validation Rules And Operational Constraints](#validation-rules-and-operational-constraints)
- [What The Tests Verify](#what-the-tests-verify)
- [Maven Profiles Included](#maven-profiles-included)
- [Limitations And Design Notes](#limitations-and-design-notes)

## Overview

XaihtKyber is a Jakarta Servlet + JSP application built around the Bouncy Castle Post-Quantum provider. It uses **Kyber as a KEM** to derive shared secrets and then applies conventional primitives on top of those secrets for practical web operations:

- **Key generation** for `Kyber-512`, `Kyber-768`, and `Kyber-1024`
- **Buffer cipher** using Kyber encapsulation + `AES-256-GCM`
- **Buffer decipher** using Kyber decapsulation + `AES-256-GCM`
- **Attestation generation** using Kyber encapsulation + `HKDF` + `HMAC-SHA-256`
- **Attestation verification** using Kyber decapsulation + `HKDF` + `HMAC-SHA-256`

This project is intentionally explicit about one important cryptographic fact:

> Kyber is **not** a digital signature algorithm.  
> The "signer" flow in this application is implemented as a **receiver-authenticated integrity attestation**, not a publicly verifiable signature.

## Why This Application Exists

This application provides a browser-based control surface for testing and demonstrating post-quantum transport-style workflows inside a traditional Java web stack.

It is useful when you want to:

- generate compatible Kyber key pairs from a UI
- encrypt a text payload to a recipient public key
- decrypt a previously generated envelope with the recipient private key
- bind a payload to a verifier-owned Kyber private key through an encapsulated shared secret
- validate whether a payload was altered after an attestation envelope was created

## What It Is Built With

| Layer | Technology |
| --- | --- |
| Language | Java 17 |
| Build tool | Maven |
| Packaging | WAR |
| Web stack | Jakarta EE 10 Web Profile (Servlet 6.0, CDI 4.0, JSP) |
| PQC provider | Bouncy Castle PQC (`bcprov-jdk18on`) |
| KEM | CRYSTALS-Kyber |
| Symmetric encryption | AES-256-GCM |
| Integrity attestation | HKDF(SHA-256) + HMAC-SHA-256 |
| Tests | JUnit 4 |

## How The Application Is Structured

```text
src/
├─ main/
│  ├─ java/com/xaiht/kyber/
│  │  ├─ crypto/      # envelopes, enums, validation, provider registry
│  │  ├─ service/     # core Kyber, cipher, and attestation logic
│  │  └─ web/         # servlets and servlet helpers
│  └─ webapp/
│     ├─ *.jsp        # user-facing pages
│     ├─ assets/      # CSS
│     └─ WEB-INF/     # web.xml and JSP helpers
└─ test/
   └─ java/com/xaiht/kyber/service/
      ├─ KyberCipherServiceTest.java
      └─ KyberAttestationServiceTest.java
```

## Internal Design

### 1. Key generation

`KyberKeyService` uses Bouncy Castle's Kyber implementation to generate:

- a Base64-encoded **X.509 public key**
- a Base64-encoded **PKCS#8 private key**
- a short SHA-256-based **key fingerprint**

### 2. Buffer cipher flow

`KyberCipherService.cipher(...)` performs:

1. Parse and validate the recipient public key.
2. Use Kyber encapsulation to derive a shared secret.
3. Convert that secret into an AES-256 key through the KEM API.
4. Generate a random 12-byte IV.
5. Encrypt the plaintext with `AES/GCM/NoPadding`.
6. Return an envelope containing:
   - security level
   - recipient fingerprint
   - encapsulation
   - IV
   - ciphertext
   - associated data encoded as Base64

### 3. Buffer decipher flow

`KyberCipherService.decipher(...)` performs:

1. Parse and validate the recipient private key.
2. Decode the encapsulation, IV, ciphertext, and AAD.
3. Reconstruct the AES key via Kyber decapsulation.
4. Decrypt the payload with `AES/GCM/NoPadding`.

### 4. Attestation flow

`KyberAttestationService.attest(...)` performs:

1. Parse and validate the verifier public key.
2. Encapsulate a shared secret with Kyber.
3. Derive a MAC key using HKDF-SHA-256.
4. Compute `HMAC-SHA-256(payload || 0x00 || context)`.
5. Return an envelope with:
   - security level
   - verifier fingerprint
   - encapsulation
   - MAC
   - context encoded as Base64

### 5. Verification flow

`KyberAttestationService.verify(...)` performs:

1. Parse and validate the verifier private key.
2. Decapsulate the shared secret.
3. Recompute the MAC over the supplied payload and context.
4. Compare the supplied MAC with the recomputed MAC in constant time.

## Security Features Implemented In The Web Layer

Every servlet response applies security-oriented headers through `ServletSupport`:

- `Cache-Control: no-store, no-cache, must-revalidate, max-age=0`
- `Pragma: no-cache`
- `Expires: 0`
- `X-Content-Type-Options: nosniff`
- `Content-Security-Policy`
- `Referrer-Policy: no-referrer`
- `X-Frame-Options: DENY`

The application also enforces:

- **CSRF token validation** on every POST flow
- **input length limits** for all parameters
- **Kyber security-level matching** between the selected profile and submitted keys

## Supported Pages And Endpoints

| UI Page | Purpose | POST Endpoint |
| --- | --- | --- |
| `/index.jsp` | landing page / control surface | none |
| `/key-generator.jsp` | generate Kyber key pairs | `/keys/generate` |
| `/buffer-cipher.jsp` | encrypt a plaintext buffer | `/buffer/cipher` |
| `/buffer-decipher.jsp` | decrypt a cipher envelope | `/buffer/decipher` |
| `/kyber-signer.jsp` | generate an attestation envelope | `/attestation/sign` |
| `/kyber-sign-verifier.jsp` | verify an attestation envelope | `/attestation/verify` |

## Supported Security Levels

- `Kyber-512`
- `Kyber-768`
- `Kyber-1024`

The application accepts either numeric values (`512`, `768`, `1024`) or the named values (`Kyber512`, `Kyber768`, `Kyber1024`) internally, but the UI submits numeric values.

## Requirements

- JDK 17+
- Maven 3.9+ recommended
- GlassFish 7.x or another Jakarta EE 10 compatible servlet container

Typical deployment targets:

- GlassFish 7
- Payara 6
- any servlet container capable of running a Jakarta EE 10 / Servlet 6 WAR

## Build And Verify

Run the test suite:

```bash
mvn clean test
```

Build the WAR:

```bash
mvn clean package
```

The packaged artifact is generated at:

```text
target/XaihtKyber.war
```

## Run And Deploy

### Manual deployment

1. Build the WAR with `mvn clean package`
2. Deploy `target/XaihtKyber.war` to GlassFish 7 or another Jakarta EE 10 container
3. Open the application in the deployed context, commonly:

```text
http://localhost:8080/XaihtKyber/
```

### GlassFish 7 auto-deploy profile

If `GLASSFISH_HOME` points at a GlassFish 7 installation, the project can copy the WAR to that domain's `autodeploy` directory during install:

```bash
mvn clean install -Pauto-deploy
```

If needed, override the autodeploy directory explicitly:

```bash
mvn clean install -Pauto-deploy -Dglassfish.autodeploy.dir="C:\path\to\glassfish\domains\domain1\autodeploy"
```

## Optional Dependency Audit

The project includes an OWASP Dependency-Check Maven profile:

```bash
mvn verify -Pdependency-check
```

If `NVD_API_KEY` is available and you want the generated HTML report opened automatically on Windows:

```bash
mvn verify -Pdependency-check,launch-report
```

## How To Use The Application

### Recommended Workflow

For the best experience, use the application in this order:

1. Generate a key pair.
2. Use the public key for either cipher or attestation generation.
3. Use the matching private key for decipher or attestation verification.

Because Kyber operations are randomized, **your generated Base64 values will differ on every run**. The examples below show the exact field shapes and realistic sample values, but not stable deterministic outputs.

### Operation 1: Generate Kyber Keys

**Page:** `/key-generator.jsp`  
**Endpoint:** `POST /keys/generate`

### Input

| Field | Required | Example |
| --- | --- | --- |
| `kyberSecurityLevel` | yes | `1024` |

### Example

Select `Kyber-1024` and submit the form.

### Example output

```text
Security profile: Kyber1024
Key fingerprint: 4d7e3ba442cbf54fe1e9ac43

Public key (Base64):
MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA...

Private key (Base64):
MIIJQwIBADANBgkqhkiG9w0BAQEFAASCCS0wggkpAgEA...
```

### What you do with the result

- Share the **public key** with senders
- Keep the **private key** confidential
- Reuse the key pair with the matching security level only

### Operation 2: Cipher A Buffer

**Page:** `/buffer-cipher.jsp`  
**Endpoint:** `POST /buffer/cipher`

### Input

| Field | Required | Example |
| --- | --- | --- |
| `kyberSecurityLevel` | yes | `768` |
| `recipientPublicKey` | yes | `MIIC...` |
| `plaintext` | yes | `Confidential payload for tenant A1` |
| `aad` | no | `tenant=A1;scope=demo` |

### Example

```text
kyberSecurityLevel = 768
recipientPublicKey = MIIC...recipient public key...
plaintext = Confidential payload for tenant A1
aad = tenant=A1;scope=demo
```

### Example output

```text
Security profile: Kyber768
Recipient fingerprint: a81d59f6d0197f3016cf0b6d

Encapsulation:
CmM4m+3uNf4xLx8m0n1lY4wJQfQ1x4x5...

Initialization vector:
Z4sU1l5A7M3j5cQq

Ciphertext:
I5jwPqkY4nb1n0m4q9bY1m2d0h2l6k4s8L7Y...

Associated data (Base64):
dGVuYW50PUExO3Njb3BlPWRlbW8=
```

### What the envelope means

- `encapsulation` lets the recipient reconstruct the AES key
- `initializationVector` is the AES-GCM IV
- `ciphertext` is the encrypted payload plus authentication tag
- `aadBase64` must be preserved unchanged for decryption

### Operation 3: Decipher A Buffer

**Page:** `/buffer-decipher.jsp`  
**Endpoint:** `POST /buffer/decipher`

### Input

| Field | Required | Example |
| --- | --- | --- |
| `kyberSecurityLevel` | yes | `768` |
| `recipientPrivateKey` | yes | `MIIJ...` |
| `encapsulation` | yes | `CmM4m+3u...` |
| `initializationVector` | yes | `Z4sU1l5A7M3j5cQq` |
| `ciphertext` | yes | `I5jwPqkY...` |
| `aadBase64` | yes | `dGVuYW50PUExO3Njb3BlPWRlbW8=` |

### Example

Use the envelope returned by **Buffer Cipher** together with the matching private key.

### Example output

```text
Recovered plaintext:
Confidential payload for tenant A1
```

### Important note

If any of these do not match, decryption will fail:

- wrong private key
- wrong Kyber security level
- modified ciphertext
- modified IV
- modified AAD

### Operation 4: Generate An Attestation Envelope

**Page:** `/kyber-signer.jsp`  
**Endpoint:** `POST /attestation/sign`

### Input

| Field | Required | Example |
| --- | --- | --- |
| `kyberSecurityLevel` | yes | `1024` |
| `verifierPublicKey` | yes | `MIIC...` |
| `payload` | yes | `PO-4451 approved for release` |
| `context` | no | `transaction-42` |

### Example

```text
kyberSecurityLevel = 1024
verifierPublicKey = MIIC...verifier public key...
payload = PO-4451 approved for release
context = transaction-42
```

### Example output

```text
Security profile: Kyber1024
Verifier fingerprint: 12f84d22e9c54e784bd4f1aa

Encapsulation:
Rk1mY2NpQ2x4M3drQnErZ3JCSm9U...

MAC:
q8H6rQ50B+7Qq5Jm9n9K4H+P3T9dQnPzU/0e4b4c3Sk=

Context (Base64):
dHJhbnNhY3Rpb24tNDI=
```

### What this operation really provides

This is **receiver-authenticated integrity**, not a public signature:

- only the holder of the matching private key can verify it
- third parties cannot independently validate it
- if you need public verifiability, add a true signature scheme such as Dilithium

### Operation 5: Verify An Attestation Envelope

**Page:** `/kyber-sign-verifier.jsp`  
**Endpoint:** `POST /attestation/verify`

### Input

| Field | Required | Example |
| --- | --- | --- |
| `kyberSecurityLevel` | yes | `1024` |
| `verifierPrivateKey` | yes | `MIIJ...` |
| `payload` | yes | `PO-4451 approved for release` |
| `context` | no | `transaction-42` |
| `encapsulation` | yes | `Rk1mY2Np...` |
| `mac` | yes | `q8H6rQ50...` |

### Example

Submit the same payload and context used during attestation, together with the attestation envelope and the matching private key.

### Example output for a valid attestation

```text
The attestation matched the supplied payload.

Security profile: Kyber1024
Verifier fingerprint: 12f84d22e9c54e784bd4f1aa
Computed MAC:
q8H6rQ50B+7Qq5Jm9n9K4H+P3T9dQnPzU/0e4b4c3Sk=
```

### Example output for a tampered payload

```text
The attestation did not match the supplied payload.
```

This behavior is covered by the test suite.

### Practical End-To-End Example

### Confidential message round trip

1. Generate a `Kyber-768` key pair.
2. Copy the public key into **Buffer Cipher**.
3. Encrypt:

```text
Plaintext: Quarterly revenue draft
AAD: tenant=finance;doc=Q4
```

4. Copy the resulting `encapsulation`, `initializationVector`, `ciphertext`, and `aadBase64`.
5. Open **Buffer Decipher**.
6. Paste the matching private key and the envelope values.
7. Submit and confirm the original plaintext is recovered.

### Integrity attestation round trip

1. Generate a `Kyber-1024` key pair.
2. Copy the public key into **Kyber Attestation**.
3. Attest:

```text
Payload: Contract revision 7 approved
Context: legal-review-2026-03
```

4. Copy the returned `encapsulation` and `mac`.
5. Open **Attestation Verify**.
6. Paste the private key, original payload, original context, encapsulation, and MAC.
7. Submit and confirm the attestation verifies successfully.

If you change the payload or context before verifying, the result should fail.

## Validation Rules And Operational Constraints

- Keys must be valid Base64 or PEM-wrapped Base64.
- Public/private keys must match the selected Kyber security level.
- The decipher IV must decode to exactly **12 bytes**.
- Large text input is accepted, but bounded by servlet-side maximum lengths.
- POST requests require a valid CSRF token generated from the corresponding form.

## What The Tests Verify

The current tests cover:

- successful cipher/decipher round trips across all supported Kyber levels
- successful attestation/verification round trips across all supported Kyber levels
- rejection of a tampered attested payload

GlassFish 7 alignment details:

- the WAR is compiled with `--release 17`
- Jakarta APIs are supplied by `jakarta.jakartaee-web-api` with `provided` scope
- servlet collaborators are CDI-managed through `beans.xml` and `@Inject`

Verified locally with:

```bash
mvn test
```

## Maven Profiles Included

| Profile | Purpose |
| --- | --- |
| `auto-clean` | explicit clean behavior for `target/` |
| `auto-deploy` | copy the WAR to a GlassFish autodeploy directory on install |
| `dependency-check` | run OWASP Dependency-Check during `verify` |
| `launch-report` | open the generated dependency report on Windows |

## Limitations And Design Notes

- This application currently operates on **text buffers** submitted through JSP forms.
- It is designed as a **server-rendered web app**, not a REST API.
- It returns envelope values to the browser for demonstration and operational use.
- The attestation workflow is intentionally named in the UI as a signer/verifier flow, but the implementation correctly uses **Kyber-compatible semantics** rather than pretending Kyber is a signature primitive.

## Future Enhancements

- expose REST endpoints in addition to JSP forms
- add PEM export formatting helpers
- add clipboard or download actions for generated envelopes
- integrate a true PQ signature algorithm such as Dilithium for public verification workflows
- add containerized local development setup

## Repository Notes

- Packaging type: `war`
- Final artifact name: `XaihtKyber.war`
- Welcome page: `index.jsp`
- Default visual style: custom JSP + CSS control surface with a GitHub-friendly, documentation-friendly structure

---

If you place this repository on GitHub, this README is intended to function as both:

- a **project overview**
- a **hands-on operator guide** for every implemented application flow
