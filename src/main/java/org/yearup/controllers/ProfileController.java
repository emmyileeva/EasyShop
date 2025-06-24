package org.yearup.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.ProfileDao;
import org.yearup.models.Profile;

import java.security.Principal;

@RestController
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()") // Only authenticated users can access these endpoints
public class ProfileController
{
    private final ProfileDao profileDao;

    // Constructor injection of ProfileDao
    public ProfileController(ProfileDao profileDao)
    {
        this.profileDao = profileDao;
    }

    // GET /profile
    // Returns the profile of the currently authenticated user
    @GetMapping
    public Profile getProfile(Principal principal)
    {
        int userId = Integer.parseInt(principal.getName());
        return profileDao.getByUserId(userId);
    }

    // PUT /profile
    // Updates the profile for the currently authenticated user
    @PutMapping
    public void updateProfile(@RequestBody Profile profile, Principal principal)
    {
        int userId = Integer.parseInt(principal.getName());
        profile.setUserId(userId); // Make sure the user can't update someone else's profile
        profileDao.update(profile);
    }
}
