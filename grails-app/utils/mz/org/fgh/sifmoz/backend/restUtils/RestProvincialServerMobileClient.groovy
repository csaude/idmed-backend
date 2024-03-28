package mz.org.fgh.sifmoz.backend.restUtils


import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RestProvincialServerMobileClient {

    RestProvincialServerMobileClient() {

    }

    static def postRequestProvincialServerClient(ProvincialServer provincialServer, String urlPath, StringEntity object) {
        String restUrlBase = provincialServer.getUrlPath() + provincialServer.getPort()
        String restUrl = provincialServer.getUrlPath().contains("https") ? restUrlBase.replaceAll(':'+provincialServer.getPort(),'') + urlPath : provincialServer.getUrlPath() + provincialServer.getPort() + urlPath
        String result = ""
        restUrlBase = provincialServer.getUrlPath().contains("https") ? restUrlBase.replaceAll(':'+provincialServer.getPort(),'') : restUrlBase
        try {
            //println PostgrestAuthenticationUtils.getJWTPermission(restUrlBase, provincialServer.getUsername(), provincialServer.getPassword(), provincialServer.destination)
            CloseableHttpClient client = HttpClients.createDefault();

            HttpPost request = new HttpPost(restUrl);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");
            request.setHeader("Authorization", "Bearer " + PostgrestAuthenticationUtils.getJWTPermissionFromHttpsOrHttpServer(restUrlBase, provincialServer.getUsername(), provincialServer.getPassword(), provincialServer.destination))
            request.setEntity(object)
            CloseableHttpResponse response = client.execute(request)

            result = response.getStatusLine().getStatusCode()
        } catch (Exception e) {
            result = "-> Red <-\t" + "Wrong domain - Exception: " + e.getMessage();
        }
        println(result)
        return result
    }

    static def patchRequestProvincialServerClient(ProvincialServer provincialServer, String urlPath, StringEntity object) {
        String restUrlBase = provincialServer.getUrlPath() + provincialServer.getPort()
        String restUrl = provincialServer.getUrlPath().contains("https") ? restUrlBase.replaceAll(':'+provincialServer.getPort(),'') + urlPath : provincialServer.getUrlPath() + provincialServer.getPort() + urlPath
        String result = ""
        restUrlBase = provincialServer.getUrlPath().contains("https") ? restUrlBase.replaceAll(':'+provincialServer.getPort(),'') : restUrlBase
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPatch request = new HttpPatch(restUrl);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");
            request.setHeader("Authorization", "Bearer " + PostgrestAuthenticationUtils.getJWTPermissionFromHttpsOrHttpServer(restUrlBase, provincialServer.getUsername(), provincialServer.getPassword(), provincialServer.destination))
            request.setEntity(object)
            CloseableHttpResponse response = client.execute(request)


            result = response.getStatusLine().getStatusCode()
        } catch (Exception e) {
            result = "-> Red <-\t" + "Wrong domain - Exception: " + e.getMessage();
        }
        println(result)
        return result
    }

    static def getRequestProvincialServerClient(ProvincialServer provincialServer, String urlPath) {
        String restUrlBase = provincialServer.getUrlPath() + provincialServer.getPort()
        String restUrl = provincialServer.getUrlPath().contains("https") ? restUrlBase.replaceAll(':'+provincialServer.getPort(),'') + urlPath : provincialServer.getUrlPath() + provincialServer.getPort() + urlPath
        String result = ""
        restUrlBase = provincialServer.getUrlPath().contains("https") ? restUrlBase.replaceAll(':'+provincialServer.getPort(),'') : restUrlBase
        try {
            String token =  PostgrestAuthenticationUtils.getJWTPermissionFromHttpsOrHttpServer(restUrlBase, provincialServer.getUsername(), provincialServer.getPassword(), provincialServer.destination)

            HttpRequest request
            if (provincialServer.destination.contains("METADATA")) {
                 request = HttpRequest
                        .newBuilder()
                        .uri(new URI(restUrl))
                        .header("Accept", "application/json")
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Auth-Token", token)
                        .GET()
                        .build()
            } else {
                request = HttpRequest
                        .newBuilder()
                        .uri(new URI(restUrl))
                        .header("Accept", "application/json")
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build()
            }

            HttpResponse<String> response = HttpClient
                    .newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                 result = response.body()
                if(response.body().toString().startsWith("["))
                return new JSONArray(result)
            }
        }
        catch (Exception e) {
            println("Wrong domain - Exception: java.net.ConnectException -"+ restUrl )
            // e.printStackTrace();
        }
        return result
    }

    static def requestGetDataOnProvincialServerClient(ProvincialServer provincialServer, String urlPath) {
        String restUrlBase = provincialServer.getUrlPath() + provincialServer.getPort()
        String restUrl = provincialServer.getUrlPath().contains("https") ? restUrlBase.replaceAll(':'+provincialServer.getPort(),'') + urlPath : provincialServer.getUrlPath() + provincialServer.getPort() + urlPath
        String result = ""
        restUrlBase = provincialServer.getUrlPath().contains("https") ? restUrlBase.replaceAll(':'+provincialServer.getPort(),'') : restUrlBase
        int code = 200
        try {
            println(restUrl)
            URL siteURL = new URL(restUrl)
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection()
            connection.setRequestProperty("Authorization", "Bearer " + PostgrestAuthenticationUtils.getJWTPermissionFromHttpsOrHttpServer(restUrlBase, provincialServer.getUsername(), provincialServer.getPassword(), provincialServer.destination))
            connection.setRequestMethod("GET")
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setDoInput(true)
            connection.setDoOutput(true)
//            connection.setConnectTimeout(3000)
            // Send post request
            connection.connect()
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) { // success
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))
                String inputLine
                ///  List response = new ArrayList<>()
                StringBuffer response = new StringBuffer()
                while ((inputLine = input.readLine()) != null) {
                    response.append(inputLine)
                }
                input.close()

                // print result
                String responseStr = response.toString();
                println(new JSONArray(responseStr))

                return new JSONArray(responseStr)
            } else {
                println("GET request not worked")
                return new JSONObject("{\"sessionId\":null,\"authenticated\":null}")
            }

//            connection.connect()
            code = connection.getResponseCode()
            connection.disconnect()
            if (code == 201) {
                result = "-> Green <-\t" + "Code: " + code;
            } else {
                result = "-> Yellow <-\t" + "Code: " + code;
            }
        } catch (Exception e) {
            result = "-> Red <-\t" + "Wrong domain - Exception: " + e.getMessage();
        }
        println(result)
        return result
    }

}
