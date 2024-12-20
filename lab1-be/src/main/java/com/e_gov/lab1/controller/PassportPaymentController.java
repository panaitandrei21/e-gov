package com.e_gov.lab1.controller;

import com.e_gov.lab1.model.PassportPayment;
import com.e_gov.lab1.repository.PassportPaymentRepository;
import com.e_gov.lab1.service.PassportPaymentService;
import com.e_gov.lab1.service.ReportService;
import org.jfree.chart.JFreeChart;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")  // Permite accesul din Angular (localhost:4200)
public class PassportPaymentController {

    @Autowired
    private PassportPaymentRepository passportPaymentRepository;

    @Autowired
    private PassportPaymentService passportPaymentService;


    @PostMapping("/submit")
    public ResponseEntity<ByteArrayResource> submitPayment(@RequestBody PassportPayment passportPayment) {
        try {
            JSONObject jsonObject = new JSONObject(passportPayment);
            String xmlString = XML.toString(jsonObject);
            passportPayment.setXmlData(xmlString);
            passportPayment.setPaymentDate(Date.from(Instant.now()));
            PassportPayment savedPayment = passportPaymentRepository.save(passportPayment);

            List<Map<String, Object>> queryResults = new ArrayList<>();
            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("Nume complet", savedPayment.getFullName());
            paymentDetails.put("CNP", savedPayment.getCnp());
            paymentDetails.put("Adresă", savedPayment.getAddress());
            paymentDetails.put("Tip pașaport", savedPayment.getPassportType());
            paymentDetails.put("Taxă de procesare", savedPayment.getProcessingFee());
            paymentDetails.put("Sumă totală", savedPayment.getTotalAmount());

            queryResults.add(paymentDetails);

            ByteArrayOutputStream pdfStream = generatePdfStream(queryResults);

            ByteArrayResource resource = new ByteArrayResource(pdfStream.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=factura-pasaport.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    public static ByteArrayOutputStream generatePdfStream(List<Map<String, Object>> queryResults) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        for (Map<String, Object> row : queryResults) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String label = entry.getKey();
                Object value = entry.getValue();
                String text = label + ": " + value.toString();
                Paragraph paragraph = new Paragraph(text);
                document.add(paragraph);
            }
            document.add(new Paragraph("\n"));
        }

        document.close();
        return outputStream;
    }
    @GetMapping("/generate-pdf")
    public ResponseEntity<byte[]> generateReport() {
        try {
            List<Object[]> passportTypes = passportPaymentRepository.countPassportTypes();
            List<Object[]> paymentSums = passportPaymentRepository.sumTotalPaymentsByMonth();

            JFreeChart pieChart = passportPaymentService.createPieChart(passportTypes);
            JFreeChart lineChart = passportPaymentService.createLineChart(paymentSums);

            Document document = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            passportPaymentService.addChartToPdf(document, pieChart);
            passportPaymentService.addChartToPdf(document, lineChart);

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
