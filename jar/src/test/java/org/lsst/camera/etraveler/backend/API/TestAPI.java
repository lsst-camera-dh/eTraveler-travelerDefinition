package org.lsst.camera.etraveler.backend.API;

import com.fasterxml.jackson.core.JsonProcessingException;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

public class TestAPI {
  private CloseableHttpClient m_httpclient; 

  private String     m_db="Prod";
  private ByteArrayOutputStream m_postPayload;

  private static final String
    prodServerURL="http://lsst-camera.slac.stanford.edu/eTraveler/";
  private static final String
    devServerURL="http://lsst-camera-dev.slac.stanford.edu/eTraveler/";
  // use prod server by default
  private String m_url = prodServerURL;
  
  private HashMap<String, Object> m_params;

  public class MyResponseHandler implements ResponseHandler<ByteArrayOutputStream> {
    public ByteArrayOutputStream handleResponse(final HttpResponse response) throws
      ClientProtocolException, IOException {
      System.out.println("Inside handleResponse\n");
      int status = response.getStatusLine().getStatusCode();
      System.out.println("Returned response was ");
      System.out.println(status);
      if (status >= 200 && status < 305) {
        HttpEntity entity = response.getEntity();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.out.println("Created ByteArrayOutputStream\n");
        entity.writeTo(out);
        return out;
      } else {
        throw new ClientProtocolException();
      }
    }
  }


  @Before
  public void setup() {
    m_url += m_db;
    m_url += "/Results/";
    m_params = new HashMap<String, Object>();
    m_params.put("operator", "jrb");
  }
  
  @Test
  public void testGetRunInfo() throws UnsupportedEncodingException {
      System.out.println("Running testGetRunInfo test");

      m_httpclient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
      m_params.put("activityId", 200);
      String payload;
      try {
        payload = new ObjectMapper().writeValueAsString(m_params);
        System.out.println("The payload: " + payload);
      } catch (JsonProcessingException ex) {
          System.out.println("ObjectMapper.writeValueAsString failed");
          return;
      }
      
      m_url += "getRunInfo";
      System.out.println("target url is " + m_url + "\n");
      HttpPost httppost = new HttpPost(m_url);
      List<NameValuePair> params = new ArrayList<NameValuePair>(1);
      params.add(new BasicNameValuePair("jsonObject", payload));
                 
      httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
      HttpResponse response;
      MyResponseHandler hand = new MyResponseHandler();

      try {

        ByteArrayOutputStream out  = m_httpclient.execute(httppost, hand);
        System.out.println("Did the execute\n");

        System.out.println("Size of returned output is ");
        System.out.println(out.size());
        System.out.println("Out is:\n");
        System.out.println(out.toString());
      } catch (IOException ex) {
        System.out.println("post failed with message " + ex.getMessage());
      }
  }
}
