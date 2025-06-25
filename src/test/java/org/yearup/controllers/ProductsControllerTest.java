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
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductsController.class) // Test only the ProductsController (web layer)
public class ProductsControllerTest {
    @Autowired
    private MockMvc mockMvc; // Used to simulate HTTP requests

    @MockBean
    private ProductDao productDao; // Mocked DAO dependency

    // Mock security components to avoid actual authentication/authorization
    @MockBean
    private org.yearup.security.jwt.TokenProvider tokenProvider;

    @MockBean
    private org.yearup.security.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private org.yearup.security.JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockBean
    private org.yearup.security.UserModelDetailsService userModelDetailsService;

    @Autowired
    private ObjectMapper objectMapper; // For converting Java objects to JSON and vice versa

    private Product sampleProduct1;
    private Product sampleProduct2;

    @BeforeEach
    public void setUp() {
        // Sample products for use in test cases
        sampleProduct1 = new Product(1, "Red Shirt", new BigDecimal("29.99"), 1, "A red shirt", "red", 10, false, "url1");
        sampleProduct2 = new Product(2, "Blue Shirt", new BigDecimal("49.99"), 2, "A blue shirt", "blue", 15, true, "url2");
    }

    @Test
    public void testSearchProductsWithCategoryFilter() throws Exception {
        // Arrange: Set up mock return value when searching by category
        Mockito.when(productDao.search(eq(1), any(), any(), any())).thenReturn(List.of(sampleProduct1));

        // Act & Assert: Send GET request and verify correct product is returned
        mockMvc.perform(get("/products?cat=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].name").value("Red Shirt"));
    }

    @Test
    public void testSearchProductsWithPriceRange() throws Exception {
        // Arrange: Mock search result for price range between 25 and 50
        Mockito.when(productDao.search(null, new BigDecimal("25"), new BigDecimal("50"), null))
                .thenReturn(Arrays.asList(sampleProduct1, sampleProduct2));

        // Act & Assert: Verify that two products are returned
        mockMvc.perform(get("/products?minPrice=25&maxPrice=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testSearchProductsWithColorFilter() throws Exception {
        // Arrange: Mock search result for products with color "blue"
        Mockito.when(productDao.search(null, null, null, "blue"))
                .thenReturn(List.of(sampleProduct2));

        // Act & Assert: Confirm only blue-colored products are returned
        mockMvc.perform(get("/products?color=blue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].color").value("blue"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}) // Simulate an admin user
    public void testUpdateProductFixesBug2() throws Exception {
        // Arrange: Prepare updated product details
        Product updatedProduct = new Product(1, "Updated Shirt", new BigDecimal("39.99"), 1, "Updated desc", "red", 12, false, "url1");

        // Act: Perform PUT request to update the product
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk());

        // Assert: Verify that update() method was called instead of create()
        Mockito.verify(productDao).update(eq(1), any(Product.class));
    }
}
