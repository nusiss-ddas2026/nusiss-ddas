package nusiss.ddas.app.service;

import nusiss.ddas.app.model.User;
import nusiss.ddas.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        User userEntity = user.get();
        return new org.springframework.security.core.userdetails.User(
            userEntity.getUsername(),
            userEntity.getPasswordHash(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name()))
        );
    }
}
