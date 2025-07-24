package com.teknokote.ess.integration;

import com.teknokote.ess.core.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

@SpringBootTest
@AutoConfigureMockMvc
public class EssApplicationTests
{

   @Autowired
   protected MockMvc mvc;

   @Autowired
   private UserService userService;

   protected void connectUser(){
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userService.getDao().getRepository().findUserByUsernameIgnoreCase("ctr").get(),null, Collections.singletonList(new SimpleGrantedAuthority("admin"))));
   }

}
