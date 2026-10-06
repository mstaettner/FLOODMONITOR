package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.service.SimulationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class StationController {

    private final SimulationService simulationService;

    public StationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/stations")
    public List<Station> getStations() {
        return simulationService.generateStations();
    }

    @GetMapping("/stations/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable String id) {
        return simulationService.getStationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stations/count")
    public int countStations(){
        List<Station> stations = simulationService.generateStations();
        return stations.size();
    }

    @PostMapping("/stations")
    public ResponseEntity<Station> createStation(@RequestBody Station station){
        Station createdStation = simulationService.addStation(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStation);
    }
}
