package mz.org.fgh.sifmoz.backend.restUtils

import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.interoperabilityAttribute.InteroperabilityAttribute
import mz.org.fgh.sifmoz.backend.migration.params.stock.StockTakeMigrationSearchParams
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen
import org.apache.http.entity.StringEntity
import org.apache.logging.log4j.LogManager
import org.grails.web.json.JSONObject
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

class RestOpenMRSClient {

    final static org.apache.logging.log4j.Logger logger = LogManager.getLogger(RestOpenMRSClient.class)

    RestOpenMRSClient() {

    }

    String createOpenMRSDispense(Pack pack, Patient patient) {

        String inputAddPerson = "{}"
        String customizedDosage = ""
        String obsGroupsJson = null
        String dispenseMod = ""
        int packSize = 0

        Patient.withNewSession {
            try {
                List<String> obsGroups = new ArrayList<>()
                List<InteroperabilityAttribute> interoperabilityAttributes = Patient.get(patient.id).his.interoperabilityAttributes as List<InteroperabilityAttribute>
                PatientVisitDetails pvd = PatientVisitDetails.findByPack(pack)
                PrescriptionDetail prescriptionDetail = PrescriptionDetail.findByPrescription(Prescription.findById(pvd.prescription.id))

                TherapeuticRegimen therapeuticRegimen = null

                if(prescriptionDetail.therapeuticRegimen){
                    therapeuticRegimen = TherapeuticRegimen.findById(prescriptionDetail.therapeuticRegimen.id)
                    if(therapeuticRegimen.isTARV() || therapeuticRegimen.isPPE()){
                        inputAddPerson =  setOpenMRSFILA(interoperabilityAttributes, pack, patient, customizedDosage, obsGroupsJson, dispenseMod, packSize, obsGroups)
                    }
                    if(therapeuticRegimen.isTPT())
                        inputAddPerson =  setOpenMRSFILT(interoperabilityAttributes, pack, patient)
                }else{
                    logger.error("Paciente "+ patient.firstNames +" "+ patient.lastNames+" com prescricao sem Regime Terapeutico")
                    PrescriptionDetail.withNewTransaction {
                        List<Drug> drugs = new ArrayList<Drug>()
                        Drug drug = Drug.findById(pack.packagedDrugs.first().drug.id)
                        therapeuticRegimen = drug.therapeuticRegimenList.first()
                        prescriptionDetail.setTherapeuticRegimen(therapeuticRegimen)
                        prescriptionDetail.save(flush: true, failOnError: true)
                    }
                }
            } catch (Exception e) {
                e.printStackTrace()
            }

            return inputAddPerson
        }
    }

