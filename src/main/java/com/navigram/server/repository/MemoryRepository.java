package com.navigram.server.repository;

import com.navigram.server.model.Memory;
import com.navigram.server.model.User;
import com.navigram.server.model.VisibilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemoryRepository extends JpaRepository<Memory, String> {
    List<Memory> findByUser(User user);

    List<Memory> findByVisibility(VisibilityType visibility);

    @Query(value = """
        SELECT m.* FROM memories m
        WHERE m.visibility = 'PUBLIC'
        """, nativeQuery = true)
    List<Memory> findAllPublicMemories();

    @Query(value = """
        SELECT m.* FROM memories m
        WHERE (m.visibility = 'PUBLIC'
           OR (m.visibility = 'FOLLOWERS' AND m.user_id IN
               (SELECT followed_id FROM user_followers WHERE follower_id = :userId))
           OR (m.user_id = :userId))
        AND (:lat IS NOT NULL AND :lng IS NOT NULL AND :distance IS NOT NULL)
        """, nativeQuery = true)
    List<Memory> findNearbyMemories(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("distance") double distance,
        @Param("userId") long userId
    );

    @Query(value = """
        SELECT m.* FROM memories m
        WHERE m.visibility = 'PUBLIC'
        AND :lat IS NOT NULL AND :lng IS NOT NULL AND :distance IS NOT NULL
        ORDER BY m.created_at DESC
        LIMIT 50
        """, nativeQuery = true)
    List<Memory> findNearbyPublicMemories(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("distance") double distance
    );

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE memories
        SET location = ST_GeomFromText(CONCAT('POINT(', :longitude, ' ', :latitude, ')'), 4326)
        WHERE id = :id
        """, nativeQuery = true)
    void updateLocation(
        @Param("id") String id,
        @Param("latitude") double latitude,
        @Param("longitude") double longitude
    );

    List<Memory> findByIsFlaggedTrue();

    long countByCreatedAtAfter(LocalDateTime date);

    long countByIsFlaggedTrue();
}
