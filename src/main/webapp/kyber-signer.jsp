<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.xaiht.kyber.crypto.AttestationEnvelope" %>
<%@ include file="/WEB-INF/jspf/helpers.jspf" %>
<%
    String csrfToken = com.xaiht.kyber.web.ServletSupport.ensureCsrfToken(request);
    String selectedSecurityLevel = String.valueOf(request.getAttribute("selectedSecurityLevel") == null ? "1024" : request.getAttribute("selectedSecurityLevel"));
    String verifierPublicKey = String.valueOf(request.getAttribute("verifierPublicKey") == null ? "" : request.getAttribute("verifierPublicKey"));
    String payload = String.valueOf(request.getAttribute("payload") == null ? "" : request.getAttribute("payload"));
    String context = String.valueOf(request.getAttribute("context") == null ? "" : request.getAttribute("context"));
    AttestationEnvelope attestationEnvelope = (AttestationEnvelope) request.getAttribute("attestationEnvelope");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kyber Attestation Generator</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/app.css">
</head>
<body>
<main class="layout">
    <section class="masthead">
        <div class="brand">
            <span class="brand-mark">Attestation</span>
            <h1>Bind a payload to a Kyber-derived shared secret for receiver-authenticated integrity.</h1>
            <p>This page implements the requested signer flow with correct cryptographic semantics for Kyber: encapsulation plus HMAC, not a publicly verifiable digital signature.</p>
        </div>
        <nav class="nav">
            <a href="<%= contextPath %>/index.jsp">Overview</a>
            <a href="<%= contextPath %>/key-generator.jsp">Key Generator</a>
            <a href="<%= contextPath %>/buffer-cipher.jsp">Buffer Cipher</a>
            <a href="<%= contextPath %>/kyber-sign-verifier.jsp">Attestation Verify</a>
        </nav>
    </section>

    <section class="panel">
        <div class="callout warning">
            Use this only where the verifier controls the matching private key. If you need third-party verifiable signatures, introduce a signature scheme such as Dilithium.
        </div>
        <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="notice-error"><%= h(request.getAttribute("errorMessage")) %></div>
        <% } %>
        <form method="post" action="<%= contextPath %>/attestation/sign">
            <input type="hidden" name="csrfToken" value="<%= h(csrfToken) %>">
            <div>
                <label for="kyberSecurityLevel">Kyber security level</label>
                <select id="kyberSecurityLevel" name="kyberSecurityLevel">
                    <option value="512" <%= selected(selectedSecurityLevel, "512") %>>Kyber-512</option>
                    <option value="768" <%= selected(selectedSecurityLevel, "768") %>>Kyber-768</option>
                    <option value="1024" <%= selected(selectedSecurityLevel, "1024") %>>Kyber-1024</option>
                </select>
            </div>
            <div>
                <label for="verifierPublicKey">Verifier public key</label>
                <textarea id="verifierPublicKey" name="verifierPublicKey" class="mono"><%= h(verifierPublicKey) %></textarea>
            </div>
            <div>
                <label for="payload">Payload</label>
                <textarea id="payload" name="payload"><%= h(payload) %></textarea>
            </div>
            <div>
                <label for="context">Context or transaction scope</label>
                <textarea id="context" name="context"><%= h(context) %></textarea>
            </div>
            <button type="submit">Generate Attestation</button>
        </form>
    </section>

    <% if (attestationEnvelope != null) { %>
    <section class="panel">
        <h2>Attestation envelope</h2>
        <div class="result-grid">
            <div class="result-card">
                <strong>Security profile</strong>
                <span class="mono"><%= h(attestationEnvelope.getSecurityLevel().getDisplayName()) %></span>
            </div>
            <div class="result-card">
                <strong>Verifier fingerprint</strong>
                <span class="mono"><%= h(attestationEnvelope.getVerifierFingerprint()) %></span>
            </div>
        </div>
        <div>
            <label>Encapsulation</label>
            <textarea readonly class="mono"><%= h(attestationEnvelope.getEncapsulationBase64()) %></textarea>
        </div>
        <div>
            <label>MAC</label>
            <textarea readonly class="mono"><%= h(attestationEnvelope.getMacBase64()) %></textarea>
        </div>
        <div>
            <label>Context (Base64)</label>
            <textarea readonly class="mono"><%= h(attestationEnvelope.getContextBase64()) %></textarea>
        </div>
    </section>
    <% } %>
</main>
</body>
</html>
