package com.bookstore.services.security;

import com.bookstore.entities.security.PasswordResetToken;
import com.bookstore.entities.security.User;
import com.bookstore.entities.security.UserRole;

import java.util.Set;

public interface UserServiceInterface {
    PasswordResetToken getPasswordResetToken(final String token);

    void createPasswordResetTokenForUser(final User user, final String token);

    User findByUsername(String username);
    User findByEmail(String email);

    User createUser(User user, Set<UserRole> userRoles) throws Exception;
    User saveUser(User user);
    PasswordResetToken saveToken(PasswordResetToken passwordResetToken);
    PasswordResetToken findTokenEmail(String email);
}
