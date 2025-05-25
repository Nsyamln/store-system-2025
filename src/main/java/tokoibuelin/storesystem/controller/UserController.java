package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.RegistSupplierReq;
import tokoibuelin.storesystem.model.request.UpdateProfileReq;
import tokoibuelin.storesystem.repository.UserRepository;
import tokoibuelin.storesystem.util.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.service.UserService;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/secured/user")
@CrossOrigin(origins = {"http://127.0.0.1:5500","http://127.0.0.1:3000"})
public class UserController {
    private final UserService userService;

    @Autowired
    private UserRepository userRepository;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getUserById/{userId}")
    public Optional<User> getUserById(@PathVariable String userId){
        Optional<User> user = userRepository.findById(userId);
        System.out.println("Data User : " + user);
        return userRepository.findById(userId);
    }

    @PostMapping("/register-supplier")
    public Response<Object> registerSeller(@RequestBody RegistSupplierReq req) {
        System.out.println("Received Request : "+req);
        Authentication authentication = SecurityContextHolder.getAuthentication();
        System.out.println("Authentication in controller: {}"+ authentication);
        return userService.registerSupplier(authentication, req);
    }

    @PostMapping("/register-pegawai")
    public Response<Object> registerPegawai(@RequestBody RegistSupplierReq req) {
        System.out.println("Received Request : "+req);
        Authentication authentication = SecurityContextHolder.getAuthentication();
        System.out.println("Authentication in controller: {}"+ authentication);
        return userService.registerPegawai(authentication, req);
    }

    @PostMapping("/update-profile")
    public Response<Object> updateProfile(@RequestBody UpdateProfileReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return userService.updateProfile(authentication, req);
    }
    @PostMapping("/update-supplier/{supplierId}")
    public Response<Object> updateSupplier(@RequestBody UpdateProfileReq req, @PathVariable String supplierId) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        System.out.println("Data dari permintaan -> " + req);
        return userService.updateSupplier(authentication, req,supplierId);
    }

    @DeleteMapping("/delete-user/{userId}")
    public Response<Object> deleteUser(@PathVariable String userId) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return userService.deletedUser(authentication, userId);
    }

    @GetMapping("/getUsers/{role}")
    public ResponseEntity<List<User>> getSuppliers(@PathVariable String role) {
        List<User> suppliers = userRepository.getSuppliersByRole( role);
        return ResponseEntity.ok(suppliers);
    }
}
