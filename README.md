# XaihtKyber

[![Java 21+](https://img.shields.io/badge/Java-21%2B-0b5fff?style=for-the-badge)](https://www.oracle.com/java/)
[![Maven WAR](https://img.shields.io/badge/Maven-WAR-b14e2d?style=for-the-badge)](https://maven.apache.org/)
[![Jakarta EE 10](https://img.shields.io/badge/Jakarta_EE-10-1a7f4b?style=for-the-badge)](https://jakarta.ee/)

XaihtKyber is a Maven `war` project that exposes CRYSTALS-Kyber workflows through Jakarta Servlet + JSP pages.

It provides:

- Kyber key generation
- Buffer encryption with Kyber KEM + AES-256-GCM
- Buffer decryption with Kyber decapsulation + AES-256-GCM
- Receiver-authenticated integrity attestation with Kyber + HKDF + HMAC-SHA-256
- Attestation verification with the matching Kyber private key

Important cryptographic note:

- Kyber is a KEM, not a digital signature algorithm.
- The "signer/verifier" pages in this project implement two-party integrity attestation, not third-party verifiable signatures.

## What A Developer Needs

This is the minimum practical stack for working on the project safely:

| Component | Required | Why |
| --- | --- | --- |
| JDK | Java 21 or newer | `pom.xml` compiles with `maven.compiler.release=21` and enforces Java `[21,)` |
| Maven | Maven 3.9+ recommended | There is no Maven Wrapper in this repository |
| Application server | GlassFish 7 recommended | The app depends on Jakarta EE 10 web APIs, JSP, and CDI |
| Alternative server | Payara 6 or another Jakarta EE 10 Web Profile runtime | Safe alternative if it provides Servlet 6, JSP, and CDI |

Read this carefully:

- Use a JDK, not a JRE.
- Java 21 is the baseline target. Later JDKs may work, but the project is configured and verified against Java 21.
- A plain servlet container is not the safest assumption here.
- The project uses `jakarta.jakartaee-web-api` with `provided` scope and injects services with CDI (`@Inject`), so a Jakarta EE 10 Web Profile server is the correct runtime target.
- There is no embedded server and no `main()` entry point. You build a WAR and deploy it to an application server.

## Verified From The Repository

These facts were confirmed from the codebase:

- Packaging: `war`
- Final artifact: `target/XaihtKyber.war`
- Java release target: `21`
- Jakarta API dependency: `jakarta.jakartaee-web-api:10.0.0` with `provided` scope
- PQC provider: `org.bouncycastle:bcprov-jdk18on:1.78.1.redhat-00002`
- Welcome page: `index.jsp`
- CDI enabled: `src/main/webapp/WEB-INF/beans.xml`
- Web descriptor version: Servlet `6.0`
- No database, message broker, or external secret store is required to compile or run the app
- Optional environment variables:
  - `GLASSFISH_HOME` for the `auto-deploy` Maven profile
  - `NVD_API_KEY` for faster OWASP Dependency-Check usage

Local verification performed in this workspace:

- Maven `3.9.12`
- JDK `21.0.6`
- `mvn test` passed
- `mvn package` passed
- The WAR was produced at `target/XaihtKyber.war`

## Project Layout

```text
src/
├─ main/
│  ├─ java/com/xaiht/kyber/
│  │  ├─ crypto/   # envelopes, enums, validation, provider registry
│  │  ├─ service/  # key generation, cipher, attestation logic
│  │  └─ web/      # servlets and servlet helpers
│  └─ webapp/
│     ├─ *.jsp
│     ├─ assets/
│     └─ WEB-INF/
└─ test/
   └─ java/com/xaiht/kyber/service/
```

## Compile The Project

### 1. Check your toolchain

On Windows PowerShell:

```powershell
java -version
javac -version
mvn -version
```

You want to see:

- `javac` available
- Java 21 or newer
- Maven available on `PATH`

If `javac` is missing, you are using a JRE or your `JAVA_HOME` is wrong.

### 2. Build and run tests

```powershell
mvn clean test
```

This validates:

- source compilation
- test compilation
- Kyber cipher round-trip tests
- Kyber attestation verification tests

### 3. Build the deployable WAR

```powershell
mvn clean package
```

Expected output artifact:

```text
target/XaihtKyber.war
```

If `mvn clean package` succeeds, the project has compiled correctly.

## Run With Docker

This repository includes a multi-stage Dockerfile that builds the WAR with Maven
and runs it on Payara Server Web Profile 6.

Why this image:

- `payara/server-web` matches the Jakarta EE Web Profile requirements of this app
- the Payara 6 line is aligned with Jakarta EE 10
- the image runs the server as the non-root `payara` user
- only the application HTTP port needs to be exposed for normal use

Build the image:

```powershell
docker build -t xaiht-kyber .
```

Run the container:

```powershell
docker run --rm -p 8080:8080 xaiht-kyber
```

Then open:

```text
http://localhost:8080/XaihtKyber/
```

Operational note:

- The Dockerfile does not expose Payara admin port `4848`.
- Keep it that way unless you explicitly need remote administration.
- If you later publish this image, pin the base image by digest for stronger supply-chain control.

## Quick Start For New Developers

If you want the shortest correct path:

1. Install JDK 21.
2. Install Maven 3.9+.
3. Install GlassFish 7.
4. Confirm `java`, `javac`, and `mvn` work from the terminal.
5. Run `mvn clean package`.
6. Deploy `target/XaihtKyber.war` to GlassFish 7.
7. Open `http://localhost:8080/XaihtKyber/`.

## Set Up GlassFish 7

GlassFish 7 is the safest documented target for this repository.

Before deploying, make sure:

- GlassFish 7 is installed
- a domain exists, usually `domain1`
- the domain is started

Typical GlassFish URL after deployment:

```text
http://localhost:8080/XaihtKyber/
```

The context path is derived from the WAR name, so `XaihtKyber.war` becomes `XaihtKyber`.

## Deploy To GlassFish 7

### Option A: Manual copy to `autodeploy`

1. Build the WAR:

```powershell
mvn clean package
```

1. Copy it into the domain autodeploy folder:

```powershell
Copy-Item .\target\XaihtKyber.war "C:\path\to\glassfish7\glassfish\domains\domain1\autodeploy\"
```

1. Start the domain if needed, then open:

```text
http://localhost:8080/XaihtKyber/
```

### Option B: Use the Maven `auto-deploy` profile

The repository already contains an `auto-deploy` profile in `pom.xml`.

It copies the built WAR during the `install` phase to:

```text
%GLASSFISH_HOME%\glassfish\domains\domain1\autodeploy
```

Important:

- Set `GLASSFISH_HOME` to the GlassFish installation root.
- Do not set it to the nested `glassfish` folder.

Example:

- Correct: `C:\glassfish7`
- Wrong: `C:\glassfish7\glassfish`

PowerShell example:

```powershell
$env:GLASSFISH_HOME = "C:\glassfish7"
mvn clean install -Pauto-deploy
```

If your domain is not `domain1`, override it:

```powershell
mvn clean install -Pauto-deploy -Dglassfish.domain.name=customDomain
```

If you want to bypass `GLASSFISH_HOME` and point directly to the folder:

```powershell
mvn clean install -Pauto-deploy -Dglassfish.autodeploy.dir="C:\path\to\glassfish7\glassfish\domains\domain1\autodeploy"
```

### Option C: Deploy with `asadmin`

If you prefer explicit GlassFish administration:

```powershell
C:\path\to\glassfish7\bin\asadmin.bat start-domain domain1
C:\path\to\glassfish7\bin\asadmin.bat deploy --force=true .\target\XaihtKyber.war
```

## Runtime Requirements And Assumptions

This application expects a Jakarta EE web runtime that provides:

- Servlet 6.0
- JSP
- CDI 4.0
- Jakarta EE 10 web APIs

The code specifically uses:

- `@WebServlet`
- `@Inject`
- `beans.xml`
- JSP pages under `src/main/webapp`

That is why GlassFish 7 and Payara 6 are appropriate targets.

## What The Application Does

### Key generation

`KyberKeyService` generates:

- a Base64 X.509 public key
- a Base64 PKCS#8 private key
- a short SHA-256-based fingerprint

### Buffer cipher

`KyberCipherService`:

1. Validates the Kyber public key and selected security level.
2. Uses Kyber encapsulation to derive a shared secret.
3. Produces an AES-256 key.
4. Encrypts the plaintext with `AES/GCM/NoPadding`.
5. Returns an envelope containing:
   - security profile
   - recipient fingerprint
   - encapsulation
   - Base64 IV
   - Base64 ciphertext
   - Base64 AAD

### Buffer decipher

`KyberCipherService` decapsulation recovers the AES key and decrypts the envelope.

### Attestation

`KyberAttestationService`:

1. Encapsulates a shared secret to the verifier public key.
2. Derives an HMAC key with HKDF-SHA-256.
3. Computes `HMAC-SHA-256(payload || 0x00 || context)`.
4. Returns encapsulation, MAC, and Base64-encoded context.

### Verification

The verifier uses the matching private key to decapsulate the shared secret and recompute the MAC.

## Supported Security Levels

- `Kyber-512`
- `Kyber-768`
- `Kyber-1024`

Accepted form values in the code:

- `512`, `768`, `1024`
- `Kyber512`, `Kyber768`, `Kyber1024`

The JSP UI submits numeric values.

## Pages And Endpoints

| Page | Purpose | POST endpoint |
| --- | --- | --- |
| `/index.jsp` | landing page | none |
| `/key-generator.jsp` | generate key pairs | `/keys/generate` |
| `/buffer-cipher.jsp` | encrypt plaintext | `/buffer/cipher` |
| `/buffer-decipher.jsp` | decrypt ciphertext envelope | `/buffer/decipher` |
| `/kyber-signer.jsp` | generate attestation envelope | `/attestation/sign` |
| `/kyber-sign-verifier.jsp` | verify attestation envelope | `/attestation/verify` |

## Validation Rules That Matter In Practice

The application enforces:

- CSRF token validation on every POST
- security headers on responses
- security-level matching between the selected profile and submitted keys
- size limits on inputs
- Base64 validation on envelope fields

Important operational details:

- Public and private keys may be plain Base64 or PEM-wrapped Base64.
- The decryption IV must decode to exactly 12 bytes.
- If you encrypted with empty AAD, the `aadBase64` field is expected to remain present but can be blank.
- The attestation result includes `context` as Base64 for display, but verification still expects the original raw context text from the user, not the Base64 display value.

## Security Headers Applied By The Web Layer

`ServletSupport` applies:

- `Cache-Control: no-store, no-cache, must-revalidate, max-age=0`
- `Pragma: no-cache`
- `Expires: 0`
- `X-Content-Type-Options: nosniff`
- `Content-Security-Policy`
- `Referrer-Policy: no-referrer`
- `X-Frame-Options: DENY`

## Tests Included

Current automated tests cover:

- buffer cipher and decipher round trips across all supported Kyber levels
- attestation and verification round trips across all supported Kyber levels
- rejection of a tampered attested payload

Run them with:

```powershell
mvn test
```

## Maven Profiles

| Profile | Purpose | When to use it |
| --- | --- | --- |
| `auto-clean` | Explicit `target/` cleanup configuration | Usually not needed beyond normal `mvn clean` |
| `auto-deploy` | Copy WAR to GlassFish autodeploy directory during `install` | Use for local GlassFish deployments |
| `dependency-check` | Run OWASP Dependency-Check during `verify` | Use for dependency auditing |
| `launch-report` | Open the generated dependency report on Windows | Use together with `dependency-check` |

Examples:

```powershell
mvn verify -Pdependency-check
mvn verify -Pdependency-check,launch-report
```

Note:

- `launch-report` is Windows-oriented because it uses PowerShell `Start-Process`.
- `auto-deploy` runs during `install`, so use `mvn clean install -Pauto-deploy`, not just `package`.

## Common Mistakes

### "It builds on my machine but does not deploy"

Most likely causes:

- deploying to a server that does not provide CDI/JSP/Jakarta EE 10 web APIs
- using a plain servlet container without the missing Jakarta EE pieces
- deploying an old WAR after changing the source

### "Java is installed but Maven cannot compile"

Most likely causes:

- `JAVA_HOME` points to a JRE or wrong JDK
- `javac` is not on `PATH`
- Java version is below 21

### "The auto-deploy profile cannot find GlassFish"

Most likely causes:

- `GLASSFISH_HOME` points to the wrong folder
- the domain name is not `domain1`
- the autodeploy directory does not exist yet

### "Decrypt or verify fails even though the values look correct"

Check all of these:

- the same Kyber security level was used end-to-end
- the private key matches the public key used originally
- the `aadBase64` value was preserved exactly for decryption
- the original raw `context` text was re-entered for verification
- ciphertext, encapsulation, MAC, and IV were not modified

## Typical Developer Workflow

1. Generate a key pair on `/key-generator.jsp`.
2. Use the public key in either cipher or attestation generation.
3. Preserve the returned envelope values exactly.
4. Use the matching private key for decipher or verification.

## In Short

If you only need the essential instructions:

```powershell
mvn clean package
Copy-Item .\target\XaihtKyber.war "C:\path\to\glassfish7\glassfish\domains\domain1\autodeploy\"
```

Then open:

```text
http://localhost:8080/XaihtKyber/
```

That is the intended local developer path for this repository.
