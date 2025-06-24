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
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.QuantityUpdateDto;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShoppingCartController.class) // Tests only the ShoppingCartController class (web layer)
public class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc simulates HTTP requests for unit testing the controller

    @MockBean
    private ShoppingCartDao shoppingCartDao; // Mocked DAO dependency for cart operations

    @MockBean
    private UserDao userDao; // Mocked DAO dependency for user retrieval

    @MockBean
    private ProductDao productDao; // Mocked DAO dependency for product operations

    // Mocks for security components to bypass authentication and authorization
    @MockBean
    private org.yearup.security.jwt.TokenProvider tokenProvider;

    @MockBean
    private org.yearup.security.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private org.yearup.security.JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockBean
    private org.yearup.security.UserModelDetailsService userModelDetailsService;

    @Autowired
    private ObjectMapper objectMapper; // Used to convert Java objects to JSON and vice versa

    private User mockUser;

    @BeforeEach
    public void setUp() {
        // Create a mock user object for use in all test cases
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("testUser");
    }

    @Test
    @WithMockUser(username = "testUser") // Ensures principal.getName() returns "testUser"
    public void testGetCart_ReturnsShoppingCartForUser() throws Exception {
        // Arrange: Set up mock behavior for user lookup and cart retrieval
        ShoppingCart mockCart = new ShoppingCart();
        Mockito.when(userDao.getByUserName("testUser")).thenReturn(mockUser);
        Mockito.when(shoppingCartDao.getByUserId(1)).thenReturn(mockCart);

        // Act & Assert: Perform GET request and verify response is OK and JSON is returned
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "testUser") // Ensures principal.getName() returns "testUser"
    public void testPostCart_AddsProductToCart() throws Exception {
        // Arrange: Set up mock behavior for user and cart product addition
        Mockito.when(userDao.getByUserName("testUser")).thenReturn(mockUser);
        Mockito.when(shoppingCartDao.existsInCart(1, 15)).thenReturn(false);

        // Act & Assert: Send POST request to add product 15 to cart and expect HTTP 200
        mockMvc.perform(post("/cart/products/15"))
                .andExpect(status().isOk());

        // Verify addProduct was called
        Mockito.verify(shoppingCartDao).addProduct(1, 15);
    }

    @Test
    @WithMockUser(username = "testUser") // Ensures principal.getName() returns "testUser"
    public void testDeleteCart_ClearsUserCart() throws Exception {
        // Arrange: Mock the userDao to return a user with ID 1
        Mockito.when(userDao.getByUserName("testUser")).thenReturn(mockUser);

        // Act & Assert: Perform DELETE request and expect HTTP 200
        mockMvc.perform(delete("/cart"))
                .andExpect(status().isOk());

        // Verify clearCart was called with correct user ID
        Mockito.verify(shoppingCartDao).clearCart(1);
    }

    @Test
    @WithMockUser(username = "testUser") // Ensures principal.getName() returns "testUser"
    public void testPutCart_UpdatesQuantityOfCartItem() throws Exception {
        // Arrange
        Mockito.when(userDao.getByUserName("testUser")).thenReturn(mockUser);

        // Mock that the product exists in the cart
        Mockito.when(shoppingCartDao.existsInCart(1, 15)).thenReturn(true);

        QuantityUpdateDto updateDto = new QuantityUpdateDto();
        updateDto.setQuantity(3);

        // Act & Assert
        mockMvc.perform(put("/cart/products/15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        Mockito.verify(shoppingCartDao).updateQuantity(1, 15, 3);
    }

}
