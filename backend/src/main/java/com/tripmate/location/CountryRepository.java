package com.tripmate.location;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCode(String code);

    @Query("select distinct c from Country c left join fetch c.cities order by c.displayOrder asc")
    List<Country> findAllWithCities();
}
