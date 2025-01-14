package mz.org.fgh.sifmoz.backend.multithread;


import grails.validation.Validateable;
import groovy.lang.Closure;
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Validated
public class ReportSearchParams implements Validateable {
    public static final String PERIOD_TYPE_SPECIFIC = "SPECIFIC";
    public static final String PERIOD_TYPE_MONTH = "MONTH";
    public static final String PERIOD_TYPE_QUARTER = "QUARTER";
    public static final String PERIOD_TYPE_SEMESTER = "SEMESTER";
    public static final String PERIOD_TYPE_ANNUAL = "ANNUAL";


    public static final String PERIOD_QUARTER_1 = "1";
    public static final String PERIOD_QUARTER_2 = "2";
    public static final String PERIOD_QUARTER_3 = "3";
    public static final String PERIOD_QUARTER_4 = "4";
    public static final String PERIOD_SEMESTER_1 = "1";

    public static final String PERIOD_SEMESTER_2 = "2";

    public static final String PERIOD_TYPE_NA = "NOT_APPLICABLE";

    private String id;
    private String clinicId;
    private String provinceId;
    private String districtId;
    private String periodType;
    private String period;
    private String clinicalService;
    private int year;
    private Date startDate;
    private Date endDate;
    private String startDateParam;
    private String endDateParam;
    private String reportType;

    public ReportSearchParams() {
    }

    public ReportSearchParams(String periodType) {
        this.periodType = periodType;
    }

    public static ReportSearchParams generateAnnualPeriod(int year) {
        ReportSearchParams params = new ReportSearchParams(PERIOD_TYPE_ANNUAL);
        params.setYear(year);
        params.determineStartEndDate();
        return params;
    }

    public void determineStartEndDate() {
        Date currentDate = new Date();

        switch (getPeriodType()) {
            case PERIOD_TYPE_SPECIFIC:
                handleSpecificPeriod();
                break;
            case PERIOD_TYPE_MONTH:
                handleMonthPeriod(currentDate);
                break;
            case PERIOD_TYPE_QUARTER:
                handleQuarterPeriod(currentDate);
                break;
            case PERIOD_TYPE_SEMESTER:
                handleSemesterPeriod(currentDate);
                break;
            case PERIOD_TYPE_ANNUAL:
                handleAnnualPeriod(currentDate);
                break;
            case PERIOD_TYPE_NA:
                break;
        }
    }

    private void handleSpecificPeriod() {
        this.startDate = ConvertDateUtils.getDateAtStartOfDay(ConvertDateUtils.createDate(getStartDateParam(), "dd-MM-yyyy"));
        this.endDate = ConvertDateUtils.createDate(getEndDateParam(), "dd-MM-yyyy");
    }

    private void handleMonthPeriod(Date currentDate) {
        int month = Integer.parseInt(getPeriod());
        Date startDateTemp = DateUtils.addMonths(ConvertDateUtils.getDateFromDayAndMonthAndYear(21, month, getYear()), -1);
        this.startDate = ConvertDateUtils.getDateAtStartOfDay(startDateTemp);
        this.endDate = ConvertDateUtils.getDateFromDayAndMonthAndYear(20, month, getYear());
        adjustEndDateIfAfterCurrentDate(currentDate);
    }

    private void handleQuarterPeriod(Date currentDate) {
        int startMonth, endMonth;
        switch (getPeriod()) {
            case PERIOD_QUARTER_1:
                startMonth = 12;
                endMonth = 3;
                break;
            case PERIOD_QUARTER_2:
                startMonth = 3;
                endMonth = 6;
                break;
            case PERIOD_QUARTER_3:
                startMonth = 6;
                endMonth = 9;
                break;
            default:
                startMonth = 9;
                endMonth = 12;
                break;
        }
        this.startDate = ConvertDateUtils.getDateAtStartOfDay(ConvertDateUtils.getDateFromDayAndMonthAndYear(21, startMonth, (startMonth == 12 ? getYear() - 1 : getYear())));
        this.endDate = ConvertDateUtils.getDateFromDayAndMonthAndYear(20, endMonth, getYear());
        adjustEndDateIfAfterCurrentDate(currentDate);
    }

    private void handleSemesterPeriod(Date currentDate) {
        int startMonth, endMonth;
        switch (getPeriod()) {
            case PERIOD_SEMESTER_1:
                startMonth = 12;
                endMonth = 6;
                break;
            default:
                startMonth = 6;
                endMonth = 12;
                break;
        }
        this.startDate = ConvertDateUtils.getDateAtStartOfDay(ConvertDateUtils.getDateFromDayAndMonthAndYear(21, startMonth, (startMonth == 12 ? getYear() - 1 : getYear())));
        this.endDate = ConvertDateUtils.getDateFromDayAndMonthAndYear(20, endMonth, getYear());
        adjustEndDateIfAfterCurrentDate(currentDate);
    }

    private void handleAnnualPeriod(Date currentDate) {
        this.startDate = ConvertDateUtils.getDateAtStartOfDay(ConvertDateUtils.getDateFromDayAndMonthAndYear(21, 12, getYear() - 1));
        this.endDate = ConvertDateUtils.getDateFromDayAndMonthAndYear(20, 12, getYear());
        adjustEndDateIfAfterCurrentDate(currentDate);
    }

    private void adjustEndDateIfAfterCurrentDate(Date currentDate) {
        if(getReportType() != null)
        if (!getReportType().equalsIgnoreCase("EXPECTED_PATIENTS") && endDate.after(currentDate)) {
            setEndDate(currentDate);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClinicalService() {
        return clinicalService;
    }

    public void setClinicalService(String clinicalService) {
        this.clinicalService = clinicalService;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getClinicId() {
        return clinicId;
    }

    public String getStartDateParam() {
        return startDateParam;
    }

    public void setStartDateParam(String startDateParam) {
        this.startDateParam = startDateParam;
    }

    public String getEndDateParam() {
        return endDateParam;
    }

    public void setEndDateParam(String endDateParam) {
        this.endDateParam = endDateParam;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    @Override
    public Errors getErrors() {
        return null;
    }

    @Override
    public void setErrors(Errors errors) {

    }

    @Override
    public Boolean hasErrors() {
        return null;
    }

    @Override
    public void clearErrors() {

    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public boolean validate(Closure<?>... adHocConstraintsClosures) {
        return false;
    }

    @Override
    public boolean validate(Map<String, Object> params) {
        return false;
    }

    @Override
    public boolean validate(Map<String, Object> params, Closure<?>... adHocConstraintsClosures) {
        return false;
    }

    @Override
    public boolean validate(List fieldsToValidate) {
        return false;
    }

    @Override
    public boolean validate(List fieldsToValidate, Closure<?>... adHocConstraintsClosures) {
        return false;
    }

    @Override
    public boolean validate(List fieldsToValidate, Map<String, Object> params) {
        return false;
    }

    @Override
    public boolean validate(List fieldsToValidate, Map<String, Object> params, Closure<?>... adHocConstraintsClosures) {
        return false;
    }
}