package net.konjarla.smagent.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class RequestLoggingInterceptor implements HttpRequestInterceptor {

    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        try {
            log.debug("Request Method: {}", request.getMethod());
            log.debug("Request URL: {}", request.getUri().toString());
            log.debug("Request Headers: {}", Arrays.toString(request.getHeaders()));
            if (entity != null) {
                log.debug(entity.toString());
            }
        } catch (Exception e) {
            throw new HttpException("Error with RequestLoggingInterceptor", e);
        }
    }
}
