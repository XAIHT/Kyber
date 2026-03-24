<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.xaiht.kyber.crypto.VerificationResult" %>
<%@ include file="/WEB-INF/jspf/helpers.jspf" %>
<%
    String csrfToken = com.xaiht.kyber.web.ServletSupport.ensureCsrfToken(request);
    String selectedSecurityLevel = String.valueOf(request.getAttribute("selectedSecurityLevel") == null ? "1024" : request.getAttribute("selectedSecurityLevel"));
    String verifierPrivateKey = String.valueOf(request.getAttribute("verifierPrivateKey") == null ? "" : request.getAttribute("verifierPrivateKey"));
    String payload = String.valueOf(request.getAttribute("payload") == null ? "" : request.getAttribute("payload"));
    String context = String.valueOf(request.getAttribute("context") == null ? "" : request.getAttribute("context"));
    String encapsulation = String.valueOf(request.getAttribute("encapsulation") == null ? "" : request.getAttribute("encapsulation"));
    String mac = String.valueOf(request.getAttribute("mac") == null ? "" : request.getAttribute("mac"));
    VerificationResult verificationResult = (VerificationResult) request.getAttribute("verificationResult");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kyber Attestation Verification</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/app.css">
</head>
<body>
<main class="layout">
    <section class="masthead">
        <div class="brand">
            <span class="brand-mark">Verification</span>
            <h1>Verify the receiver-authenticated integrity envelope.</h1>
            <p>The verifier decapsulates the shared secret with the Kyber private key and recomputes the HMAC over the payload and context.</p>
        </div>
        <nav class="nav">
            <a href="<%= contextPath %>/index.jsp">Overview</a>
            <a href="<%= contextPath %>/key-generator.jsp">Key Generator</a>
            <a href="<%= contextPath %>/kyber-signer.jsp">Kyber Attestation</a>
            <a href="<%= contextPath %>/buffer-decipher.jsp">Buffer Decipher</a>
        </nav>
    </section>

    <section class="panel">
        <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="notice-error"><%= h(request.getAttribute("errorMessage")) %></div>
        <% } %>
        <form method="post" action="<%= contextPath %>/attestation/verify">
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
                <label for="verifierPrivateKey">Verifier private key</label>
                <textarea id="verifierPrivateKey" name="verifierPrivateKey" class="mono"><%= h(verifierPrivateKey) %></textarea>
            </div>
            <div>
                <label for="payload">Payload</label>
                <textarea id="payload" name="payload"><%= h(payload) %></textarea>
            </div>
            <div>
                <label for="context">Context or transaction scope</label>
                <textarea id="context" name="context"><%= h(context) %></textarea>
            </div>
            <div>
                <label for="encapsulation">Encapsulation</label>
                <textarea id="encapsulation" name="encapsulation" class="mono"><%= h(encapsulation) %></textarea>
            </div>
            <div>
                <label for="mac">MAC</label>
                <textarea id="mac" name="mac" class="mono"><%= h(mac) %></textarea>
            </div>
            <button type="submit">Verify Attestation</button>
        </form>
    </section>

    <% if (verificationResult != null) { %>
    <section class="panel">
        <h2>Verification result</h2>
        <div class="<%= verificationResult.isVerified() ? "notice-success" : "notice-error" %>">
            <%= verificationResult.isVerified() ? "The attestation matched the supplied payload." : "The attestation did not match the supplied payload." %>
        </div>
        <div class="result-grid">
            <div class="result-card">
                <strong>Security profile</strong>
                <span class="mono"><%= h(verificationResult.getSecurityLevel().getDisplayName()) %></span>
            </div>
            <div class="result-card">
                <strong>Verifier fingerprint</strong>
                <span class="mono"><%= h(verificationResult.getVerifierFingerprint()) %></span>
            </div>
        </div>
        <div>
            <label>Computed MAC</label>
            <textarea readonly class="mono"><%= h(verificationResult.getComputedMacBase64()) %></textarea>
        </div>
    </section>
    <% } %>
</main>
</body>
</html>
