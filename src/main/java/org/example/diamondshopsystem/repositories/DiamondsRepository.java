package org.example.diamondshopsystem.repositories;

import org.example.diamondshopsystem.entities.Diamond;
import org.example.diamondshopsystem.entities.DiscountCodes;
import org.example.diamondshopsystem.entities.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiamondsRepository  extends JpaRepository<Diamond,Integer> {

    @Query("SELECT d FROM Diamond d WHERE d.carat = :carat AND d.cut = :cut AND d.color = :color AND d.clarity = :clarity")
    List<Diamond> findAllByCaratAndCutAndColorAndClarity(@Param("carat") double carat, @Param("cut") String cut, @Param("color") String color, @Param("clarity") String clarity);

    @Query("SELECT d FROM Diamond d WHERE d.product.productId = :productId AND d.status = true ORDER BY d.diamondId DESC")
    Diamond findFirstByProduct_ProductIdAndStatusIsTrue(@Param("productId") int productId);
}
