package com.tn.esprit.homeworkcorn.daos;

import com.tn.esprit.homeworkcorn.entities.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SensorDataDao extends JpaRepository<SensorData,Long> {
    @Query("SELECT s FROM SensorData s WHERE ABS(s.reading - s.threshold) / s.threshold <= 0.2")
    List<SensorData> findCorrectReadings();
}
