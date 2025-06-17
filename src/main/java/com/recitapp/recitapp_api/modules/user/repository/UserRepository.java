package com.recitapp.recitapp_api.modules.user.repository;

import com.recitapp.recitapp_api.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
    Optional<User> findByEmail(String email);
    Optional<User> findByDni(String dni);
    Optional<User> findByFirebaseUid(String firebaseUid);

    // Método para buscar un usuario por nombre, apellido y DNI
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) = LOWER(:firstName) AND LOWER(u.lastName) = LOWER(:lastName) AND u.dni = :dni")
    Optional<User> findByFirstNameAndLastNameAndDni(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("dni") String dni);

    // Métodos para recomendaciones personalizadas
    @Query("SELECT af.artist.id FROM ArtistFollower af WHERE af.user.id = :userId")
    List<Long> findFollowedArtistIds(@Param("userId") Long userId);

    @Query("SELECT DISTINCT ag.genre.name FROM ArtistGenre ag " +
           "WHERE ag.artist.id IN (" +
           "    SELECT DISTINCT e.mainArtist.id FROM Event e " +
           "    JOIN Ticket t ON t.event.id = e.id " +
           "    WHERE t.user.id = :userId AND e.mainArtist.id IS NOT NULL" +
           "    UNION " +
           "    SELECT DISTINCT ea.artist.id FROM EventArtist ea " +
           "    JOIN Ticket t ON t.event.id = ea.event.id " +
           "    WHERE t.user.id = :userId" +
           ")")
    List<String> findUserPreferredGenres(@Param("userId") Long userId);

    @Query("SELECT DISTINCT t.event.id FROM Ticket t WHERE t.user.id = :userId")
    List<Long> findUserEventIds(@Param("userId") Long userId);

    // Método para obtener IDs de recintos seguidos
    @Query("SELECT vf.venue.id FROM VenueFollower vf WHERE vf.user.id = :userId")
    List<Long> findFollowedVenueIds(@Param("userId") Long userId);

    // Método para obtener nombres de artistas seguidos
    @Query("SELECT af.artist.name FROM ArtistFollower af WHERE af.user.id = :userId")
    List<String> findFollowedArtistNames(@Param("userId") Long userId);

    // Método para obtener nombres de recintos seguidos
    @Query("SELECT vf.venue.name FROM VenueFollower vf WHERE vf.user.id = :userId")
    List<String> findFollowedVenueNames(@Param("userId") Long userId);
}