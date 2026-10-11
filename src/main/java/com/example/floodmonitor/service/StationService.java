package com.example.floodmonitor.service;

import com.example.floodmonitor.dto.StationResponse;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StationService {
    private final StationRepository stationRepository;
    private final WarningLevelService warningLevelService;

    public StationService(StationRepository stationRepository, WarningLevelService warningLevelService) {
        this.stationRepository = stationRepository;
        this.warningLevelService = warningLevelService;
    }

    private StationResponse toStationResponse(Station s) {
        return new StationResponse(s.id(), s.name(), s.river(), s.location(), s.normalWaterLevel(), s.warningWaterLevel(), s.criticalWaterLevel(), s.isActive(), warningLevelService.getCurrentWarningLevel(s));
    }

    public List<StationResponse> getStations() {
        return stationRepository.findAll().stream().map(this::toStationResponse).toList();
    }

    public Optional<StationResponse> getStationById(String id) {
        return stationRepository.findById(id).map(this::toStationResponse);
    }

    public boolean addStation(Station station) {
        return stationRepository.create(station);
    }

}
