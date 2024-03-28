package mz.org.fgh.sifmoz.backend.restUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grails.web.json.JSONArray;
import org.grails.web.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class PostgrestAuthenticationUtils {

    public static String getJWTPermission(String baseUri, String username, String pass, String destination) {
        HttpURLConnection connection = null;
        String urlIDart = baseUri + "/rpc/login";
        String urlIDMED = baseUri + "/api/login";
        String url = null;
        String parameters = null;

        if (destination.contains("IDMED")) {
            url = urlIDMED;
            parameters = "{\"username\":\"" + username + "\",\"password\":\"" + pass + "\"}";
        }  else if (destination.contains("METADATA")) {
            url = urlIDMED;
            parameters = "{\"username\":\"" + username + "\",\"password\":\"" + pass + "\"}";
        } else {
            url = urlIDart;
            parameters = "{\"username\":\"" + username + "\",\"pass\":\"" + pass + "\"}";
        }


        String result = "";
        int code = 200;
        try {
            URL siteURL = new URL(url);
            connection = (HttpURLConnection) siteURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(parameters);
            writer.flush();
            connection.setConnectTimeout(3000);
            connection.connect();
            Reader input = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

            for (int c; (c = input.read()) >= 0; ) {
                result = result.concat(String.valueOf((char) c));
            }

            result = result.replace("[{", "{");
            result = result.replace("}]", "}");
            if (result.contains("{")) {
                JSONObject jsonObj = new JSONObject(result);
                try {
                    if (destination.contains("METADATA"))
                        result = jsonObj.get("access_token").toString();
                    else
                        result = jsonObj.get("token").toString();
                } catch (Exception e) {
                    connection.disconnect();
                    e.printStackTrace();
                }
            }
            code = connection.getResponseCode();
            connection.disconnect();
            if (code == 200) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            if (connection != null)
                connection.disconnect();
            return null;
        }
    }

    public static String getJWTPermissionFromHttpsOrHttpServer(String baseUri, String username, String pass, String destination) {
        HttpURLConnection connection = null;
        String urlIDart = baseUri + "/rpc/login";
        String urlIDMED = baseUri + "/api/login";
        String url = null;
        String parameters = null;

        if (destination.contains("IDMED")) {
            url = urlIDMED;
            parameters = "{\"username\":\"" + username + "\",\"password\":\"" + pass + "\"}";
        }  else if (destination.contains("METADATA")) {
            url = urlIDMED;
            parameters = "{\"username\":\"" + username + "\",\"password\":\"" + pass + "\"}";
        } else {
            url = urlIDart;
            parameters = "{\"username\":\"" + username + "\",\"pass\":\"" + pass + "\"}";
        }


        String result = "";
        JSONObject jsonObj = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            HttpRequest request = HttpRequest
                        .newBuilder()
                        .uri(new URI(url))
                        .header("Accept", "application/json")
                        .header("Content-type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(parameters))
                        .build();

        HttpResponse<String> response = HttpClient
                    .newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                if(response.body().toString().startsWith("[")){
                    jsonObj = new JSONObject(objectMapper.readValue(response.body(), ArrayList.class).get(0).toString());
                } else if (response.body().toString().startsWith("{")) {
                    jsonObj = new JSONObject(response.body().toString());
                }
                if(jsonObj != null){
                    if (destination.contains("METADATA") || destination.contains("IDMED") )
                        result = jsonObj.get("access_token").toString();
                    else
                        result = jsonObj.get("token").toString();
                }

                return result;
            }
        }
        catch (Exception e) {
            System.out.println("Wrong domain - Exception: java.net.ConnectException");
            // throw new RuntimeException(e);
        }
        return result;
    }
}
