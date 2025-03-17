package net.konjarla.smagent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class RestClientInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.debug(request.getURI().toString());
        log.debug(Arrays.toString(new String[]{new String(body, StandardCharsets.UTF_8)}));
        ClientHttpResponse response = execution.execute(request, body);
        // Copy the response body to a byte array for logging
        byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());

        // Print the response
        log.debug("Response: " + new String(responseBody));

        // Create a new response with the copied body
        return new BufferedClientHttpResponseWrapper(response, responseBody);
        // return execution.execute(request, body);
    }
}
