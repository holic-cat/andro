package org.apache.http.impl.execchain;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolException;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.util.Args;

@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class ProtocolExec implements ClientExecChain {
    private final HttpProcessor httpProcessor;
    private final Log log = LogFactory.getLog(getClass());
    private final ClientExecChain requestExecutor;

    public ProtocolExec(ClientExecChain clientExecChain, HttpProcessor httpProcessor2) {
        Args.notNull(clientExecChain, "HTTP client request executor");
        Args.notNull(httpProcessor2, "HTTP protocol processor");
        this.requestExecutor = clientExecChain;
        this.httpProcessor = httpProcessor2;
    }

    /* access modifiers changed from: 0000 */
    public void rewriteRequestURI(HttpRequestWrapper httpRequestWrapper, HttpRoute httpRoute) {
        URI uri = httpRequestWrapper.getURI();
        if (uri != null) {
            try {
                httpRequestWrapper.setURI(URIUtils.rewriteURIForRoute(uri, httpRoute));
            } catch (URISyntaxException e) {
                throw new ProtocolException("Invalid URI: " + uri, e);
            }
        }
    }

    public CloseableHttpResponse execute(HttpRoute httpRoute, HttpRequestWrapper httpRequestWrapper, HttpClientContext httpClientContext, HttpExecutionAware httpExecutionAware) {
        Args.notNull(httpRoute, "HTTP route");
        Args.notNull(httpRequestWrapper, "HTTP request");
        Args.notNull(httpClientContext, "HTTP context");
        HttpRequest original = httpRequestWrapper.getOriginal();
        URI uri = null;
        if (original instanceof HttpUriRequest) {
            uri = ((HttpUriRequest) original).getURI();
        } else {
            String uri2 = original.getRequestLine().getUri();
            try {
                uri = URI.create(uri2);
            } catch (IllegalArgumentException e) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug("Unable to parse '" + uri2 + "' as a valid URI; request URI and Host header may be inconsistent", e);
                }
            }
        }
        httpRequestWrapper.setURI(uri);
        rewriteRequestURI(httpRequestWrapper, httpRoute);
        HttpHost httpHost = (HttpHost) httpRequestWrapper.getParams().getParameter(ClientPNames.VIRTUAL_HOST);
        if (httpHost != null && httpHost.getPort() == -1) {
            int port = httpRoute.getTargetHost().getPort();
            if (port != -1) {
                httpHost = new HttpHost(httpHost.getHostName(), port, httpHost.getSchemeName());
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Using virtual host" + httpHost);
            }
        }
        HttpHost httpHost2 = null;
        if (httpHost != null) {
            httpHost2 = httpHost;
        } else if (!(uri == null || !uri.isAbsolute() || uri.getHost() == null)) {
            httpHost2 = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        }
        if (httpHost2 == null) {
            httpHost2 = httpRequestWrapper.getTarget();
        }
        if (httpHost2 == null) {
            httpHost2 = httpRoute.getTargetHost();
        }
        if (uri != null) {
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                CredentialsProvider credentialsProvider = httpClientContext.getCredentialsProvider();
                if (credentialsProvider == null) {
                    credentialsProvider = new BasicCredentialsProvider();
                    httpClientContext.setCredentialsProvider(credentialsProvider);
                }
                credentialsProvider.setCredentials(new AuthScope(httpHost2), new UsernamePasswordCredentials(userInfo));
            }
        }
        httpClientContext.setAttribute("http.target_host", httpHost2);
        httpClientContext.setAttribute("http.route", httpRoute);
        httpClientContext.setAttribute("http.request", httpRequestWrapper);
        this.httpProcessor.process(httpRequestWrapper, httpClientContext);
        CloseableHttpResponse execute = this.requestExecutor.execute(httpRoute, httpRequestWrapper, httpClientContext, httpExecutionAware);
        try {
            httpClientContext.setAttribute("http.response", execute);
            this.httpProcessor.process(execute, httpClientContext);
            return execute;
        } catch (RuntimeException e2) {
            execute.close();
            throw e2;
        } catch (IOException e3) {
            execute.close();
            throw e3;
        } catch (HttpException e4) {
            execute.close();
            throw e4;
        }
    }
}
