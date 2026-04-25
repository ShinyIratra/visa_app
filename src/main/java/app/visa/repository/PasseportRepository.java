package app.visa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.visa.entity.Passeport;

public interface PasseportRepository extends JpaRepository<Passeport, Integer> {
}
