package com.bookstore.services.security;

import com.bookstore.entities.content.ShoppingCart;
import com.bookstore.entities.content.UserPayment;
import com.bookstore.entities.content.UserShipping;
import com.bookstore.entities.security.PasswordResetToken;
import com.bookstore.entities.security.User;
import com.bookstore.entities.security.UserRole;
import com.bookstore.repositories.PasswordResetTokenRepository;
import com.bookstore.repositories.RoleRepository;
import com.bookstore.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Set;

@Service
@Transactional
public class UserService implements UserServiceInterface{

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Override
    public PasswordResetToken getPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        final PasswordResetToken myToken = new PasswordResetToken(token,user);
        passwordResetTokenRepository.save(myToken);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(User user, Set<UserRole> userRoles) throws Exception{
        User localUser = userRepository.findByUsername(user.getUsername());

        if (localUser != null){
            throw new Exception("user already exists. Nothing will be done");
        }else{
            for (UserRole ur : userRoles){
                roleRepository.save(ur.getRole());
            }
            user.getUserRoles().addAll(userRoles);

            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUser(user);
            user.setShoppingCart(shoppingCart);

            user.setUserShippingList(new ArrayList<UserShipping>());
            user.setUserPaymentList(new ArrayList<UserPayment>());

            localUser = userRepository.save(user);
        }
        return localUser;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public PasswordResetToken saveToken(PasswordResetToken passwordResetToken) {
        return passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public PasswordResetToken findTokenEmail(String email) {
        return passwordResetTokenRepository.findTokenByUserEmail(email);
    }

    public void deleteUser(User user){
        userRepository.delete(user);
    }

    public User getUser(Long id) {
        return userRepository.getOne(id);
    }
}
