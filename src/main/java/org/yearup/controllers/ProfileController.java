package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;

import java.security.Principal;

@RestController
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()") // Only authenticated users can access these endpoints
public class ProfileController
{
    private final ProfileDao profileDao;

    private final UserDao userDao;

    // Constructor injection of ProfileDao
    public ProfileController(ProfileDao profileDao, UserDao userDao)
    {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    // GET /profile
    // Returns the profile of the currently authenticated user
    @GetMapping
    public Profile getProfile(Principal principal)
    {
        int userId = userDao.getIdByUsername(principal.getName());
       Profile profile = profileDao.getByUserId(userId);
        // If the profile is not found, you can throw an exception or return null
        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for user ID: " + userId);
        }
        return profile;
    }

    // PUT /profile
    // Updates the profile for the currently authenticated user
    @PutMapping
    public void updateProfile(@RequestBody Profile profile, Principal principal)
    {
        int userId = userDao.getIdByUsername(principal.getName());
        profile.setUserId(userId); // Make sure the user can't update someone else's profile
        profileDao.update(profile);
    }
}
