// MockMvc is a Spring testing tool to simulate HTTP requests and test controllers without running the full application or hitting the database.
// Instead of actually connecting to a database, it uses mock objects or fake data to simulate the database's behavior.

package org.yearup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriesController.class) // Limits the test to only the web for CategoriesController
public class CategoriesControllerTest {
    @Autowired
    private MockMvc mockMvc; // Allows us to simulate HTTP requests and test responses

    @MockBean
    private CategoryDao categoryDao; // Mocked DAO used instead of the real database

    @MockBean
    private ProductDao productDao; // Required since the controller depends on it

    // Mock beans for security components, so I don't need to set up the entire security context
    @MockBean
    private org.yearup.security.jwt.TokenProvider tokenProvider;

    @MockBean
    private org.yearup.security.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private org.yearup.security.JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockBean
    private org.yearup.security.UserModelDetailsService userModelDetailsService;


    @Autowired
    private ObjectMapper objectMapper; // Used to serialize/deserialize JSON objects

    private Category sampleCategory;

    @BeforeEach
    public void setUp() {
        sampleCategory = new Category(1, "Electronics", "Gadgets and more");
    }

    @Test
    public void testGetAllCategories() throws Exception {
        // Arrange: Set up the mock return value for getAllCategories
        Mockito.when(categoryDao.getAllCategories()).thenReturn(Arrays.asList(sampleCategory));

        // Act & Assert: Perform GET request and verify response contains expected category
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].name").value("Electronics"));
    }

    @Test
    public void testGetCategoryById() throws Exception {
        // Arrange: Set up mock return value for getById
        Mockito.when(categoryDao.getById(1)).thenReturn(sampleCategory);

        // Act & Assert: Perform GET request and verify the category details
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".description").value("Gadgets and more"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}) // Simulates an admin user
    public void testAddCategory() throws Exception {
        // Arrange: Mock the DAO to return the sample category on creation
        Mockito.when(categoryDao.create(any(Category.class))).thenReturn(sampleCategory);

        // Act & Assert: Perform POST request and check the response body
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".name").value("Electronics"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUpdateCategory() throws Exception {
        // Act: Perform PUT request to update the category
        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCategory)))
                .andExpect(status().isOk());

        // Assert: Verify the update method was called on the DAO
        Mockito.verify(categoryDao).update(eq(1), any(Category.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteCategory() throws Exception {
        // Act: Perform DELETE request to remove the category
        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isOk());

        // Assert: Verify the delete method was called on the DAO
        Mockito.verify(categoryDao).delete(1);
    }
}

