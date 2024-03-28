package mz.org.fgh.sifmoz.backend.tansreference;

import mz.org.fgh.sifmoz.backend.clinic.Clinic;
import mz.org.fgh.sifmoz.backend.dispenseMode.DispenseMode;
import mz.org.fgh.sifmoz.backend.dispenseType.DispenseType;
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District;
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Localidade;
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.PostoAdministrativo;
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province;
import mz.org.fgh.sifmoz.backend.drug.Drug;
import mz.org.fgh.sifmoz.backend.duration.Duration;
import mz.org.fgh.sifmoz.backend.group.GroupPack;
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem;
import mz.org.fgh.sifmoz.backend.identifierType.IdentifierType;
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug;
import mz.org.fgh.sifmoz.backend.packaging.Pack;
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier;
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit;
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails;
import mz.org.fgh.sifmoz.backend.prescription.Prescription;
import mz.org.fgh.sifmoz.backend.prescriptionDrug.PrescribedDrug;
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason;
import mz.org.fgh.sifmoz.backend.therapeuticLine.TherapeuticLine;
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen;

import java.util.Date;
import java.util.List;

public class TransReferenceData {
  Long id;
  String firstNames;
  String middleNames;
  String lastNames;
  String gender;
  Date dateOfBirth;
  String cellphone;
  String alternativeCellphone;
  String address;
  String addressReference;
  boolean accountstatus;
  String hisUuid;
  String hisLocation;
  String hisLocationName;
  HealthInformationSystem his;
  String provinceCode;
  String bairroCode;
  String districtCode;
  String postoAdministrativoCode;
    String originClinicUuid;
    String destinationClinicUuid;
    String clinicalServiceCode;
    String patientNid;
    Date startDate;
    Date endDate;
    Date reopenDate;
    String state;
    boolean prefered;
    String identifierTypeCode;
    Date patientVisitDate;
    Date episodeDate;
    String startStopReasonCode;
    String episodeNotes;
    String episodeTypeCode;
    String clinicSectorCode;
    String clinicSectorTypeCode;
    Date prescriptionDate;
    Date expiryDate;
    boolean current;
    String notes;
    String prescriptionSeq;
    String patientType;
    String patientStatus;
     int weeksDuration;
    String reasonForUpdate;
    String therapeuticLineCode;
    String therapeuticRegimenCode;
    String dispenseTypeCode;
    Date dateLeft;
    Date dateReceived;
    boolean modified;
    Date packDate;
    Date pickupDate;
    Date nextPickUpDate;
    int weeksSupply;
  Date dateReturned;
  int stockReturned;
  int packageReturned;
  String reasonForPackageReturn;
  String dispenseModeCode;


  String jsonPrescribedDrug;
  String jsonPackagedDrug;
   //Falta prescribedDrug organizar json

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

  public String getFirstNames() {
    return firstNames;
  }

  public void setFirstNames(String firstNames) {
    this.firstNames = firstNames;
  }

  public String getMiddleNames() {
    return middleNames;
  }

  public void setMiddleNames(String middleNames) {
    this.middleNames = middleNames;
  }

  public String getLastNames() {
    return lastNames;
  }

