package com.prestashop.web.controller;







import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prestashop.web.domain.User;

import com.prestashop.web.services.UserService;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private UserService userService;
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    
    private final RestTemplate restTemplate;
    
    public AuthController(RestTemplate restTemplate, UserService userService) {
        this.restTemplate = restTemplate;
        this.userService = userService;
    }
    

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody Map<String, String> request) {
    	 	String username = request.get("username");
    	    String password = request.get("password");
    	    String email = request.get("email");
    	    String name = request.get("name");
    	    String surname = request.get("surname");
    	    
    	    // Verifica se l'utente esiste già in Keycloak
    	    RealmResource realmResource = keycloak.realm("myrealm");        
    	    UsersResource usersResource = realmResource.users();
    	    
    	    // Crea l'utente in Keycloak
    	    UserRepresentation keycloakUser = new UserRepresentation();
    	    keycloakUser.setUsername(username);
    	    keycloakUser.setEmail(email);
    	    keycloakUser.setEnabled(true);
    	    keycloakUser.setEmailVerified(true);
    	    
    	    CredentialRepresentation credential = new CredentialRepresentation();
    	    credential.setType(CredentialRepresentation.PASSWORD);
    	    credential.setValue(password);
    	    credential.setTemporary(false);
    	    keycloakUser.setCredentials(Collections.singletonList(credential));
    	    
    	    
    	    Response response = null;
    	    String userId = null;
    	    
    	    try {
    	        response = usersResource.create(keycloakUser);
    	        
    	        if (response.getStatus() != 201) {
    	            // Log dell'errore
    	            String errorMessage = response.readEntity(String.class);
    	            System.err.println("Errore durante la creazione dell'utente in Keycloak. Status: " + response.getStatus());
    	            System.err.println("Messaggio di errore: " + errorMessage);
    	            throw new RuntimeException("Errore durante la creazione dell'utente in Keycloak: " + errorMessage);
    	        }
    	        
    	        // ID utente Keycloak 
    	        String location = response.getHeaderString("Location");
    	        if (location != null) {
    	            userId = location.substring(location.lastIndexOf("/") + 1);
    	        }
    	        
    	        
    	        // Ruolo USER all'utente
    	        if (userId != null) {
    	            RoleRepresentation userRole = realmResource.roles().get("USER").toRepresentation();
    	            usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(userRole));
    	        }
    	        
    	        // Crea utente 
    	        try {
    	            User newUser = new User();
    	            newUser.setName(name);
    	            newUser.setSurname(surname);
    	            newUser.setEmail(email);
    	            
    	            
    	            // Salvataggio dell'utente 
    	            userService.createUser(newUser);
    	            
    	            return ResponseEntity.status(HttpStatus.CREATED).build();
    	        } catch (Exception dbException) {
    	            
    	            if (userId != null) {
    	                usersResource.delete(userId);
    	            }
    	            
    	            
    	            System.err.println("Errore durante il salvataggio dell'utente nel database: " + dbException.getMessage());
    	            throw new RuntimeException("Errore durante il salvataggio dell'utente nel database", dbException);
    	        }
    	    } catch (Exception e) {
    	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    	                
    	    } finally {
    	        if (response != null) {
    	            response.close();
    	        }
    	    }
    	}
    
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            // Costruzione URL token
            String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Body richiesta
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientId);
            formData.add("username", loginRequest.get("username"));
            formData.add("password", loginRequest.get("password"));
            formData.add("grant_type", "password");
            formData.add("client_secret", clientSecret);
            
            // Invio richiesta a Keycloak
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            ResponseEntity<String> keycloakResponse = restTemplate.postForEntity(
                    tokenUrl, 
                    request, 
                    String.class
            );
            
            if (keycloakResponse.getStatusCode() == HttpStatus.OK && keycloakResponse.getBody() != null) {
            	// Risposta in JSON 
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> tokenResponse = objectMapper.readValue(
                        keycloakResponse.getBody(), 
                        new TypeReference<Map<String, Object>>() {}
                );
                
                // Estrazione info utente dal token
                String accessToken = (String) tokenResponse.get("access_token");
                Map<String, Object> userInfo = extractUserInfo(accessToken);
                String email = (String) userInfo.get("email");
                
                // Recupero dell'utente nel sistema
                Long userId = userService.getUserIdByEmail(email);
                
                
                // Creazione risposta al frontend
                Map<String, Object> response = new HashMap<>();
                response.put("userId", userId);
                response.put("email", email);
                response.put("accessToken", accessToken);
                response.put("refreshToken", tokenResponse.get("refresh_token"));
                response.put("expiresIn", tokenResponse.get("expires_in"));
                response.put("roles", extractRoles(userInfo));
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Autenticazione fallita");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Errore durante l'autenticazione: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> refreshRequest) {
        try {
            // Costruzione URL token
            String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Body
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientId);
            formData.add("refresh_token", refreshRequest.get("refreshToken"));
            formData.add("grant_type", "refresh_token");
            formData.add("client_secret", clientSecret);
            
            // Invio richiesta a Keycloak
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            ResponseEntity<String> keycloakResponse = restTemplate.postForEntity(
                    tokenUrl, 
                    request, 
                    String.class
            );
            
            if (keycloakResponse.getStatusCode() == HttpStatus.OK && keycloakResponse.getBody() != null) {
            	// Risposta in JSON
            	ObjectMapper objectMapper = new ObjectMapper();
            	Map<String, Object> tokenResponse = objectMapper.readValue(
            	        keycloakResponse.getBody(), 
            	        new TypeReference<Map<String, Object>>() {}
            	);
                
                // Estrazione info utente dal token
                String accessToken = (String) tokenResponse.get("access_token");
                Map<String, Object> userInfo = extractUserInfo(accessToken);
                String email = (String) userInfo.get("email");
                Long userId = userService.getUserIdByEmail(email);
                
                // Risposta da inviare al frontend
                Map<String, Object> response = new HashMap<>();
                response.put("userId", userId);
                response.put("email", email);
                response.put("accessToken", accessToken);
                response.put("refreshToken", tokenResponse.get("refresh_token"));
                response.put("expiresIn", tokenResponse.get("expires_in"));
                response.put("roles", extractRoles(userInfo));
                
                
                return ResponseEntity.ok(response);
                
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Refresh token non valido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Errore durante il refresh del token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    private Map<String, Object> extractUserInfo(String accessToken) {
    	
        // Decodifica del token JWT per estrarre le informazioni dell'utente
        String[] chunks = accessToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        
        String payload = new String(decoder.decode(chunks[1]));
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella decodifica del token", e);
        }
    }
    
    private List<String> extractRoles(Map<String, Object> userInfo) {
        List<String> roles = new ArrayList<>();
        
        // Estrazione dei ruoli dal token
        try {
            Map<String, Object> realmAccess = (Map<String, Object>) userInfo.get("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                roles = (List<String>) realmAccess.get("roles");
            }
        } catch (Exception e) {
            
        }
        
        return roles;
    }

    
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, String> logoutRequest) {
    	System.out.println("logout.....");
        try {
            // Costruzione URL logout
            String logoutUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
            
            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Body
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("refresh_token", logoutRequest.get("refreshToken"));
            
            // Richiesta a Keycloak
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            ResponseEntity<String> keycloakResponse = restTemplate.postForEntity(
                    logoutUrl, 
                    request, 
                    String.class
            );
            
            Map<String, Object> response = new HashMap<>();
            if (keycloakResponse.getStatusCode() == HttpStatus.NO_CONTENT 
                    || keycloakResponse.getStatusCode() == HttpStatus.OK) {
                response.put("message", "Logout effettuato con successo");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Errore durante il logout");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Errore durante il logout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
         
    }
    
}

	
    

