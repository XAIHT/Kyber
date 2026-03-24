<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>XaihtKyber Control Surface</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/app.css">
</head>
<body>
<main class="layout">
    <section class="masthead">
        <div class="brand">
            <span class="brand-mark">XaihtKyber</span>
            <h1>CRYSTALS-Kyber operations for enterprise transport security.</h1>
            <p>
                This control surface provides production-oriented web flows for Kyber key generation,
                buffer encryption and decryption, and receiver-authenticated integrity attestations built from
                Kyber shared secrets.
            </p>
        </div>
        <nav class="nav">
            <a href="<%= contextPath %>/key-generator.jsp">Key Generator</a>
            <a href="<%= contextPath %>/buffer-cipher.jsp">Buffer Cipher</a>
            <a href="<%= contextPath %>/buffer-decipher.jsp">Buffer Decipher</a>
            <a href="<%= contextPath %>/kyber-signer.jsp">Kyber Attestation</a>
            <a href="<%= contextPath %>/kyber-sign-verifier.jsp">Attestation Verify</a>
        </nav>
    </section>

    <section class="grid columns-2">
        <article class="panel">
            <h2>Implemented flows</h2>
            <p>
                The cipher and decipher pipeline uses Kyber-512, Kyber-768, or Kyber-1024 as a KEM to derive
                an AES-256 session key, then protects the payload with AES-GCM.
            </p>
            <p>
                The requested signer and verifier workflow is implemented as a receiver-authenticated
                integrity attestation. Kyber does not produce public digital signatures, so this path derives a
                shared secret through Kyber encapsulation and binds the payload with HKDF plus HMAC-SHA-256.
            </p>
        </article>

        <article class="panel">
            <h2>Operational guidance</h2>
            <p>
                Generate a key pair first, distribute only the public key to senders, and protect the private key
                as confidential server-side material. All application responses are marked non-cacheable and all
                POST flows require a CSRF token.
            </p>
            <div class="callout warning">
                Digital signatures should use a signature primitive such as CRYSTALS-Dilithium, Falcon, or
                SPHINCS+. Kyber can safely provide confidentiality and two-party integrity binding, but not
                third-party verifiable signatures.
            </div>
        </article>
    </section>

    <p class="footer">
        XaihtKyber is structured for servlet-container deployment as a Maven WAR with Bouncy Castle PQC support.
    </p>
</main>
</body>
</html>
