package utb.fai.RESTAPIServer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. Získání seznamu všech uživatelů
    @GetMapping("/users")
    public ResponseEntity<List<MyUser>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // 7. Vyhledávání uživatelů
    @GetMapping("/users/search")
    public ResponseEntity<List<MyUser>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) {

        List<MyUser> allUsers = userRepository.findAll();

        List<MyUser> filtered = allUsers.stream().filter(user -> {
            if (name != null && !name.isEmpty()) {
                if (user.getName() == null || !user.getName().toLowerCase().contains(name.toLowerCase())) {
                    return false;
                }
            }
            if (email != null && !email.isEmpty()) {
                if (user.getEmail() == null || !user.getEmail().toLowerCase().contains(email.toLowerCase())) {
                    return false;
                }
            }
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                if (user.getPhoneNumber() == null || !user.getPhoneNumber().contains(phoneNumber)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    // 2. Získání jednoho uživatele podle ID
    @GetMapping("/users/{id}")
    public ResponseEntity<MyUser> getUserById(@PathVariable Long id) {
        Optional<MyUser> userOpt = userRepository.findById(id);
        return userOpt.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 3. Vytvoření nového uživatele
    @PostMapping("/users")
    public ResponseEntity<MyUser> createUser(@RequestBody MyUser user) {
        if (user == null || !user.isUserDataValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        MyUser newUser = new MyUser(user.getName(), user.getEmail(), user.getPhoneNumber());
        MyUser saved = userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 4. Aktualizace existujícího uživatele
    @PutMapping("/users/{id}")
    public ResponseEntity<MyUser> updateUser(@PathVariable Long id, @RequestBody MyUser user) {
        Optional<MyUser> existingOpt = userRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (user == null || !user.isUserDataValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        MyUser existing = existingOpt.get();
        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        existing.setPhoneNumber(user.getPhoneNumber());
        MyUser updated = userRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    // 5. Smazání jednoho uživatele
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<MyUser> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        MyUser deleted = userOpt.get();
        userRepository.deleteById(id);
        return ResponseEntity.ok(deleted);
    }

    // 6. Smazání všech uživatelů
    @DeleteMapping("/users")
    public ResponseEntity<List<MyUser>> deleteAllUsers() {
        try {
            userRepository.deleteAll();
            return ResponseEntity.ok(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
