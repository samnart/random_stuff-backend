package com.random_stuff.api.repository;
 
import com.random_stuff.api.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
 
    Optional<Category> findBySlug(String slug);
 
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
 
    boolean existsBySlug(String slug);
}