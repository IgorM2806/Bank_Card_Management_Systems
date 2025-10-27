    package org.example.repository;

    import org.example.entity.Cards;
    import org.example.entity.User;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Modifying;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;

    import java.util.List;

    public interface CardRepository extends JpaRepository<Cards, Long> {

        List<Cards> findAllByOwner(User owner);

        @Modifying
        @Query(value ="""
                   UPDATE cards SET request_blocking = :blocking 
                   WHERE id = :cardId AND owner_id = :userId""", nativeQuery = true)
        int updateRequestBlocking(
                @Param("blocking") String blocking,
                @Param("cardId") Long cardId,
                @Param("userId") Long userId
        );


        boolean existsByCardNumber(String cardNumber);

    }
