package com.agrorent.api.repository;

import com.agrorent.api.model.AvailabilityStatus;
import com.agrorent.api.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    
    @Query("SELECT e FROM Equipment e WHERE e.owner.id = :ownerId")
    List<Equipment> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT e FROM Equipment e WHERE e.category.id = :categoryId")
    List<Equipment> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT e FROM Equipment e WHERE e.availabilityStatus = :status")
    List<Equipment> findByAvailabilityStatus(@Param("status") AvailabilityStatus status);

    @Query("SELECT DISTINCT e.location FROM Equipment e WHERE e.location IS NOT NULL AND e.location != ''")
    List<String> findDistinctLocations();

    @Query("SELECT e FROM Equipment e WHERE " +
            "(:categoryId IS NULL OR e.category.id = :categoryId) AND " +
            "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:status IS NULL OR e.availabilityStatus = :status) AND " +
            "(:minPrice IS NULL OR e.pricePerDay >= :minPrice) AND " +
            "(:maxPrice IS NULL OR e.pricePerDay <= :maxPrice)")
    List<Equipment> filterEquipment(
            @Param("categoryId") Long categoryId,
            @Param("location") String location,
            @Param("status") AvailabilityStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}
