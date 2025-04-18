package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.DTO.AddressDTO;
import com.example.shoppecommerce.Entity.Address;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Repository.AddressRepository;
import com.example.shoppecommerce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public List<AddressDTO> getAllAddressesByUserId(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream().map(address -> new AddressDTO(
                address.getId(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getPhone(),
                address.getEmail()
        )).collect(Collectors.toList());
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