    static def requestOpenMRSClient(String base64code, String object, String urlBase, String urlPath, String method) {
        String restUrl = urlBase.concat(urlPath)
        String result = ""
        String messageResponse = ""
        int code = 200
        try {
            String userCredentials = base64code
            String basicAuth = "Basic " + base64code
            println(restUrl)
            println(basicAuth)
            URL siteURL = new URL(restUrl)
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection()
            connection.setRequestProperty("Authorization", basicAuth)
            connection.setRequestMethod(method)
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setDoInput(true)
            connection.setConnectTimeout(10000)
            connection.setDoOutput(true)
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream())
            wr.writeBytes(object)
            wr.flush()
            wr.close()
            messageResponse = connection.getResponseMessage()
            code = connection.getResponseCode()
            connection.disconnect()
            if (code == 201) {
                result = "-> Green <-\t" + "Code: " + code +"\n"+ messageResponse
            } else {
                result = "-> Yellow <-\t" + "Code: " + code +"\n"+ messageResponse
            }
        } catch (Exception e) {
            result = "-> Red <-\t" + "Wrong domain - Exception: " + e.getMessage();
        }
        return result
    }

    static def getResponseOpenMRSClient(String openmrsBase64, String object, String urlBase, String urlPath, String method) {

        String restUrl = urlBase.concat(urlPath)
        int code = 200
        try {
            String userCredentials = openmrsBase64
            String basicAuth = "Basic " + openmrsBase64
            println(restUrl)
            println(basicAuth)
            URL siteURL = new URL(restUrl)
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection()
            connection.setRequestProperty("Authorization", basicAuth)
            connection.setRequestMethod(method)
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setDoInput(true)
            connection.setDoOutput(true)
            connection.setConnectTimeout(10000)

            code = connection.getResponseCode()

            if (code==HttpURLConnection.HTTP_OK ||  code==HttpURLConnection.HTTP_CREATED ) { // success
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))
                String inputLine
                StringBuffer response = new StringBuffer()
                while ((inputLine = input.readLine()) != null) {
                    response.append(inputLine)
                }
                input.close()
                return new JSONObject(response.toString())
            } else {
                println("GET request not worked")
                return null
            }
            connection.disconnect()
        } catch (Exception e) {
            e.printStackTrace()
        }
        println("Connection Refused")
        return new JSONObject("{\"sessionId\":null,\"authenticated\":null}")
    }

    static def getPatientResponseOpenMRSClient(String openmrsBase64, String object, String urlBase, String urlPath, String method) {

        String restUrl = urlBase.concat(urlPath)
        int code = 200
        try {
            String userCredentials = openmrsBase64
            String basicAuth = "Basic " + openmrsBase64
            println(restUrl)
            println(basicAuth)
            URL siteURL = new URL(restUrl)
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection()
            connection.setRequestProperty("Authorization", basicAuth)
            connection.setRequestMethod(method)
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setDoInput(true)
            connection.setDoOutput(true)
            connection.setConnectTimeout(10000)
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream())
            wr.writeBytes(object)
            wr.flush()
            wr.close()
            code = connection.getResponseCode()

            if (code==HttpURLConnection.HTTP_OK ||  code==HttpURLConnection.HTTP_CREATED ) { // success
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))
                String inputLine
                StringBuffer response = new StringBuffer()
                while ((inputLine = input.readLine()) != null) {
                    response.append(inputLine)
                }
                input.close()
                return new JSONObject(response.toString())
            } else {
                println("GET request not worked")
                return null
            }
            connection.disconnect()
        } catch (Exception e) {
            e.printStackTrace()
        }
        println("Connection Refused")
        return new JSONObject("{\"sessionId\":null,\"authenticated\":null}")
    }


    String setOpenMRSFILA(List<InteroperabilityAttribute> interoperabilityAttributes, Pack pack, Patient patient,
                          String customizedDosage, String obsGroupsJson, String dispenseMod, int packSize, List<String> obsGroups){

        String filaUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "FORM_FILA_UUID" }.value
        String dispenseModeUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "DISPENSE_MODE_CONCEPT_UUID" }.value
        String encounterType = interoperabilityAttributes.find { it.interoperabilityType.code == "FILA_ENCOUNTER_TYPE_CONCEPT_UUID" }.value
        String providerUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "UNIVERSAL_PROVIDER_UUID" }.value
        String regimeUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "FILA_REGIMEN_CONCEPT_UUID" }.value
        String dispensedAmountUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "DISPENSED_AMOUNT_CONCEPT" }.value
        String dosageUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "DOSAGE_CONCEPT_UUID" }.value
        String returnVisitUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "FILA_NEXT_VISIT_CONCEPT_UUID" }.value
        PatientVisitDetails pdv = PatientVisitDetails.findByPack(pack)
        String strRegimenAnswerUuid = PrescriptionDetail.findByPrescription(Prescription.findById(pdv.prescription.id)).therapeuticRegimen.openmrsUuid

        obsGroupsJson = ""

        for (PackagedDrug pd : pack.packagedDrugs) {
            //posologia
            customizedDosage = "Tomar " + String.valueOf(pd.timesPerDay) +" "+pd.drug.form.description+" "+String.valueOf(pd.amtPerTime).replace(".0","")+" vez(es) por "+pd.drug.defaultPeriodTreatment

            String formulationString = "{\"" +
                    "person\":\"" + patient.hisUuid + "\"," +
                    "\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\"," +
                    "\"concept\":\"7956cd89-2ef6-4d25-90f9-f8940507eee8\"," +
                    "\"value\":\"" + pd.drug.uuidOpenmrs + "\"," +
                    "\"comment\":\"IDMED\"" +
                    "}"

            String quantityString = "{\"" +
                    "person\":\"" + patient.hisUuid + "\"," +
                    "\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\"," +
                    "\"concept\":\"e1de2ca0-1d5f-11e0-b929-000c29ad1d07\"," +
                    "\"value\":\"" + (pd.quantitySupplied.intValue() * pd.drug.packSize) + "\"," +
                    "\"comment\":\"IDMED\"" +
                    "}"

            String dosageString = "{\"" +
                    "person\":\"" + patient.hisUuid + "\"," +
                    "\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\"," +
                    "\"concept\":\"e1de28ae-1d5f-11e0-b929-000c29ad1d07\"," +
                    "\"value\":\"outraPosologia\"," +
                    "\"comment\":\"IDMED\"" +
                    "}"

            String posologyString = "{\"" +
                    "person\":\"" + patient.hisUuid + "\"," +
                    "\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\"," +
                    "\"concept\":\"a46a603e-788d-4edc-9465-5f2fa69f060e\"," +
                    "\"value\":\""+customizedDosage+"\"," +
                    "\"comment\":\"IDMED\"" +
                    "}"

            String obsGroup = "{\"" +
                    "person\":\"" + patient.hisUuid + "\"," +
                    "\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\"," +
                    "\"concept\":\"5ad593a4-bea2-4eef-ac88-11654e79d9da\"," +
                    "\"comment\":\"IDMED\"," +
                    "\"groupMembers\": [" + formulationString + "," + quantityString + "," + dosageString + "," + posologyString + "]" +
                    "}"

            obsGroups.add(obsGroup)

            //Dispensed amount
            packSize = packSize + pd.getQuantitySupplied().intValue()
        }

        for (String group : obsGroups) {
            if (!Utilities.stringHasValue(obsGroupsJson))
                obsGroupsJson = group
            else {
                obsGroupsJson = obsGroupsJson + "," + group
            }
        }

        if (pack.dispenseMode.openmrsUuid) {
            dispenseMod = "{\"person\":\""
                    .concat(patient.hisUuid + "\",")
                    .concat("\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate))
                    .concat("\",\"concept\":\"" + dispenseModeUuid + "\",\"value\":\"")
                    .concat(pack.dispenseMode.openmrsUuid + "\",\"comment\":\"IDMED\"},")
        }

        String buildDispenseMap = "{\"encounterDatetime\": \"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\", \"patient\": \"" + patient.hisUuid + "\", \"encounterType\": \"" + encounterType + "\", "
                .concat("\"location\":\"" + patient.hisLocation + "\", \"form\":\"" + filaUuid + "\", \"encounterProviders\":[{\"provider\":\"" + providerUuid + "\", \"encounterRole\":\"a0b03050-c99b-11e0-9572-0800200c9a66\"}], ")
                .concat("\"obs\":[")
                .concat("{\"person\":\"" + patient.hisUuid + "\",\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\",\"concept\":\"" + regimeUuid + "\",\"value\":\"" + strRegimenAnswerUuid + "\", \"comment\":\"IDMED\"},")
                .concat("{\"person\":\"" + patient.hisUuid + "\",\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\",\"concept\":\"" + dispensedAmountUuid + "\",\"value\":\"" + packSize + "\",\"comment\":\"IDMED\"},")
                .concat("{\"person\":\"" + patient.hisUuid + "\",")
                .concat("\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\",\"concept\":\"" + dosageUuid + "\",\"value\":\"" + customizedDosage + "\",\"comment\":\"IDMED\"},")
                .concat("{\"person\":\"" + patient.hisUuid + "\",")
                .concat("\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\",\"concept\":\"" + returnVisitUuid + "\",\"value\":\"" + Utilities.formatToYYYYMMDD(pack.nextPickUpDate) + "\",\"comment\":\"IDMED\"},")
                .concat(dispenseMod)
                .concat(obsGroupsJson)
                .concat("]")
                .concat("}")

        return  new String(buildDispenseMap.getBytes(), StandardCharsets.UTF_8)
    }


    String setOpenMRSFILT(List<InteroperabilityAttribute> interoperabilityAttributes, Pack pack, Patient patient) {
        String filtNextApointmentUuid = "b7c246bc-f2b6-49e5-9325-911cdca7a8b3"
        String filtUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "FORM_FILT_UUID" }.value
        String dispenseModeUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "DISPENSE_MODE_CONCEPT_UUID" }.value
        String encounterType = interoperabilityAttributes.find { it.interoperabilityType.code == "FILT_ENCOUNTER_TYPE_CONCEPT_UUID" }.value
        String providerUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "UNIVERSAL_PROVIDER_UUID" }.value
        String regimeUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "FILT_REGIMEN_CONCEPT_UUID" }.value
        String returnVisitUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "FILT_TPT_PATIENT_TYPE_UUID" }.value
        String tipoDispensaUuid = interoperabilityAttributes.find { it.interoperabilityType.code == "FILT_DISPENSED_TYPE_CONCEPT_UUID" }.value
        PatientVisitDetails pdv = PatientVisitDetails.findByPack(pack)
        String strRegimenAnswerUuid = PrescriptionDetail.findByPrescription(Prescription.findById(pdv.prescription.id)).therapeuticRegimen.openmrsUuid
        String strCodeDispenseType = PrescriptionDetail.findByPrescription(Prescription.findById(pdv.prescription.id)).dispenseType.code
        PatientVisitDetails patientVisitDetails = PatientVisitDetails.findByPack(pack)
        String strDispenseType = interoperabilityAttributes.find { it.interoperabilityType.code == "MONTHLY_DISPENSED_TYPE_CONCEPT_UUID" }.value

        boolean packContinue = false
        String nextFollowUp = ""
        String dispenseMod = ""

        if (pack?.packagedDrugs) {
            def firstPackagedDrug = pack?.packagedDrugs?.first()
            if (firstPackagedDrug) {
                packContinue = firstPackagedDrug.getToContinue()
            }
        }

        String nextVisitDate = "{\"person\":\""
                .concat(patient.hisUuid + "\",")
                .concat("\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate))
                .concat("\",\"concept\":\"" + filtNextApointmentUuid)
                .concat("\",\"value\":\"" + Utilities.formatToYYYYMMDD(pack.nextPickUpDate))
                .concat("\",\"comment\":\"IDMED\"},")

        if(!strCodeDispenseType.equalsIgnoreCase("DM")){
            strDispenseType = interoperabilityAttributes.find { it.interoperabilityType.code == "QUARTERLY_DISPENSED_TYPE_CONCEPT_UUID" }.value
        }

        if(patientVisitDetails.episode.getStartStopReason().isNew()){
            nextFollowUp =  interoperabilityAttributes.find { it.interoperabilityType.code == "PATIENT_TYPE_INITIAL_UUID" }.value
        }else{
            if(packContinue){
                nextFollowUp =  interoperabilityAttributes.find { it.interoperabilityType.code == "PATIENT_TYPE_CONTINUE_UUID" }.value
            }else {
                nextVisitDate = ""
                nextFollowUp =  interoperabilityAttributes.find { it.interoperabilityType.code == "PATIENT_TYPE_END_UUID" }.value
            }
        }

        if(patientVisitDetails.episode.getStartStopReason().code.startsWith("REIN")){
            nextFollowUp =  interoperabilityAttributes.find { it.interoperabilityType.code == "PATIENT_TYPE_RESTART_UUID" }.value
        }


        if (pack.dispenseMode.openmrsUuid) {
            dispenseMod = "{\"person\":\""
                    .concat(patient.hisUuid + "\",")
                    .concat("\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate))
                    .concat("\",\"concept\":\"" + dispenseModeUuid + "\",\"value\":\"")
                    .concat(pack.dispenseMode.openmrsUuid + "\",\"comment\":\"IDMED\"}")
        }

        String buildDispenseMap = "{\"encounterDatetime\": \"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\", \"patient\": \"" + patient.hisUuid + "\", \"encounterType\": \"" + encounterType + "\", "
                .concat("\"location\":\"" + patient.hisLocation + "\", \"form\":\"" + filtUuid + "\", \"encounterProviders\":[{\"provider\":\"" + providerUuid + "\", \"encounterRole\":\"a0b03050-c99b-11e0-9572-0800200c9a66\"}], ")
                .concat("\"obs\":[")
                .concat("{\"person\":\"" + patient.hisUuid + "\",\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\",\"concept\":\"" + regimeUuid + "\",\"value\":\"" + strRegimenAnswerUuid + "\", \"comment\":\"IDMED\"},")
                .concat("{\"person\":\"" + patient.hisUuid + "\",\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\",\"concept\":\"" + tipoDispensaUuid + "\",\"value\":\"" + strDispenseType + "\",\"comment\":\"IDMED\"},")
                .concat("{\"person\":\"" + patient.hisUuid + "\",\"obsDatetime\":\"" + Utilities.formatToYYYYMMDD(pack.pickupDate) + "\",\"concept\":\"" + returnVisitUuid + "\",\"value\":\"" + nextFollowUp + "\",\"comment\":\"IDMED\"},")
                .concat(nextVisitDate)
                .concat(dispenseMod)
                .concat("]")
                .concat("}")

        return  new String(buildDispenseMap.getBytes(), StandardCharsets.UTF_8)

    }




    String createOpenMRSPatient( Patient patient, PatientServiceIdentifier patientServiceIdentifier, String identifierTypeIdOpenMrs) {



        Patient.withNewSession {
            try {
                Timestamp timestamp = patient.dateOfBirth
                LocalDateTime localDateTime = timestamp.toLocalDateTime();
                String dateOfBirthFormated = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                String openmrsJSON  =
                         "{\"person\":"
                                .concat("{")
                                .concat( "\"gender\": \"" + patient?.gender?.toString()?.charAt(0)?.toUpperCase() + "\",")
                                .concat( "\"names\":")
                                .concat("[{\"givenName\": \"" + patient.firstNames + "\", \"middleName\": \"" + patient.middleNames + "\", \"familyName\": \"" + patient.lastNames + "\"}], \"birthdate\": \"" + dateOfBirthFormated+ "\"")
                                .concat( "},")
                                .concat( "\"identifiers\":")
                                .concat("[")
                                .concat("{")
                                .concat("\"identifier\": \"" + patientServiceIdentifier.value + "\", \"identifierType\":  \"" + identifierTypeIdOpenMrs+ "\",")
                                .concat( "\"location\": \"" + patient.hisLocation+ "\", \"preferred\": \"true\"")
                                .concat("}")
                                .concat("]")
                                .concat( "}");

             return   new String(openmrsJSON.getBytes(), StandardCharsets.UTF_8)


            } catch (Exception e) {
                e.printStackTrace()
            }

        }
    }


    }
