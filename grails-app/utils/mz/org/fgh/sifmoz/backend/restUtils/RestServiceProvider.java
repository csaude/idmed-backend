package mz.org.fgh.sifmoz.backend.restUtils;

import groovy.transform.CompileStatic;
import mz.org.fgh.sifmoz.backend.provincialServer.IProvincialServerService;
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer;
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServerService;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.grails.web.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.HttpURLConnection;

@CompileStatic
public class RestServiceProvider {

    private final ProvincialServer provincialServer;
    private final String baseUrl;
    static Logger logger = LogManager.getLogger(RestServiceProvider.class);

    ProvincialServerService provincialServerService;

    public RestServiceProvider(String serverCode, String serverDestination) {
        this.provincialServer = provincialServerService.getByCodeAndDestination(serverCode, serverDestination);
        this.baseUrl =  provincialServer.getUrlPath()+provincialServer.getPort();
    }

    private void buildRequestHeader(HttpRequestBase request) {
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("Authorization", "Bearer " + PostgrestAuthenticationUtils.getJWTPermission(this.baseUrl, provincialServer.getUsername(), provincialServer.getPassword(),provincialServer.getDestination()));
        request.setHeader("Accept-Encoding", "gzip, deflate, br");
    }

    public JSONArray get(String uri) {
        String result = "";
        try {
            CloseableHttpClient client =  HttpClients.createDefault();
            HttpGet request = new HttpGet(this.baseUrl+uri);
            buildRequestHeader(request);
            CloseableHttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                String result1 = EntityUtils.toString(response.getEntity());
                logger.info(result1);
                return new JSONArray(result1);
            }
            logger.info(response.getStatusLine().getStatusCode());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public int patch(String uri, String object) {
        try {
            CloseableHttpClient client =  HttpClients.createDefault();
            HttpPatch request = new HttpPatch(this.baseUrl+uri);
            buildRequestHeader(request);
            request.setEntity(new StringEntity(object));
            CloseableHttpResponse response = client.execute(request);

            logger.info(response.getStatusLine().getStatusCode());
            return response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int post(String uri, String object) {
        try {
            CloseableHttpClient client =  HttpClients.createDefault();
            HttpPost request = new HttpPost(this.baseUrl+uri);
            buildRequestHeader(request);
            request.setEntity(new StringEntity(object));
            CloseableHttpResponse response = client.execute(request);

            logger.info(response.getStatusLine().getStatusCode());
            return response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
