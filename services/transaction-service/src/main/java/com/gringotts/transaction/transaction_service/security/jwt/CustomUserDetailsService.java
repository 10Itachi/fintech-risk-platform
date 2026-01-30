package com.gringotts.transaction.transaction_service.security.jwt;

import com.gringotts.transaction.transaction_service.security.iam.User;
import com.gringotts.transaction.transaction_service.security.iam.IsActive;
import com.gringotts.transaction.transaction_service.api.exception.UserNotFound;
import com.gringotts.transaction.transaction_service.security.iam.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(()->new UserNotFound("User not found with phone number " + phoneNumber));
        if(user.getIsActive()!= IsActive.ACTIVE) {
            throw new UsernameNotFoundException("User is not Active");
        }
        return new CustomerUserDetails(user);
    }
}
