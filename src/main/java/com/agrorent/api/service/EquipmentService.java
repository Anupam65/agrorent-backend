package com.agrorent.api.service;

import com.agrorent.api.dto.EquipmentDTO;
import com.agrorent.api.model.AvailabilityStatus;
import com.agrorent.api.model.Equipment;

import java.math.BigDecimal;
import java.util.List;

public interface EquipmentService {
    Equipment createEquipment(EquipmentDTO dto, Long ownerId);
    Equipment updateEquipment(Long id, EquipmentDTO dto, Long userId);
    void deleteEquipment(Long id, Long userId);
    Equipment getEquipmentById(Long id);
    List<Equipment> getAllEquipment();
    List<Equipment> getOwnerEquipment(Long ownerId);
    List<Equipment> searchAndFilter(Long categoryId, String location, AvailabilityStatus status, BigDecimal minPrice, BigDecimal maxPrice);
    List<String> getUniqueLocations();
}
