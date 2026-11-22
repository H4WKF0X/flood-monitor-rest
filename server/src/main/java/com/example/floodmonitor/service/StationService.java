package com.example.floodmonitor.service;

import com.example.floodmonitor.dto.StationListResponse;
import com.example.floodmonitor.dto.StationResponse;
import com.example.floodmonitor.exception.StationAlreadyExistsException;
import com.example.floodmonitor.exception.StationNotFoundException;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.WarningLevel;
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

    public StationListResponse getStations(String riverFilter, Boolean isActiveFilter, WarningLevel warningLevelFilter) {
        List<StationResponse> stationResponses = stationRepository.findAll().stream().map(this::toStationResponse)
                .filter(s -> riverFilter == null || s.river().equalsIgnoreCase(riverFilter))
                .filter(s -> isActiveFilter == null || s.isActive() == isActiveFilter)
                .filter(s -> warningLevelFilter == null || s.currentWarningLevel() == warningLevelFilter)
                .toList();
        return new StationListResponse(stationResponses,  stationResponses.size());
    }

    public StationResponse getStationById(String id) {
        return stationRepository.findById(id).map(this::toStationResponse).orElseThrow(() -> new StationNotFoundException(id));
    }

    public void addStation(Station station) {
        if (!stationRepository.create(station)) throw new StationAlreadyExistsException(station.id());
    }


    private StationResponse toStationResponse(Station s) {
        return new StationResponse(s.id(), s.name(), s.river(), s.location(), s.normalWaterLevel(), s.warningWaterLevel(), s.criticalWaterLevel(), s.isActive(), warningLevelService.getCurrentWarningLevel(s));
    }
}
