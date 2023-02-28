package edu.neu.info7255.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import edu.neu.info7255.model.Message;
import edu.neu.info7255.util.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;

//@Component
public class JwtFilter extends OncePerRequestFilter {
    private TokenGenerator tokenGenerator;
//    @Autowired
    public JwtFilter(TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String auth = request.getHeader("Authorization");
        if(auth == null || auth.isBlank() ){
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(objectMapper.writeValueAsString(new Message("Authorization header cannot be empty!")));
            response.flushBuffer();
            return;
        }
        String jwt = auth.substring(7);
        if(jwt == null || jwt.isBlank() ){
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(objectMapper.writeValueAsString(new Message("Authorization header cannot be empty!")));
            response.flushBuffer();
        }else{
            try {
                boolean valid = tokenGenerator.validateToken(jwt);
                if(valid){
                    filterChain.doFilter(request, response);
                }else{
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write(objectMapper.writeValueAsString(new Message("Token invalid!")));
                    response.flushBuffer();
                }
            } catch (Exception e){
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(objectMapper.writeValueAsString(new Message(e.getMessage())));
                response.flushBuffer();
            }
        }



    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "/token".equals(path);
    }
}
