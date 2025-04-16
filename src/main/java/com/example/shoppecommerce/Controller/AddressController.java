package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.Address;
import com.example.shoppecommerce.Service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping("/create")
    public ResponseEntity<Address> addAddress(@RequestBody Address address) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = addressService.findUserIdByUsername(user.getUsername());
        Address newAddress = addressService.addAddress(userId, address);
        return ResponseEntity.ok(newAddress);
    }

    @GetMapping("/view")
    public ResponseEntity<List<Address>> getAllAddressesByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = addressService.findUserIdByUsername(user.getUsername());
        List<Address> addresses = addressService.getAllAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long addressId, @RequestBody Address addressDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = addressService.findUserIdByUsername(user.getUsername());
        Address updatedAddress = addressService.updateAddress(userId, addressId, addressDetails);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long userId = addressService.findUserIdByUsername(user.getUsername());
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}
