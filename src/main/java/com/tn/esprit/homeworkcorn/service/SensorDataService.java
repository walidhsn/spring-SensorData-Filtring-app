package com.tn.esprit.homeworkcorn.service;

import com.tn.esprit.homeworkcorn.entities.SensorData;

import java.io.IOException;
import java.util.List;

public interface SensorDataService {
    void generateSensorData() throws IOException;
    void processNewFiles() throws IOException;
    void filterReadingsUsingStreams() throws IOException;
    void filterReadingsUsingDatabaseQuery() throws IOException;
    void printCorrectReadingsPercentage();
    void writeCorrectReadingsToCsv(List<SensorData> correctReadings, String filePath) throws IOException;
}
