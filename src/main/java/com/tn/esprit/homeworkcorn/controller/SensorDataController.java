package com.tn.esprit.homeworkcorn.controller;

import com.tn.esprit.homeworkcorn.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sensor-data")
public class SensorDataController {
    @Autowired
    private SensorDataService sensorDataService;

    @GetMapping
    public ResponseEntity<String> sensorDataGenerator() throws Exception{
        sensorDataService.generateSensorData();
        return ResponseEntity.ok("Sensor data generated and saved in news directory");
    }
}
