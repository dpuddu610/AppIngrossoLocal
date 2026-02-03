package com.ingrosso.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.ingrosso.dao.UtenteDao;
import com.ingrosso.model.Ruolo;
import com.ingrosso.model.Utente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static AuthService instance;

    private final UtenteDao utenteDao;
    private Utente currentUser;

    private AuthService() {
        this.utenteDao = new UtenteDao();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        Optional<Utente> utenteOpt = utenteDao.findByUsername(username);

        if (utenteOpt.isEmpty()) {
            logger.warn("Login failed: user not found - {}", username);
            return false;
        }

        Utente utente = utenteOpt.get();

        if (!utente.isAttivo()) {
            logger.warn("Login failed: user disabled - {}", username);
            return false;
        }

        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), utente.getPasswordHash());

        if (!result.verified) {
            logger.warn("Login failed: invalid password - {}", username);
            return false;
        }

        currentUser = utente;
        utenteDao.updateUltimoAccesso(utente.getId());
        logger.info("User logged in: {}", username);
        return true;
    }

    public void logout() {
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getUsername());
            currentUser = null;
        }
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public Utente getCurrentUser() {
        return currentUser;
    }

    public boolean hasPermission(Ruolo requiredRole) {
        if (currentUser == null) return false;

        return switch (currentUser.getRuolo()) {
            case ADMIN -> true; // Admin has all permissions
            case OPERATORE -> requiredRole != Ruolo.ADMIN;
            case VISUALIZZATORE -> requiredRole == Ruolo.VISUALIZZATORE;
        };
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRuolo() == Ruolo.ADMIN;
    }

    public boolean canEdit() {
        return currentUser != null && (currentUser.getRuolo() == Ruolo.ADMIN ||
                currentUser.getRuolo() == Ruolo.OPERATORE);
    }

    public String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        Optional<Utente> utenteOpt = utenteDao.findById(userId);

        if (utenteOpt.isEmpty()) {
            return false;
        }

        Utente utente = utenteOpt.get();
        BCrypt.Result result = BCrypt.verifyer().verify(oldPassword.toCharArray(), utente.getPasswordHash());

        if (!result.verified) {
            return false;
        }

        String newHash = hashPassword(newPassword);
        return utenteDao.updatePassword(userId, newHash);
    }

    public boolean resetPassword(int userId, String newPassword) {
        if (!isAdmin()) {
            logger.warn("Password reset denied: not admin");
            return false;
        }

        String newHash = hashPassword(newPassword);
        return utenteDao.updatePassword(userId, newHash);
    }

    public Utente createUser(String username, String password, String nome, String cognome, Ruolo ruolo) {
        if (!isAdmin()) {
            logger.warn("User creation denied: not admin");
            return null;
        }

        if (utenteDao.findByUsername(username).isPresent()) {
            logger.warn("User creation failed: username already exists - {}", username);
            return null;
        }

        Utente utente = new Utente();
        utente.setUsername(username);
        utente.setPasswordHash(hashPassword(password));
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setRuolo(ruolo);
        utente.setAttivo(true);

        int id = utenteDao.insert(utente);
        if (id > 0) {
            utente.setId(id);
            logger.info("User created: {}", username);
            return utente;
        }

        return null;
    }
}
