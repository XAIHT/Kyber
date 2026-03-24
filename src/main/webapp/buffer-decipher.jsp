<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jspf/helpers.jspf" %>
<%
    String csrfToken = com.xaiht.kyber.web.ServletSupport.ensureCsrfToken(request);
    String selectedSecurityLevel = String.valueOf(request.getAttribute("selectedSecurityLevel") == null ? "1024" : request.getAttribute("selectedSecurityLevel"));
    String recipientPrivateKey = String.valueOf(request.getAttribute("recipientPrivateKey") == null ? "" : request.getAttribute("recipientPrivateKey"));
    String encapsulation = String.valueOf(request.getAttribute("encapsulation") == null ? "" : request.getAttribute("encapsulation"));
    String initializationVector = String.valueOf(request.getAttribute("initializationVector") == null ? "" : request.getAttribute("initializationVector"));
    String ciphertext = String.valueOf(request.getAttribute("ciphertext") == null ? "" : request.getAttribute("ciphertext"));
    String aadBase64 = String.valueOf(request.getAttribute("aadBase64") == null ? "" : request.getAttribute("aadBase64"));
    String plaintextResult = String.valueOf(request.getAttribute("plaintextResult") == null ? "" : request.getAttribute("plaintextResult"));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kyber Buffer Decipher</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/app.css">
</head>
<body>
<main class="layout">
    <section class="masthead">
        <div class="brand">
            <span class="brand-mark">Decipher</span>
            <h1>Decapsulate the shared secret and recover the original plaintext buffer.</h1>
            <p>Submit the ciphertext envelope components together with the recipient private key that matches the selected Kyber security profile.</p>
        </div>
        <nav class="nav">
            <a href="<%= contextPath %>/index.jsp">Overview</a>
            <a href="<%= contextPath %>/key-generator.jsp">Key Generator</a>
            <a href="<%= contextPath %>/buffer-cipher.jsp">Buffer Cipher</a>
            <a href="<%= contextPath %>/kyber-sign-verifier.jsp">Attestation Verify</a>
        </nav>
    </section>

    <section class="panel">
        <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="notice-error"><%= h(request.getAttribute("errorMessage")) %></div>
        <% } %>
        <form method="post" action="<%= contextPath %>/buffer/decipher">
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
                <label for="recipientPrivateKey">Recipient private key</label>
                <textarea id="recipientPrivateKey" name="recipientPrivateKey" class="mono"><%= h(recipientPrivateKey) %></textarea>
            </div>
            <div>
                <label for="encapsulation">Encapsulation</label>
                <textarea id="encapsulation" name="encapsulation" class="mono"><%= h(encapsulation) %></textarea>
            </div>
            <div>
                <label for="initializationVector">Initialization vector (Base64)</label>
                <input id="initializationVector" name="initializationVector" class="mono" value="<%= h(initializationVector) %>">
            </div>
            <div>
                <label for="ciphertext">Ciphertext (Base64)</label>
                <textarea id="ciphertext" name="ciphertext" class="mono"><%= h(ciphertext) %></textarea>
            </div>
            <div>
                <label for="aadBase64">Associated data (Base64)</label>
                <textarea id="aadBase64" name="aadBase64" class="mono"><%= h(aadBase64) %></textarea>
            </div>
            <button type="submit">Decipher Buffer</button>
        </form>
    </section>

    <% if (request.getAttribute("plaintextResult") != null) { %>
    <section class="panel">
        <h2>Recovered plaintext</h2>
        <textarea readonly><%= h(plaintextResult) %></textarea>
    </section>
    <% } %>
</main>
</body>
</html>
