package com.echomap.server.repository;

import com.echomap.server.model.Memory;
import com.echomap.server.model.User;
import com.echomap.server.model.VisibilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MemoryRepository extends JpaRepository<Memory, String> {
    List<Memory> findByUser(User user);
    
    List<Memory> findByVisibility(VisibilityType visibility);

    @Query(value = """
        SELECT m.* FROM memories m 
        WHERE ST_Distance_Sphere(m.location, POINT(:lng, :lat)) <= :distance
        AND (m.visibility = 'PUBLIC' 
             OR (m.visibility = 'FOLLOWERS' AND m.user_id IN 
                (SELECT followed_id FROM user_followers WHERE follower_id = :userId))
             OR (m.user_id = :userId))
        """, nativeQuery = true)
    List<Memory> findNearbyMemories(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("distance") double distance,
        @Param("userId") String userId
    );

    @Query(value = """
        SELECT m.* FROM memories m 
        WHERE ST_Distance_Sphere(m.location, POINT(:lng, :lat)) <= :distance
        AND m.visibility = 'PUBLIC'
        """, nativeQuery = true)
    List<Memory> findNearbyPublicMemories(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("distance") double distance
    );
}