package com.example.floodmonitor.service;

import com.example.floodmonitor.exception.StationAlreadyExistsException;
import com.example.floodmonitor.exception.StationNotFoundException;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationWithWarningLevel;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {
    private final StationRepository stationRepository;
    private final WarningLevelService warningLevelService;

    public StationService(StationRepository stationRepository, WarningLevelService warningLevelService) {
        this.stationRepository = stationRepository;
        this.warningLevelService = warningLevelService;
    }

    public List<StationWithWarningLevel> getStations(String riverFilter, Boolean isActiveFilter, WarningLevel warningLevelFilter) {
        return stationRepository.findAll().stream().map(this::toStationWithWarningLevel)
                .filter(s -> riverFilter == null || s.station().river().equalsIgnoreCase(riverFilter))
                .filter(s -> isActiveFilter == null || s.station().isActive() == isActiveFilter)
                .filter(s -> warningLevelFilter == null || s.warningLevel() == warningLevelFilter)
                .toList();
    }

    public StationWithWarningLevel getStationById(String id) {
        return stationRepository.findById(id).map(this::toStationWithWarningLevel).orElseThrow(() -> new StationNotFoundException(id));
    }

    public void addStation(Station station) {
        if (!stationRepository.create(station)) throw new StationAlreadyExistsException(station.id());
    }


    private StationWithWarningLevel toStationWithWarningLevel(Station s) {
        return new StationWithWarningLevel(s, warningLevelService.getCurrentWarningLevel(s));
    }
}
