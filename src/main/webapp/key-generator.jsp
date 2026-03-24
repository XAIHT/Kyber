<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.xaiht.kyber.crypto.KyberKeyMaterial" %>
<%@ include file="/WEB-INF/jspf/helpers.jspf" %>
<%
    String csrfToken = com.xaiht.kyber.web.ServletSupport.ensureCsrfToken(request);
    String selectedSecurityLevel = (String) request.getAttribute("selectedSecurityLevel");
    if (selectedSecurityLevel == null) {
        selectedSecurityLevel = "1024";
    }
    KyberKeyMaterial generatedKeys = (KyberKeyMaterial) request.getAttribute("generatedKeys");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kyber Key Generator</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/app.css">
</head>
<body>
<main class="layout">
    <section class="masthead">
        <div class="brand">
            <span class="brand-mark">Key Generation</span>
            <h1>Generate Kyber key material per security profile.</h1>
            <p>Create Base64-encoded X.509 public keys and PKCS#8 private keys compatible with the XaihtKyber servlets.</p>
        </div>
        <nav class="nav">
            <a href="<%= contextPath %>/index.jsp">Overview</a>
            <a href="<%= contextPath %>/buffer-cipher.jsp">Buffer Cipher</a>
            <a href="<%= contextPath %>/buffer-decipher.jsp">Buffer Decipher</a>
            <a href="<%= contextPath %>/kyber-signer.jsp">Kyber Attestation</a>
            <a href="<%= contextPath %>/kyber-sign-verifier.jsp">Attestation Verify</a>
        </nav>
    </section>

    <section class="panel">
        <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="notice-error"><%= h(request.getAttribute("errorMessage")) %></div>
        <% } %>
        <form method="post" action="<%= contextPath %>/keys/generate">
            <input type="hidden" name="csrfToken" value="<%= h(csrfToken) %>">
            <div>
                <label for="kyberSecurityLevel">Kyber security level</label>
                <select id="kyberSecurityLevel" name="kyberSecurityLevel">
                    <option value="512" <%= selected(selectedSecurityLevel, "512") %>>Kyber-512</option>
                    <option value="768" <%= selected(selectedSecurityLevel, "768") %>>Kyber-768</option>
                    <option value="1024" <%= selected(selectedSecurityLevel, "1024") %>>Kyber-1024</option>
                </select>
            </div>
            <div class="callout">
                Public keys can be distributed to senders. Private keys must remain in a confidential trust boundary.
            </div>
            <button type="submit">Generate Key Pair</button>
        </form>
    </section>

    <% if (generatedKeys != null) { %>
    <section class="panel">
        <h2>Generated output</h2>
        <div class="result-grid">
            <div class="result-card">
                <strong>Security profile</strong>
                <span class="mono"><%= h(generatedKeys.getSecurityLevel().getDisplayName()) %></span>
            </div>
            <div class="result-card">
                <strong>Key fingerprint</strong>
                <span class="mono"><%= h(generatedKeys.getKeyFingerprint()) %></span>
            </div>
        </div>
        <div style="margin-top: 1rem;">
            <label>Public key</label>
            <textarea readonly class="mono"><%= h(generatedKeys.getPublicKeyBase64()) %></textarea>
        </div>
        <div>
            <label>Private key</label>
            <textarea readonly class="mono"><%= h(generatedKeys.getPrivateKeyBase64()) %></textarea>
        </div>
    </section>
    <% } %>
</main>
</body>
</html>
