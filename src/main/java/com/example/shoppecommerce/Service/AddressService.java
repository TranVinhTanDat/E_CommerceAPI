package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.Address;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Repository.AddressRepository;
import com.example.shoppecommerce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    public Address addAddress(Long userId, Address address) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        address.setUser(user);
        return addressRepository.save(address);
    }

    public List<Address> getAllAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address updateAddress(Long userId, Long addressId, Address addressDetails) {
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new RuntimeException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this address");
        }
        address.setAddressLine1(addressDetails.getAddressLine1());
        address.setAddressLine2(addressDetails.getAddressLine2());
        address.setCity(addressDetails.getCity());
        address.setState(addressDetails.getState());
        address.setPostalCode(addressDetails.getPostalCode());
        address.setCountry(addressDetails.getCountry());
        address.setPhone(addressDetails.getPhone());
        return addressRepository.save(address);
    }

    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new RuntimeException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this address");
        }
        addressRepository.delete(address);
    }

    public Long findUserIdByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return userOptional.get().getId();
        } else {
            throw new RuntimeException("User not found with username: " + username);
        }
    }
}
