package com.melardev.cloud.rest.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CatchAllFilters extends OncePerRequestFilter {
    Logger LOGGER = LoggerFactory.getLogger(CatchAllFilters.class);

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        LOGGER.debug("Rest API triggered: {}", httpServletRequest.getRequestURI());
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
