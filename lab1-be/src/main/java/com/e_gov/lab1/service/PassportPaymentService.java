package com.e_gov.lab1.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import lombok.RequiredArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PassportPaymentService {

    public JFreeChart createPieChart(List<Object[]> passportTypes) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        for (Object[] row : passportTypes) {
            Integer passportType = (Integer) row[0];
            Long count = (Long) row[1];

            String label;
            if (passportType == 50) {
                label = "Pașaport 50 RON";
            } else if (passportType == 100) {
                label = "Pașaport 100 RON";
            } else if (passportType == 150) {
                label = "Pașaport 150 RON";
            } else {
                label = "Alt tip pașaport";
            }

            dataset.setValue(label, count);
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Distribuția tipurilor de pașapoarte",  // Titlul graficului
                dataset,                               // Dataset-ul
                true,                                  // Legendă
                true,                                  // Tooltips
                false                                  // URLs
        );

        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setLabelGenerator(
                new StandardPieSectionLabelGenerator("{0}: {1} ({2})")
        );

        return pieChart;
    }

    public JFreeChart createLineChart(List<Object[]> paymentSums) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, Double> sortedData = new TreeMap<>();

        for (Object[] row : paymentSums) {
            Integer year = (Integer) row[0];
            Integer month = (Integer) row[1];
            Double sum = (Double) row[2];

            String key = String.format("%04d-%02d", year, month);

            sortedData.put(key, sum);
        }

        for (Map.Entry<String, Double> entry : sortedData.entrySet()) {
            String key = entry.getKey();
            Double sum = entry.getValue();

            dataset.addValue(sum, "Sumă totală", key);
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Sumele totale de plată pe lună", // Titlul graficului
                "Lună",                          // Eticheta axei X
                "Sumă totală",                   // Eticheta axei Y
                dataset,                          // Dataset-ul
                PlotOrientation.VERTICAL,         // Orientarea graficului (verticală)
                true,                             // Legenda activată
                true,                             // Tooltips activat
                false                             // URL-uri inactive
        );

        // Returnăm graficul de tip linie
        return lineChart;
    }

    // Metoda pentru a adăuga un grafic într-un document PDF
    public void addChartToPdf(Document document, JFreeChart chart) throws Exception {
        // Convertim graficul într-o imagine
        BufferedImage chartImage = chart.createBufferedImage(500, 300);

        // Convertim imaginea într-un format care poate fi adăugat în PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(chartImage, "png", baos);
        baos.flush();
        byte[] imageBytes = baos.toByteArray();
        Image image = Image.getInstance(imageBytes);
        document.add(image);
    }
}
