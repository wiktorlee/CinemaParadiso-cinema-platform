package pl.cinemaparadiso.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.audit.CustomRevisionEntity;

/**
 * Repository dla encji CustomRevisionEntity (logi audytowe)
 */
@Repository
public interface CustomRevisionEntityRepository extends JpaRepository<CustomRevisionEntity, Long> {
    
    /**
     * Pobiera rewizje posortowane po czasie (najnowsze pierwsze)
     * Sortowanie jest przekazywane przez Pageable
     */
    Page<CustomRevisionEntity> findAll(Pageable pageable);
}

