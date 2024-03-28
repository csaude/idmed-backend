package mz.org.fgh.sifmoz.backend.restUtils


import mz.org.fgh.sifmoz.backend.interoperabilityAttribute.InteroperabilityAttribute
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.grails.web.json.JSONObject

import java.nio.charset.StandardCharsets

class RestProvincialServerClient {

    RestProvincialServerClient() {

    }

    static def requestProvincialServerClient(String object, ProvincialServer provincialServer,String method) {
        String restUrl =provincialServer.getUrlPath()
        String result = ""
        int code = 200
        try {
            String userCredentials = new StringBuffer(provincialServer.username).append(":").append(provincialServer.password).toString()
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()))
            println(restUrl)
            println(basicAuth)
            URL siteURL = new URL(restUrl)
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection()
            connection.setRequestProperty("Authorization", basicAuth)
            connection.setRequestMethod(method)
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setDoInput(true)
            connection.setDoOutput(true);
//            connection.setConnectTimeout(3000)
            // Send post request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream())
            wr.writeBytes(object)
            wr.flush()
            wr.close()

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
