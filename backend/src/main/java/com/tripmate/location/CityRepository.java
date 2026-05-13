package com.tripmate.location;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByCode(String code);

    @Query("select c from City c join fetch c.country where c.code = :code")
    Optional<City> findWithCountryByCode(@Param("code") String code);
}
