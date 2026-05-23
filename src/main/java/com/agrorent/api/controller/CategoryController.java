package com.agrorent.api.controller;

import com.agrorent.api.model.Category;
import com.agrorent.api.model.Equipment;
import com.agrorent.api.repository.CategoryRepository;
import com.agrorent.api.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        List<Equipment> equipments = equipmentRepository.findByCategoryId(id);
        equipmentRepository.deleteAll(equipments);
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
