package org.yearup.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @PostMapping
    public void checkout(Principal principal) {
        try {
            // checkout logic will go here
            String userName = principal.getName();
            System.out.println("Processing checkout for: " + userName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Checkout failed.");
        }
    }
}
