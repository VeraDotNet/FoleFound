package com.veradotnet.folefound.users.domain.service;

import com.veradotnet.folefound.users.domain.model.UserPrincipal;
import com.veradotnet.folefound.users.domain.model.Users;
import com.veradotnet.folefound.users.domain.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepo repo;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {

        Users user = repo.findByUsername(username);

        if(user == null) {
            throw new UsernameNotFoundException("user not found");
        }

        //create a class that implements userDetails(UserPrincipal)
        return new UserPrincipal(user);
    }
}
