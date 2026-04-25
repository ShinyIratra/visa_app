package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.visa.entity.VisaTransformable;

public interface VisaTransformableRepository extends JpaRepository<VisaTransformable, Integer> {
}