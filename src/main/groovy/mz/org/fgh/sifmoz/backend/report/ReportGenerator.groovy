package mz.org.fgh.sifmoz.backend.report

import mz.org.fgh.sifmoz.backend.utilities.Utilities
import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.JasperExportManager
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperReport
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.export.Exporter
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration
import org.apache.commons.collections.CollectionUtils

import java.sql.Connection

class ReportGenerator {

    static byte[] generateReport(Map<String, Object> parameters, String fileType, Collection reportObjects, String reportPath) {
        return generateReport(parameters, reportPath, fileType, null, reportObjects)
    }

    static byte[] generateReport(String reportPath, Map<String, Object> parameters, String fileType, Connection connection) {
        return generateReport(parameters, reportPath, fileType, connection, null)
    }

    static byte[] generateReport(Map<String, Object> parameters, String reportPath, String fileType, Connection connection, Collection reportObjects) {
        try {
            def jasperPrint
            JasperReport jasperReport = JasperCompileManager.compileReport(reportPath)
             if (CollectionUtils.isNotEmpty(reportObjects)) {
                JRBeanCollectionDataSource jrbeanCollectionDataSource = new JRBeanCollectionDataSource(reportObjects)
                jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jrbeanCollectionDataSource)
            } else {
                jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection)
            }
            if (fileType.equalsIgnoreCase("PDF")) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
                JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream)
                return byteArrayOutputStream.toByteArray()
            } else if (fileType.equalsIgnoreCase("XLS")) {
                SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
                configuration.setOnePagePerSheet(true);
                configuration.setIgnoreGraphics(false);

                File outputFile = new File("output.xlsx");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
                OutputStream fileOutputStream = new FileOutputStream(outputFile)
                Exporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
                exporter.setConfiguration(configuration);
                exporter.exportReport();
                byteArrayOutputStream.writeTo(fileOutputStream);

                return byteArrayOutputStream.toByteArray()
            } else throw new IllegalArgumentException("Report type not supported [" + fileType + "]")
        } catch (Exception e) {
            throw new RuntimeException("It's not possible to generate the pdf report.", e);
        }
    }

}
