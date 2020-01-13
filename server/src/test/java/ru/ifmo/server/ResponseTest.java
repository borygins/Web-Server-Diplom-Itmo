package ru.ifmo.server;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ResponseTest {

    private static String url = "http://localhost:8080";

    @Test
    public void testStatusCode() throws IOException {
        HttpUriRequest request = new HttpGet(url);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        System.out.println(httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testType() throws IOException {
        HttpUriRequest request=new HttpGet(url);
        HttpResponse httpResponse=HttpClientBuilder.create().build().execute(request);
        Assert.assertEquals("text/plain", ContentType.getOrDefault(httpResponse.getEntity()).getMimeType());
    }

}