  public void setLastNames(String lastNames) {
    this.lastNames = lastNames;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getCellphone() {
    return cellphone;
  }

  public void setCellphone(String cellphone) {
    this.cellphone = cellphone;
  }

  public String getAlternativeCellphone() {
    return alternativeCellphone;
  }

  public void setAlternativeCellphone(String alternativeCellphone) {
    this.alternativeCellphone = alternativeCellphone;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAddressReference() {
    return addressReference;
  }

  public void setAddressReference(String addressReference) {
    this.addressReference = addressReference;
  }

  public boolean isAccountstatus() {
    return accountstatus;
  }

  public void setAccountstatus(boolean accountstatus) {
    this.accountstatus = accountstatus;
  }

  public String getHisUuid() {
    return hisUuid;
  }

  public void setHisUuid(String hisUuid) {
    this.hisUuid = hisUuid;
  }

  public String getHisLocation() {
    return hisLocation;
  }

  public void setHisLocation(String hisLocation) {
    this.hisLocation = hisLocation;
  }

  public String getHisLocationName() {
    return hisLocationName;
  }

  public void setHisLocationName(String hisLocationName) {
    this.hisLocationName = hisLocationName;
  }

  public HealthInformationSystem getHis() {
    return his;
  }

  public void setHis(HealthInformationSystem his) {
    this.his = his;
  }

  public String getProvinceCode() {
    return provinceCode;
  }

  public void setProvinceCode(String provinceCode) {
    this.provinceCode = provinceCode;
  }

  public String getBairroCode() {
    return bairroCode;
  }

  public void setBairroCode(String bairroCode) {
    this.bairroCode = bairroCode;
  }

  public String getDistrictCode() {
    return districtCode;
  }

  public void setDistrictCode(String districtCode) {
    this.districtCode = districtCode;
  }

  public String getPostoAdministrativoCode() {
    return postoAdministrativoCode;
  }

  public void setPostoAdministrativoCode(String postoAdministrativoCode) {
    this.postoAdministrativoCode = postoAdministrativoCode;
  }

  public String getOriginClinicUuid() {
    return originClinicUuid;
  }

  public void setOriginClinicUuid(String originClinicUuid) {
    this.originClinicUuid = originClinicUuid;
  }

  public String getDestinationClinicUuid() {
    return destinationClinicUuid;
  }

  public void setDestinationClinicUuid(String destinationClinicUuid) {
    this.destinationClinicUuid = destinationClinicUuid;
  }

  public String getClinicalServiceCode() {
    return clinicalServiceCode;
  }

  public void setClinicalServiceCode(String clinicalServiceCode) {
    this.clinicalServiceCode = clinicalServiceCode;
  }

  public String getPatientNid() {
    return patientNid;
  }

  public void setPatientNid(String patientNid) {
    this.patientNid = patientNid;
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

  public Date getReopenDate() {
    return reopenDate;
  }

  public void setReopenDate(Date reopenDate) {
    this.reopenDate = reopenDate;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public boolean isPrefered() {
    return prefered;
  }

  public void setPrefered(boolean prefered) {
    this.prefered = prefered;
  }

  public String getIdentifierTypeCode() {
    return identifierTypeCode;
  }

  public void setIdentifierTypeCode(String identifierTypeCode) {
    this.identifierTypeCode = identifierTypeCode;
  }

  public Date getPatientVisitDate() {
    return patientVisitDate;
  }

  public void setPatientVisitDate(Date patientVisitDate) {
    this.patientVisitDate = patientVisitDate;
  }

  public Date getEpisodeDate() {
    return episodeDate;
  }

  public void setEpisodeDate(Date episodeDate) {
    this.episodeDate = episodeDate;
  }

  public String getStartStopReasonCode() {
    return startStopReasonCode;
  }

  public void setStartStopReasonCode(String startStopReasonCode) {
    this.startStopReasonCode = startStopReasonCode;
  }

  public String getEpisodeNotes() {
    return episodeNotes;
  }

  public void setEpisodeNotes(String episodeNotes) {
    this.episodeNotes = episodeNotes;
  }

  public String getEpisodeTypeCode() {
    return episodeTypeCode;
  }

  public void setEpisodeTypeCode(String episodeTypeCode) {
    this.episodeTypeCode = episodeTypeCode;
  }

  public String getClinicSectorCode() {
    return clinicSectorCode;
  }

  public void setClinicSectorCode(String clinicSectorCode) {
    this.clinicSectorCode = clinicSectorCode;
  }

  public String getClinicSectorTypeCode() {
    return clinicSectorTypeCode;
  }

  public void setClinicSectorTypeCode(String clinicSectorTypeCode) {
    this.clinicSectorTypeCode = clinicSectorTypeCode;
  }

  public Date getPrescriptionDate() {
    return prescriptionDate;
  }

  public void setPrescriptionDate(Date prescriptionDate) {
    this.prescriptionDate = prescriptionDate;
  }

  public Date getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(Date expiryDate) {
    this.expiryDate = expiryDate;
  }

  public boolean isCurrent() {
    return current;
  }

  public void setCurrent(boolean current) {
    this.current = current;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getPrescriptionSeq() {
    return prescriptionSeq;
  }

  public void setPrescriptionSeq(String prescriptionSeq) {
    this.prescriptionSeq = prescriptionSeq;
  }

  public String getPatientType() {
    return patientType;
  }

  public void setPatientType(String patientType) {
    this.patientType = patientType;
  }

  public String getPatientStatus() {
    return patientStatus;
  }

  public void setPatientStatus(String patientStatus) {
    this.patientStatus = patientStatus;
  }

  public int getWeeksDuration() {
    return weeksDuration;
  }

  public void setWeeksDuration(int weeksDuration) {
    this.weeksDuration = weeksDuration;
  }

  public String getReasonForUpdate() {
    return reasonForUpdate;
  }

  public void setReasonForUpdate(String reasonForUpdate) {
    this.reasonForUpdate = reasonForUpdate;
  }

  public String getTherapeuticLineCode() {
    return therapeuticLineCode;
  }

  public void setTherapeuticLineCode(String therapeuticLineCode) {
    this.therapeuticLineCode = therapeuticLineCode;
  }

  public String getTherapeuticRegimenCode() {
    return therapeuticRegimenCode;
  }

  public void setTherapeuticRegimenCode(String therapeuticRegimenCode) {
    this.therapeuticRegimenCode = therapeuticRegimenCode;
  }

  public String getDispenseTypeCode() {
    return dispenseTypeCode;
  }

  public void setDispenseTypeCode(String dispenseTypeCode) {
    this.dispenseTypeCode = dispenseTypeCode;
  }

  public Date getDateLeft() {
    return dateLeft;
  }

  public void setDateLeft(Date dateLeft) {
    this.dateLeft = dateLeft;
  }

  public Date getDateReceived() {
    return dateReceived;
  }

  public void setDateReceived(Date dateReceived) {
    this.dateReceived = dateReceived;
  }

  public boolean isModified() {
    return modified;
  }

  public void setModified(boolean modified) {
    this.modified = modified;
  }

  public Date getPackDate() {
    return packDate;
  }

  public void setPackDate(Date packDate) {
    this.packDate = packDate;
  }

  public Date getPickupDate() {
    return pickupDate;
  }

  public void setPickupDate(Date pickupDate) {
    this.pickupDate = pickupDate;
  }

  public Date getNextPickUpDate() {
    return nextPickUpDate;
  }

  public void setNextPickUpDate(Date nextPickUpDate) {
    this.nextPickUpDate = nextPickUpDate;
  }

  public int getWeeksSupply() {
    return weeksSupply;
  }

  public void setWeeksSupply(int weeksSupply) {
    this.weeksSupply = weeksSupply;
  }

  public Date getDateReturned() {
    return dateReturned;
  }

  public void setDateReturned(Date dateReturned) {
    this.dateReturned = dateReturned;
  }

  public int getStockReturned() {
    return stockReturned;
  }

  public void setStockReturned(int stockReturned) {
    this.stockReturned = stockReturned;
  }

  public int getPackageReturned() {
    return packageReturned;
  }

  public void setPackageReturned(int packageReturned) {
    this.packageReturned = packageReturned;
  }

  public String getReasonForPackageReturn() {
    return reasonForPackageReturn;
  }

  public void setReasonForPackageReturn(String reasonForPackageReturn) {
    this.reasonForPackageReturn = reasonForPackageReturn;
  }

  public String getDispenseModeCode() {
    return dispenseModeCode;
  }

  public void setDispenseModeCode(String dispenseModeCode) {
    this.dispenseModeCode = dispenseModeCode;
  }

  public String getJsonPrescribedDrug() {
    return jsonPrescribedDrug;
  }

  public void setJsonPrescribedDrug(String jsonPrescribedDrug) {
    this.jsonPrescribedDrug = jsonPrescribedDrug;
  }

  public String getJsonPackagedDrug() {
    return jsonPackagedDrug;
  }

  public void setJsonPackagedDrug(String jsonPackagedDrug) {
    this.jsonPackagedDrug = jsonPackagedDrug;
  }
}
