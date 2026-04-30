package com.example.quantivo.service;

import com.example.quantivo.entity.PasswordResetToken;
import com.example.quantivo.entity.Usuario;
import com.example.quantivo.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Calendar;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetToken createToken(Usuario user) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        PasswordResetToken myToken = new PasswordResetToken(token, user);
        return tokenRepository.save(myToken);
    }

    public PasswordResetToken validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = tokenRepository.findByToken(token);

        return !isTokenFound(passToken) ? null
                : isTokenExpired(passToken) ? null
                : passToken;
    }

    public void deleteToken(String token) {
        tokenRepository.deleteByToken(token);
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }
}
