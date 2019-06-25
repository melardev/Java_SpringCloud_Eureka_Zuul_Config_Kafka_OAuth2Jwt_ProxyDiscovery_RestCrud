package com.melardev.spring.zuul_service.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class AuthenticationRequestFilter extends ZuulFilter {


    @Value("${app.security.oauth.get_token_path}")
    private List<String> tokenPaths;


    @Value("${app.security.oauth.client1.id}")
    private String clientId;

    @Value("${app.security.oauth.client1.password}")
    private String clientPassword;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext currentContext = RequestContext.getCurrentContext();

        if (tokenPaths.contains(currentContext.getRequest().getRequestURI())) {
            // If the user is trying to get a token from the OAuth Server
            // then we have to provide the secret to the OAuth Server
            byte[] encoded;

            encoded = Base64.getEncoder().encode(String.format("%s:%s", clientId, clientPassword).getBytes(StandardCharsets.UTF_8));
            currentContext.addZuulRequestHeader("Authorization", "Basic " + new String(encoded));

        }
        return null;
    }

}

