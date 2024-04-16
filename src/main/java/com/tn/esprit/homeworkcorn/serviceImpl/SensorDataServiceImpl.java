package com.tn.esprit.homeworkcorn.serviceImpl;

import com.tn.esprit.homeworkcorn.daos.SensorDataDao;
import com.tn.esprit.homeworkcorn.entities.SensorData;
import com.tn.esprit.homeworkcorn.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class SensorDataServiceImpl implements SensorDataService {
    @Autowired
    private SensorDataDao sensorDataDao;
    @Override
    public void generateSensorData() throws IOException {
        String csvFilePath = "sensorData/news/sensor_data.csv";
        File file = new File(csvFilePath);

        // Create the file if it doesn't exist
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        final FileWriter csvWriter = new FileWriter(file);
        csvWriter.append("sensorId,reading,threshold\n"); // Header row
        final Random random = new Random();
        final StringBuilder stringBuilder = new StringBuilder();
        String sensorId;
        Double reading = null;
        Double threshold = null;
        for (int i = 1; i <= 100; i++) {
            sensorId = "hash_" + i;
            reading = random.nextDouble() * 100; // 0.0 to 99.9
            threshold = reading + random.nextDouble() * 10; // Threshold slightly higher than reading
            stringBuilder.setLength(0); // Clear the StringBuilder
            stringBuilder.append(sensorId).append(",").append(reading).append(",").append(threshold).append("\n");
            csvWriter.append(stringBuilder);
        }
        csvWriter.flush();
        csvWriter.close();
        System.out.println("Sensor data generated and saved to sensorData/news/sensor_data.csv");
    }
    @Scheduled(cron = "0 0/1 * * * *") // This runs the method every 1 minute
    @Override
    public void processNewFiles() throws IOException {
        File dir = new File("sensorData/news");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".csv"));
        if (files != null) {
            for (File file : files) {
                List<SensorData> sensorDataList = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    boolean firstLine = true;
                    while ((line = br.readLine()) != null) {
                        if (firstLine) {
                            firstLine = false;
                            continue;
                        }
                        String[] values = line.split(",");
                        SensorData sensorData = new SensorData();
                        sensorData.setSensorId(values[0]);
                        sensorData.setReading(Double.valueOf(values[1]));
                        sensorData.setThreshold(Double.valueOf(values[2]));
                        sensorDataList.add(sensorData);
                    }
                }
                sensorDataDao.saveAll(sensorDataList);
                System.out.println("> Successfully Saved all the sensorData from the New file to the DB \n");
                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                Files.move(file.toPath(), Paths.get("sensorData/archived/" + file.getName().replace(".csv", "") + "_" + timestamp + ".csv"), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("> Successfully moved the new File to the archived Directory \n");
            }
        }
    }
    @Scheduled(cron = "0 0/2 * * * *") // This runs the method every 2 minutes
    @Override
    public void filterReadingsUsingStreams() throws IOException {
        long startTime = System.currentTimeMillis();
        List<SensorData> allSensorData = sensorDataDao.findAll();
        List<SensorData> correctReadings = allSensorData.stream()
                .filter(data -> Math.abs(data.getReading() - data.getThreshold()) / data.getThreshold() <= 0.2)
                .collect(Collectors.toList());
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        writeCorrectReadingsToCsv(correctReadings, "sensorData/filtered/correct_readings_streams_"+timestamp+".csv");
        long endTime = System.currentTimeMillis();
        System.out.println("> Execution time of filter-Readings-Using-Streams: " + (endTime - startTime) + "ms");
    }
    @Scheduled(cron = "0 0/3 * * * *") // This runs the method every 3 minutes
    @Override
    public void filterReadingsUsingDatabaseQuery() throws IOException {
        long startTime = System.currentTimeMillis();
        List<SensorData> correctReadings = sensorDataDao.findCorrectReadings();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        writeCorrectReadingsToCsv(correctReadings, "sensorData/filtered/correct_readings_dbQ_"+timestamp+".csv");
        long endTime = System.currentTimeMillis();
        System.out.println("> Execution time of filter-Readings-Using-Database-Query: " + (endTime - startTime) + "ms");

    }
    @Scheduled(cron = "0 0/1 * * * *") // This runs the method every 1 minute
    @Override
    public void printCorrectReadingsPercentage(){
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        List<SensorData> listSensorData = sensorDataDao.findAll();
        long totalReadings = sensorDataDao.count();
        long correctReadings = listSensorData.stream()
                .filter(data -> Math.abs(data.getReading() - data.getThreshold()) / data.getThreshold() <= 0.2)
                .count();
        if(!listSensorData.isEmpty()) {
            double percentage = (double) correctReadings / totalReadings * 100;
            System.out.println("==> " + timestamp + " :| Percentage of correct readings: " + percentage + "%");
        }
    }

    @Override
    public void writeCorrectReadingsToCsv(List<SensorData> correctReadings, String filePath) throws IOException {
        FileWriter csvWriter = new FileWriter(filePath);
        csvWriter.append("sensorId,reading,threshold\n");
        for (SensorData data : correctReadings) {
            csvWriter.append(String.join(",", data.getSensorId(), data.getReading().toString(), data.getThreshold().toString()));
            csvWriter.append("\n");
        }
        csvWriter.flush();
        csvWriter.close();
    }
}
