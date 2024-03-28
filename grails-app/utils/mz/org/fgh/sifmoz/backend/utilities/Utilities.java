package mz.org.fgh.sifmoz.backend.utilities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import mz.org.fgh.sifmoz.backend.clinic.Clinic;
import org.apache.http.entity.StringEntity;
import org.grails.web.json.JSONArray;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.*;
import java.util.*;

public class Utilities {

    private static Utilities instance;

    private Utilities() {
    }

    public static Utilities getInstance(){
        if (instance == null){
            instance = new Utilities();
        }
        return instance;
    }

    public static boolean stringHasValue(String string){
        return string != null && !string.isEmpty() && string.trim().length() > 0;
    }

    public static String ensureXCaractersOnNumber(long number, int x){
        String formatedNumber = "";
        int numberOfCharacterToIncrise = 0;

        formatedNumber = number + "";

        numberOfCharacterToIncrise = x - formatedNumber.length();

        for(int i = 0; i < numberOfCharacterToIncrise; i++) formatedNumber = "0" + formatedNumber;

        return formatedNumber;
    }

    public static String concatStrings(String currentString, String toConcant, String scapeStr){
        if (!stringHasValue(currentString)) return toConcant;

        if (!stringHasValue(toConcant)) return currentString;

        return currentString + scapeStr+ toConcant;
    }

    public static boolean isStringIn(String value, String... inValues){
        if (inValues == null || value == null) return false;

        for (String str : inValues){
            if (value.equals(str)) return true;
        }

        return false;
    }

    public static boolean listHasElements(ArrayList<?> list){
        return list != null && !list.isEmpty() && list.size() > 0;
    }

    public static <T extends Object> T findOnArray(List<T> list, T toFind){
        for (T o : list) {
            if (o.equals(toFind)) return o;
        }
        return null;
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
    public boolean isValidDate(String dateStr) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
    public static String generateUUID () {
        return UUID.randomUUID().toString();
    }

    public static String generateID (Clinic clinic) {
        return clinic.getCode() +"-"+generateUUID();
    }

    public static String garantirXCaracterOnNumber(long number, int x){
        String formatedNumber = "";
        int numberOfCharacterToIncrise = 0;

        formatedNumber = number + "";

        numberOfCharacterToIncrise = x - formatedNumber.length();

        for(int i = 0; i < numberOfCharacterToIncrise; i++) formatedNumber = "0" + formatedNumber;

        return formatedNumber;
    }

    public static StringEntity parseToJSON(Object object) throws JsonProcessingException {

        Gson g = new Gson();
        String restObject = g.toJson(object);
        StringEntity inputAddPatient = new StringEntity(restObject, "UTF-8");
        inputAddPatient.setContentType("application/json");

        return inputAddPatient;

//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        return objectMapper.writeValueAsString(object);
    }

    public static String formatToYYYYMMDD (Date date ) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String strDate = dateFormat.format(date);

        return strDate;
    }

    public static Date dateformatToYYYYMMDD (String date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date strDate = null;
        try {
            strDate = dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return strDate;
    }

    public static String dateformatToDDMMMYYYY (Date date ) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

        String strDate = null;
        try {
            strDate = dateFormat.format(date);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return strDate;
    }

    public static Date addDaysInDate(Date theDate, int days){
        Date currentDate = theDate;

        // Convert the Date object to a Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Add 5 days to the current date
        calendar.add(Calendar.DAY_OF_MONTH, days);

        // Get the updated Date
        Date newDate = calendar.getTime();

        return newDate;
    }

    public static boolean compareStringIgnoringAccents(String a, String b) {
        final Collator instance = Collator.getInstance();

        instance.setStrength(Collator.NO_DECOMPOSITION);

        if (instance.compare(a, b) == 0) return true;

        return false;
    }

    public static String stripAccents(String s)
    {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    public static String excludeProcessedProp(List<String> strings) {
        String newProps = null;
        for (String s : strings) {
            if (stringHasValue(newProps)) newProps += "."+s;
            newProps = s;
        }

        return newProps;
    }

    public List<String> splitString(String st, String separator) {
        if (!stringHasValue(st)) return null;

        List<String> result = new ArrayList<>();
        String[] exploded = st.split(separator);

        for (int i=0; i < exploded.length - 1; i++) {
            result.add(exploded[i]);
        }
        return result;
    }

    public static String getMd5(String input)
    {
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            // of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
