package org.example.diamondshopsystem.repositories;


import org.example.diamondshopsystem.entities.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payments,Integer> {
}
