package com.veradotnet.folefound.shared.exception;

public class UserNotFoundException extends ResourceNotFoundException{
    public UserNotFoundException(String username) {
        super("User '" + username + "' not found.");
    }
}
