package com.bookstore.repositories;

import com.bookstore.entities.security.PasswordResetToken;
import com.bookstore.entities.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.stream.Stream;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {
    PasswordResetToken findByToken(String token);
    @Query("select p from PasswordResetToken p where p.user in (select u from User u where u.email = :email)")
    PasswordResetToken findTokenByUserEmail(@Param("email") String email);
    PasswordResetToken findByUser(User user);

    Stream<PasswordResetToken> findAllByExpiryDateLessThan(Date now);

    @Modifying
    @Query("delete from PasswordResetToken p where p.expiryDate <=?1")
    void deleteAllExpiredSince(Date now);
}
