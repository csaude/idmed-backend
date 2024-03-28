//file:noinspection GrDeprecatedAPIUsage
package mz.org.fgh.sifmoz.backend.patient

import com.lowagie.text.pdf.PdfStream
//import grails.plugins.jasper.JasperExportFormat
//import grails.plugins.jasper.JasperReportDef
import grails.rest.*
import grails.converters.*
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import net.sf.jasperreports.engine.JREmptyDataSource
import net.sf.jasperreports.engine.JRExporter
import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.JasperExportManager
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import net.sf.jasperreports.engine.export.JRPdfExporter
import org.apache.commons.io.FileUtils
import org.springframework.core.io.ClassPathResource
import org.springframework.orm.hibernate5.SessionFactoryUtils
import yakworks.jasper.spring.JasperReportDef
import yakworks.reports.ReportFormat


//import yakworks.jasper.spring.JasperReportDef

class ReportController {
	static responseFormats = ['json', 'xml']

    def sessionFactory
    def jasperService

    def index() {
        try {

            Clinic clinic = Clinic.findById("ff8081817c668dcc017c66dc3d330002")
            ClinicalService clinicalService = ClinicalService.findByCode("TARV")
            // SessionFactoryUtils.getDataSource(sessionFactory).getConnection()
            List<PatientServiceIdentifier> patients =   PatientServiceIdentifier.findAllByStartDateIsNotNullAndEndDateIsNullAndClinicAndService(clinic,clinicalService)

            Map<String, Object> map = new HashMap<String, Object>()
            map.put("path", "/home/muhammad/IdeaProjects/SIFMOZ-Backend-New/src/main/webapp/reports");
            map.put("clinic", clinic.getClinicName())
            map.put("clinicid", clinic.getId())
            Map<String, Object> map1 = new HashMap<String, Object>()
            map1.put("clinicname", clinic.getClinicName())
         //   List<Map<String, Object>> reportObjects = new ArrayList<>()
            List<Map<String, Object>> reportObjects = new ArrayList<Map<String, Object>>()
            for (PatientServiceIdentifier patient:patients) {
                Map<String, Object> reportObject = new HashMap<String, Object>()
                reportObject.put("nid", patient.value)
                reportObject.put("name", patient.patient.firstNames)
                reportObject.put("gender", patient.patient.gender)
                reportObject.put("birthDate", patient.patient.dateOfBirth)
                reportObject.put("initTreatmentDate", patient.startDate)
                reportObjects.add(reportObject)
            }
            reportObjects.add(map1)
            JRMapCollectionDataSource mapCollectionDataSource = new JRMapCollectionDataSource(reportObjects)
            JasperReport jasperReport = JasperCompileManager.compileReport("/home/muhammad/IdeaProjects/SIFMOZ-Backend-New/src/main/webapp/reports/RelatorioPacientesActivos.jrxml")
            def jasperPrint = JasperFillManager.fillReport(jasperReport, map, mapCollectionDataSource)
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
            JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream)
            render(file: byteArrayOutputStream.toByteArray(), contentType: 'application/pdf')
       //     response.setContentType("application/pdf")  //<-- you'll have to handle this dynamically at some point
        //    response.setHeader("Content-disposition", "attachment;filename=${11}")
         //   response.outputStream <<  byteArrayOutputStream.toByteArray()
        } catch (Exception e) {
            throw new RuntimeException("It's not possible to generate the pdf report.", e);
        }
    }

}
