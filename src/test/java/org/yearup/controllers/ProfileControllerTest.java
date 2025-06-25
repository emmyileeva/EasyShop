package org.yearup.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.test.context.support.WithMockUser;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// These tests verify the behavior of the GET and PUT endpoints for user profiles
@WithMockUser  // Simulates a logged-in user for Spring Security
public class ProfileControllerTest {

    @Mock
    private ProfileDao profileDao; // Mocked DAO to isolate the controller

    @Mock
    private UserDao userDao; // Mocked UserDao to simulate user data access

    @Mock
    private Principal mockPrincipal; // Mocked Principal to simulate the current user

    @InjectMocks
    private ProfileController controller; // Inject mocks into the controller

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes mocks before each test
    }

    @Test
    public void testGetProfile_ShouldReturnUserProfile() {
        // Arrange - Set up the mock behavior
        int userId = 1;
        String username = "gary";
        Profile mockProfile = new Profile();
        mockProfile.setUserId(userId);
        mockProfile.setFirstName("Emiliya");
        mockProfile.setLastName("Ileeva");
        when(mockPrincipal.getName()).thenReturn(username); // Simulate the principal's username
        when(userDao.getIdByUsername(username)).thenReturn(userId); // mock UserDao return
        when(profileDao.getByUserId(userId)).thenReturn(mockProfile); // mock DAO return

        // Act - Call the controller method
        Profile result = controller.getProfile(mockPrincipal);

        // Assert - Validate the result
        assertNotNull(result);
        assertEquals("Emiliya", result.getFirstName());
        assertEquals("Ileeva", result.getLastName());
        verify(profileDao, times(1)).getByUserId(userId); // ensure DAO was called
    }

    @Test
    public void testUpdateProfile_ShouldCallDaoWithCorrectValues() {
        // Arrange - Set up mock values
        int userId = 1;
        String username = "jenny";
        Profile profileToUpdate = new Profile();
        profileToUpdate.setFirstName("Jenny");
        profileToUpdate.setLastName("Lee");
        profileToUpdate.setEmail("jenny@example.com");
        profileToUpdate.setPhone("123-456-7890");

        when(mockPrincipal.getName()).thenReturn(username); // Simulate the principal's username
        when(userDao.getIdByUsername(username)).thenReturn(userId); // mock UserDao return

        // Act - Call the controller method
        controller.updateProfile(profileToUpdate, mockPrincipal);

        // Assert - Verify the update was triggered with correct user ID
        assertEquals(userId, profileToUpdate.getUserId());
        verify(profileDao, times(1)).update(profileToUpdate);
    }
}
