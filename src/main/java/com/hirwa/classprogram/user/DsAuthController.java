package com.hirwa.classprogram.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;

@Controller
public class DsAuthController {

    private static final String DS_PASSWORD = "Kai@123";

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/ds/login")
    public String dsLoginPage() {
        return "ds/login";
    }

    @PostMapping("/ds/authenticate")
    public String authenticateDs(@RequestParam String password, HttpServletRequest request) {
        if (DS_PASSWORD.equals(password)) {
          
            User dsUser = userService.findByUsername("DS").orElse(null);
            
            if (dsUser == null) {
               
                dsUser = new User();
                dsUser.setUsername("DS");
                dsUser.setPassword(passwordEncoder.encode(DS_PASSWORD));
                dsUser.setEmail("ds@gsgihundwe.rw");
                dsUser.setRole(User.Role.DS);
                dsUser.setFirstName("Director");
                dsUser.setLastName("of Studies");
                dsUser.setEnabled(true);
                dsUser = userService.save(dsUser);
            }

            
            dsUser = userService.findByUsername("DS").orElse(dsUser);

            
            var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_DS"));
            var authentication = new UsernamePasswordAuthenticationToken(dsUser, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
           
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            return "redirect:/dashboard";
        }
        
        return "redirect:/ds/login?error";
    }
}

