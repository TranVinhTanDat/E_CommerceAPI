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

        // Kiểm tra dữ liệu đầu vào
        if (address.getAddressLine1() == null || address.getAddressLine1().isEmpty()) {
            throw new IllegalArgumentException("Address Line 1 is required");
        }
        if (address.getCity() == null || address.getCity().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (address.getPostalCode() == null || address.getPostalCode().isEmpty()) {
            throw new IllegalArgumentException("Postal Code is required");
        }
        if (address.getCountry() == null || address.getCountry().isEmpty()) {
            throw new IllegalArgumentException("Country is required");
        }
        if (address.getPhone() == null || address.getPhone().isEmpty()) {
            throw new IllegalArgumentException("Phone is required");
        }
        if (address.getEmail() == null || address.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        address.setUser(user);

        // Nếu đây là địa chỉ đầu tiên của user hoặc được đánh dấu là mặc định
        List<Address> existingAddresses = addressRepository.findByUserId(userId);
        if (existingAddresses.isEmpty() || address.isDefault()) {
            address.setDefault(true);
            // Reset các địa chỉ khác về không mặc định
            existingAddresses.forEach(addr -> {
                if (addr.isDefault()) {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                }
            });
        }

        return addressRepository.save(address);
    }

    public List<Address> getAllAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address getDefaultAddress(Long userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElse(null); // Trả về null nếu không có địa chỉ mặc định
    }

    public Address setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this address");
        }

        // Reset tất cả địa chỉ khác của user về không mặc định
        List<Address> userAddresses = addressRepository.findByUserId(userId);
        userAddresses.forEach(addr -> {
            if (addr.isDefault()) {
                addr.setDefault(false);
                addressRepository.save(addr);
            }
        });

        // Đặt địa chỉ này làm mặc định
        address.setDefault(true);
        return addressRepository.save(address);
    }

    public Address updateAddress(Long userId, Long addressId, Address addressDetails) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this address");
        }

        // Cập nhật thông tin địa chỉ
        address.setAddressLine1(addressDetails.getAddressLine1());
        address.setAddressLine2(addressDetails.getAddressLine2());
        address.setCity(addressDetails.getCity());
        address.setState(addressDetails.getState());
        address.setPostalCode(addressDetails.getPostalCode());
        address.setCountry(addressDetails.getCountry());
        address.setPhone(addressDetails.getPhone());
        address.setEmail(addressDetails.getEmail());
        address.setDefault(addressDetails.isDefault());

        // Nếu cập nhật thành địa chỉ mặc định, reset các địa chỉ khác
        if (address.isDefault()) {
            List<Address> userAddresses = addressRepository.findByUserId(userId);
            userAddresses.forEach(addr -> {
                if (!addr.getId().equals(addressId) && addr.isDefault()) {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                }
            });
        }

        return addressRepository.save(address);
    }

    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
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