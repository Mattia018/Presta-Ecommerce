package com.prestashop.web.services;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.prestashop.web.domain.Cart;
import com.prestashop.web.domain.User;

import com.prestashop.web.repository.CartItemRepository;
import com.prestashop.web.repository.CartRepository;
import com.prestashop.web.repository.UserRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private Keycloak keycloak; 
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utente con ID " + id + " non trovato"));
    }
    
    @Transactional
    public User createUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email dell'utente non può essere vuota");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EntityExistsException("Un utente con questa email esiste già: " + user.getEmail());
        }
                
        Cart cart = user.getCart();
        if (cart != null) {
            cart.setUser(user); 
        }
        return userRepository.save(user);
    }
    
    
    @Transactional
    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        // Aggiorna solo i campi forniti
        if (userDetails.getName() != null && !userDetails.getName().isEmpty()) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getSurname() != null && !userDetails.getSurname().isEmpty()) {
            user.setSurname(userDetails.getSurname());
        }
        if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty()) {
            if (!user.getEmail().equals(userDetails.getEmail()) && userRepository.existsByEmail(userDetails.getEmail())) {
                throw new EntityExistsException("Un altro utente ha già questa email: " + userDetails.getEmail());
            }
            user.setEmail(userDetails.getEmail());
        }

        // Aggiorna l'utente in Keycloak 
        RealmResource realmResource = keycloak.realm("myrealm");
        UsersResource usersResource = realmResource.users();
        List<UserRepresentation> users = usersResource.search(user.getEmail());
        if (!users.isEmpty()) {
            UserRepresentation keycloakUser = users.get(0);
            keycloakUser.setEmail(user.getEmail());
            keycloakUser.setFirstName(user.getName());
            keycloakUser.setLastName(user.getSurname());
            usersResource.get(keycloakUser.getId()).update(keycloakUser);
        }

        return userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + userId));

        // Elimina l'utente da Keycloak
        RealmResource realmResource = keycloak.realm("myrealm");
        UsersResource usersResource = realmResource.users();
        List<UserRepresentation> users = usersResource.search(user.getEmail());
        if (!users.isEmpty()) {
            usersResource.delete(users.get(0).getId());
        }

        // Elimina il carrello associato all'utente
        Cart cart = user.getCart();
        if (cart != null) {
            cartItemRepository.deleteAllByCart(cart);
            cartRepository.delete(cart);
        }

        // Elimina l'utente dal database
        userRepository.delete(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Ottiene l'utente tramite email.
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con email: " + email));
    }
    
    /**
     * Ottiene l'ID dell'utente tramite email.
     */
    public Long getUserIdByEmail(String email) {
        User user = getUserByEmail(email);
        return user.getSequId(); 
    }
    
    // Ottiene il count degli User
    public long countUsers() {
        return userRepository.count();
    }
    
    
    
}
