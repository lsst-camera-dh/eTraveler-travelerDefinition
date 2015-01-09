/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackend.rest.client;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

/**
 *
 * @author jrb
 */
public class RestConnection extends HttpURLConnection {
  private URL url;
  public RestConnection(URL url) throws MalformedURLException {
    super(url);
    this.url = url;
    setRequestProperty("Accept", "application/json");
  }
  public void connect() {}
  public void disconnect() {}
  public boolean usingProxy() { return false;}
  public InputStream getInputStream() throws IOException {
    return url.openStream();
  }
  
}
