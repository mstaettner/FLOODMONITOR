package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Station;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StationRepository {
    private final List<Station> stations = new ArrayList<>();

    public List<Station> findAll() {
        return stations;
    }

    public Optional<Station> findById(String id) {
        return stations.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public Station save(Station station) {
        if (station.getId() == null || station.getId().isBlank()) {
            station.setId(String.valueOf(stations.size() + 1));
        }
        stations.add(station);
        return station;
    }
}