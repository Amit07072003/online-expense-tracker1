package com.expense.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expense.model.PasswordResetToken;
import com.expense.model.User;
import com.expense.repository.PasswordResetTokenRepository;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    public void createOrUpdatePasswordResetToken(User user, String token) {
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        PasswordResetToken existingToken = tokenRepository.findByUser(user).orElse(null);
        
        if (existingToken != null) {
            existingToken.setToken(token);
            existingToken.setExpiryDate(expiryDate);
            tokenRepository.save(existingToken);
        } else {
            PasswordResetToken newToken = new PasswordResetToken(token, user, expiryDate);
            tokenRepository.save(newToken);
        }
    }
}
