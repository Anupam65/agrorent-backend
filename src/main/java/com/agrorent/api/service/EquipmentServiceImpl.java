package com.agrorent.api.service;

import com.agrorent.api.dto.EquipmentDTO;
import com.agrorent.api.exception.BadRequestException;
import com.agrorent.api.exception.ResourceNotFoundException;
import com.agrorent.api.model.*;
import com.agrorent.api.repository.CategoryRepository;
import com.agrorent.api.repository.EquipmentRepository;
import com.agrorent.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Equipment createEquipment(EquipmentDTO dto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner user not found"));

        if (owner.getRole() != Role.USER && owner.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only Users or Admins can list equipment");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Equipment equipment = Equipment.builder()
                .owner(owner)
                .category(category)
                .name(dto.getName())
                .description(dto.getDescription())
                .pricePerDay(dto.getPricePerDay())
                .pricePerHour(dto.getPricePerHour())
                .availabilityStatus(dto.getAvailabilityStatus() != null ? dto.getAvailabilityStatus() : AvailabilityStatus.AVAILABLE)
                .imageUrl(dto.getImageUrl())
                .location(dto.getLocation())
                .build();

        return equipmentRepository.save(equipment);
    }

    @Override
    @Transactional
    public Equipment updateEquipment(Long id, EquipmentDTO dto, Long userId) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce owner authorization
        if (!equipment.getOwner().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("You do not own this equipment resource");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        equipment.setName(dto.getName());
        equipment.setCategory(category);
        equipment.setDescription(dto.getDescription());
        equipment.setPricePerDay(dto.getPricePerDay());
        equipment.setPricePerHour(dto.getPricePerHour());
        equipment.setLocation(dto.getLocation());
        
        if (dto.getAvailabilityStatus() != null) {
            equipment.setAvailabilityStatus(dto.getAvailabilityStatus());
        }
        if (dto.getImageUrl() != null) {
            equipment.setImageUrl(dto.getImageUrl());
        }

        return equipmentRepository.save(equipment);
    }

    @Override
    @Transactional
    public void deleteEquipment(Long id, Long userId) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce owner authorization
        if (!equipment.getOwner().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("You do not own this equipment resource");
        }

        equipmentRepository.delete(equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> getOwnerEquipment(Long ownerId) {
        return equipmentRepository.findByOwnerId(ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> searchAndFilter(Long categoryId, String location, AvailabilityStatus status, BigDecimal minPrice, BigDecimal maxPrice) {
        return equipmentRepository.filterEquipment(categoryId, location, status, minPrice, maxPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUniqueLocations() {
        return equipmentRepository.findDistinctLocations();
    }
}
