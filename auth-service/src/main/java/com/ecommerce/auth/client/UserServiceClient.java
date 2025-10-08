package com.ecommerce.auth.client;

import com.ecommerce.auth.model.UserDTO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "USER-SERVICE", path = "/users")
public interface UserServiceClient {

    @GetMapping("/email/{email}")
    UserDTO getUserByEmail(@PathVariable("email") String email);

    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    @GetMapping("/exists")
    Map<String, Boolean> checkEmailExists(@RequestParam("email") String email);

    @PostMapping
    UserDTO createUser(@RequestBody Map<String, String> userRequest);

    @GetMapping("/internal/email/{email}")
    Map<String, String> getUserPasswordForAuth(
            @PathVariable("email") String email,
            @RequestHeader("X-Internal-Auth") String authHeader
    );
}