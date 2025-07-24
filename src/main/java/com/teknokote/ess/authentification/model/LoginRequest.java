package com.teknokote.ess.authentification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {

    private String username;
    private String password;
    private boolean admin;
    public static LoginRequest init(String username,String password,boolean admin){
        return new LoginRequest(username,password,admin);
    }
}
