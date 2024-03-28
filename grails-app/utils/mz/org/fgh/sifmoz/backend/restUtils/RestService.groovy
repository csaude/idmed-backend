package mz.org.fgh.sifmoz.backend.restUtils

import mz.org.fgh.sifmoz.backend.provincialServer.IProvincialServerService
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServerService
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.grails.web.json.JSONArray
import org.springframework.beans.factory.annotation.Autowired

class RestService {

    private ProvincialServer provincialServer;
    private String baseUrl;
    static Logger logger = LogManager.getLogger(RestServiceProvider.class);

    @Autowired
    IProvincialServerService provincialServerService;

    public RestService(String serverCode, String serverDestination) {
        this.provincialServer = ProvincialServer.findByCodeAndDestination(serverCode, serverDestination);
        if (this.provincialServer == null) throw new RuntimeException("Unable to find provincial server settings.")
        this.baseUrl =  provincialServer.getUrlPath()+provincialServer.getPort();
    }

    private void buildRequestHeader(HttpRequestBase request) {
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("Authorization", "Bearer " + PostgrestAuthenticationUtils.getJWTPermission(this.baseUrl, provincialServer.getUsername(), provincialServer.getPassword(), provincialServer.destination));
        request.setHeader("Accept-Encoding", "gzip, deflate, br");
    }

    public JSONArray get(String uri) {
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
