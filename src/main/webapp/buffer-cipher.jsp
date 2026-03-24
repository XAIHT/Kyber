<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.xaiht.kyber.crypto.CipherEnvelope" %>
<%@ include file="/WEB-INF/jspf/helpers.jspf" %>
<%
    String csrfToken = com.xaiht.kyber.web.ServletSupport.ensureCsrfToken(request);
    String selectedSecurityLevel = String.valueOf(request.getAttribute("selectedSecurityLevel") == null ? "1024" : request.getAttribute("selectedSecurityLevel"));
    String recipientPublicKey = String.valueOf(request.getAttribute("recipientPublicKey") == null ? "" : request.getAttribute("recipientPublicKey"));
    String plaintext = String.valueOf(request.getAttribute("plaintext") == null ? "" : request.getAttribute("plaintext"));
    String aad = String.valueOf(request.getAttribute("aad") == null ? "" : request.getAttribute("aad"));
    CipherEnvelope cipherEnvelope = (CipherEnvelope) request.getAttribute("cipherEnvelope");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kyber Buffer Cipher</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/app.css">
</head>
<body>
<main class="layout">
    <section class="masthead">
        <div class="brand">
            <span class="brand-mark">Cipher</span>
            <h1>Encapsulate a Kyber shared secret and encrypt a buffer with AES-GCM.</h1>
            <p>The recipient public key determines the Kyber parameter profile, and the output envelope can be submitted to the decipher servlet.</p>
        </div>
        <nav class="nav">
            <a href="<%= contextPath %>/index.jsp">Overview</a>
            <a href="<%= contextPath %>/key-generator.jsp">Key Generator</a>
            <a href="<%= contextPath %>/buffer-decipher.jsp">Buffer Decipher</a>
            <a href="<%= contextPath %>/kyber-signer.jsp">Kyber Attestation</a>
        </nav>
    </section>

    <section class="panel">
        <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="notice-error"><%= h(request.getAttribute("errorMessage")) %></div>
        <% } %>
        <form method="post" action="<%= contextPath %>/buffer/cipher">
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
                <label for="recipientPublicKey">Recipient public key</label>
                <textarea id="recipientPublicKey" name="recipientPublicKey" class="mono"><%= h(recipientPublicKey) %></textarea>
            </div>
            <div>
                <label for="plaintext">Plaintext buffer</label>
                <textarea id="plaintext" name="plaintext"><%= h(plaintext) %></textarea>
            </div>
            <div>
                <label for="aad">Associated data</label>
                <textarea id="aad" name="aad"><%= h(aad) %></textarea>
            </div>
            <button type="submit">Cipher Buffer</button>
        </form>
    </section>

    <% if (cipherEnvelope != null) { %>
    <section class="panel">
        <h2>Cipher envelope</h2>
        <div class="result-grid">
            <div class="result-card">
                <strong>Security profile</strong>
                <span class="mono"><%= h(cipherEnvelope.getSecurityLevel().getDisplayName()) %></span>
            </div>
            <div class="result-card">
                <strong>Recipient fingerprint</strong>
                <span class="mono"><%= h(cipherEnvelope.getRecipientFingerprint()) %></span>
            </div>
        </div>
        <div>
            <label>Encapsulation</label>
            <textarea readonly class="mono"><%= h(cipherEnvelope.getEncapsulationBase64()) %></textarea>
        </div>
        <div>
            <label>Initialization vector</label>
            <textarea readonly class="mono"><%= h(cipherEnvelope.getInitializationVectorBase64()) %></textarea>
        </div>
        <div>
            <label>Ciphertext</label>
            <textarea readonly class="mono"><%= h(cipherEnvelope.getCipherTextBase64()) %></textarea>
        </div>
        <div>
            <label>Associated data (Base64)</label>
            <textarea readonly class="mono"><%= h(cipherEnvelope.getAadBase64()) %></textarea>
        </div>
    </section>
    <% } %>
</main>
</body>
</html>
