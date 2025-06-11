package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.TestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestTypeRepository extends JpaRepository<TestType, Long> {

    /**
     * Tìm kiếm theo tên xét nghiệm
     */
    Optional<TestType> findByName(String name);

    /**
     * Tìm kiếm theo danh mục
     */
    List<TestType> findByCategory(String category);

    /**
     * Tìm kiếm theo nhóm xét nghiệm
     */
    List<TestType> findByTestGroup(String testGroup);

    /**
     * Tìm kiếm xét nghiệm HIV theo danh mục
     */
    @Query("SELECT t FROM TestType t WHERE t.testGroup = 'HIV' AND t.category = :category ORDER BY t.name")
    List<TestType> findHIVTestsByCategory(@Param("category") String category);

    /**
     * Tìm kiếm tất cả xét nghiệm HIV
     */
    @Query("SELECT t FROM TestType t WHERE t.testGroup = 'HIV' ORDER BY t.category, t.name")
    List<TestType> findAllHIVTests();

    /**
     * Tìm kiếm theo khoảng giá
     */
    @Query("SELECT t FROM TestType t WHERE t.price BETWEEN :minPrice AND :maxPrice ORDER BY t.price")
    List<TestType> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    List<TestType> findByActiveTrue();
}
