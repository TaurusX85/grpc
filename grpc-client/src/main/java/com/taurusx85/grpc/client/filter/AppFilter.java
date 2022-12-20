package com.taurusx85.grpc.client.filter;

import com.taurusx85.grpc.common.AppContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

import static com.taurusx85.grpc.common.GrpcConstants.REQUEST_ID;

@Slf4j
@Component
public class AppFilter extends OncePerRequestFilter {

    private static final int REQUEST_ID_LENGTH = 30;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            AppContext.setRequestId(RandomStringUtils.randomAlphabetic(REQUEST_ID_LENGTH));
            MDC.put(REQUEST_ID, AppContext.getRequestId());
            filterChain.doFilter(request, response);
        } finally {
            AppContext.clean();
            MDC.clear();
        }
    }
}
