package com.agrorent.api.controller;

import com.agrorent.api.dto.EquipmentDTO;
import com.agrorent.api.model.AvailabilityStatus;
import com.agrorent.api.model.Equipment;
import com.agrorent.api.security.UserPrincipal;
import com.agrorent.api.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<List<Equipment>> filterEquipment(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) AvailabilityStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        List<Equipment> equipmentList = equipmentService.searchAndFilter(categoryId, location, status, minPrice, maxPrice);
        return ResponseEntity.ok(equipmentList);
    }

    @GetMapping("/my-listings")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Equipment>> getMyListings(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<Equipment> listings = equipmentService.getOwnerEquipment(currentUser.getId());
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/locations")
    public ResponseEntity<List<String>> getUniqueLocations() {
        List<String> locations = equipmentService.getUniqueLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Equipment> createEquipment(
            @Valid @RequestBody EquipmentDTO dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        Equipment created = equipmentService.createEquipment(dto, currentUser.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Equipment> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentDTO dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        Equipment updated = equipmentService.updateEquipment(id, dto, currentUser.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteEquipment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        equipmentService.deleteEquipment(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
