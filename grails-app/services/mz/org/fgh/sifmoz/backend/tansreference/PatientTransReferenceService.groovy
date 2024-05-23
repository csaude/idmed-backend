package mz.org.fgh.sifmoz.backend.tansreference

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episode.EpisodeService
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescriptionDrug.PrescribedDrug
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntrance
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(PatientTransReference)
abstract class PatientTransReferenceService implements IPatientTransReferenceService {

    @Autowired
    IPatientVisitDetailsService visitDetailsService
    EpisodeService episodeService

    @Override
    TransReferenceData getPatientTransReferenceDetailsByNid(String nid){
       PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.findByValue(nid)
        PatientTransReference pt = PatientTransReference.findByIdentifier(patientServiceIdentifier)

        Episode episode = episodeService.getLastInitialEpisodeByIdentifier(pt.identifier.id)
        PatientVisitDetails lastVisitDetails = visitDetailsService.getLastVisitByEpisodeId(episode.id)
        TransReferenceData transReferenceData = new TransReferenceData()
        transReferenceData.setId(111)
   //     transReferenceData.setDestinationClinic(pt.identifier.clinic)
    //    transReferenceData.setOriginClinic(pt.getOrigin())
  //    transReferenceData.setPatientServiceIdentifier(pt.identifier)
    //    transReferenceData.setPatientVisit(lastVisitDetails.patientVisit)
        /*     transReferenceData.setPrescription(lastVisitDetails.prescription)
             transReferenceData.setPack(lastVisitDetails.pack)


             List<PrescribedDrug> prescribedDrugList = new ArrayList<>()
             for(PrescribedDrug prescribedDrug : lastVisitDetails.prescription.prescribedDrugs) {
                 prescribedDrugList.add(prescribedDrug)
             }
             transReferenceData.setPrescribedDrugs(prescribedDrugList)

             List<PackagedDrug> packagedDrugList = new ArrayList<>()
             for(PackagedDrug packagedDrug : lastVisitDetails.pack.packagedDrugs) {
                 packagedDrugList.add(packagedDrug)
             }
             transReferenceData.setPackagedDrugs(packagedDrugList)

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
          */
        transReferenceData.setFirstNames(pt.identifier.patient.firstNames)
        transReferenceData.setLastNames(pt.identifier.patient.lastNames)
        transReferenceData.setMiddleNames(pt.identifier.patient.middleNames)
        transReferenceData.setGender(pt.identifier.patient.gender)
        transReferenceData.setDateOfBirth(pt.identifier.patient.dateOfBirth)
        transReferenceData.setCellphone(pt.identifier.patient.cellphone)
        transReferenceData.setAlternativeCellphone(pt.identifier.patient.alternativeCellphone)
        transReferenceData.setAccountstatus(pt.identifier.patient.accountstatus)
        transReferenceData.setHisUuid(pt.identifier.patient.hisUuid)
        transReferenceData.setHisLocation(pt.identifier.patient.hisLocation)
        transReferenceData.setHisLocationName(pt.identifier.patient.hisLocationName)
        transReferenceData.setAddress(pt.identifier.patient.address)
        transReferenceData.setAddressReference(pt.identifier.patient.addressReference)
        transReferenceData.setProvinceCode(pt.identifier.patient.province.code)
//        transReferenceData.setBairroCode(pt.identifier.patient.bairro.code)
        transReferenceData.setDistrictCode(pt.identifier.patient.district.code)
     // transReferenceData.setPostoAdministrativoCode(pt.identifier.patient.postoAdministrativo.code)
        transReferenceData.setOriginClinicUuid(pt.origin.getUuid())
        transReferenceData.setDestinationClinicUuid(pt.getDestination())
        transReferenceData.setClinicalServiceCode(pt.identifier.service.code)
        transReferenceData.setPatientNid(pt.identifier.value)
        transReferenceData.setStartDate(pt.identifier.startDate)
        transReferenceData.setEndDate(pt.identifier.endDate)
        transReferenceData.setReopenDate(pt.identifier.reopenDate)
        transReferenceData.setState(pt.identifier.state)
        transReferenceData.setPrefered(pt.identifier.prefered)
        transReferenceData.setIdentifierTypeCode(pt.identifier.identifierType.code)
        transReferenceData.setPatientVisitDate(lastVisitDetails.patientVisit.visitDate)
        transReferenceData.setEpisodeDate(lastVisitDetails.episode.episodeDate)
        transReferenceData.setStartStopReasonCode(lastVisitDetails.episode.startStopReason.code)
        transReferenceData.setEpisodeNotes(lastVisitDetails.episode.notes)
        transReferenceData.setEpisodeTypeCode(lastVisitDetails.episode.episodeType.code)
        transReferenceData.setClinicSectorCode(lastVisitDetails.episode.clinicSector.code)
        transReferenceData.setClinicSectorTypeCode(lastVisitDetails.episode.clinicSector.facilityType.code)
        transReferenceData.setPrescriptionDate(lastVisitDetails.prescription.prescriptionDate)
        transReferenceData.setExpiryDate(lastVisitDetails.prescription.expiryDate)
        transReferenceData.setNotes(lastVisitDetails.prescription.notes)
        transReferenceData.setPrescriptionSeq(lastVisitDetails.prescription.prescriptionSeq)
        transReferenceData.setPatientType(lastVisitDetails.prescription.patientType)
        transReferenceData.setPatientStatus(lastVisitDetails.prescription.patientStatus)
        transReferenceData.setWeeksDuration(lastVisitDetails.prescription.duration.weeks)
        transReferenceData.setReasonForUpdate(lastVisitDetails.prescription.prescriptionDetails.getAt(0).reasonForUpdate)
        transReferenceData.setTherapeuticLineCode(lastVisitDetails.prescription.prescriptionDetails.getAt(0).therapeuticLine.code)
        transReferenceData.setTherapeuticRegimenCode(lastVisitDetails.prescription.prescriptionDetails.getAt(0).therapeuticRegimen.code)
        transReferenceData.setDispenseTypeCode(lastVisitDetails.prescription.prescriptionDetails.getAt(0).dispenseType.code)
        transReferenceData.setDateLeft(lastVisitDetails.pack.dateLeft)
        transReferenceData.setDateReceived(lastVisitDetails.pack.dateReceived)
        transReferenceData.setDateReturned(lastVisitDetails.pack.dateReturned)
        transReferenceData.setPickupDate(lastVisitDetails.pack.pickupDate)
        transReferenceData.setPackDate(lastVisitDetails.pack.packDate)
        transReferenceData.setNextPickUpDate(lastVisitDetails.pack.nextPickUpDate)
        transReferenceData.setWeeksSupply(lastVisitDetails.pack.weeksSupply)
        transReferenceData.setStockReturned(lastVisitDetails.pack.stockReturned)
        transReferenceData.setPackageReturned(lastVisitDetails.pack.packageReturned)
        transReferenceData.setReasonForPackageReturn(lastVisitDetails.pack.reasonForPackageReturn)
        transReferenceData.setDispenseModeCode(lastVisitDetails.pack.dispenseMode.code)

     if (!lastVisitDetails.prescription.getPrescribedDrugs().isEmpty()) {

      Map<String, Object> pd = new HashMap<String, Object>()
      ArrayList listPD = new ArrayList()

      for (PrescribedDrug prescribedDrugs : lastVisitDetails.prescription.getPrescribedDrugs()) {
       pd.put("drugCode", prescribedDrugs.drug.fnmCode)
       pd.put("amtPerTime", prescribedDrugs.amtPerTime)
       pd.put("timesPerDay", prescribedDrugs.timesPerDay)
       pd.put("qtyPrescribed", prescribedDrugs.qtyPrescribed)
       pd.put("form", prescribedDrugs.form)
       listPD.add(pd);
      }
      transReferenceData.setJsonPrescribedDrug(listPD.toString());
     }

     if (!lastVisitDetails.pack.packagedDrugs.isEmpty()) {

      Map<String, Object> packagedDrugs = new HashMap<String, Object>()
      ArrayList listPackD = new ArrayList()

      for (PackagedDrug packageDrug : lastVisitDetails.pack.packagedDrugs) {
       packagedDrugs.put("drugCode", packageDrug.drug.fnmCode)
       packagedDrugs.put("quantitySupplied", packageDrug.quantitySupplied.intValue())
       packagedDrugs.put("nextPickUpDate", packageDrug.nextPickUpDate)
       packagedDrugs.put("toContinue", packageDrug.toContinue)
       listPackD.add(packageDrug);

      }
      transReferenceData.setJsonPackagedDrug(listPackD.toString());
     }
        return transReferenceData;

    }
}
