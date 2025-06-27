package org.yearup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.QuantityUpdateDto;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShoppingCartController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = org.yearup.security.WebSecurityConfig.class
        )
)
@AutoConfigureMockMvc(addFilters = false)

public class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc simulates HTTP requests for unit testing the controller

    @MockBean
    private ShoppingCartDao shoppingCartDao; // Mocked DAO dependency for cart operations

    @MockBean
    private UserDao userDao; // Mocked DAO dependency for user retrieval

    @MockBean
    private ProductDao productDao; // Mocked DAO dependency for product operations

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
    @WithMockUser(username = "testUser")
    public void testGetCart_ReturnsShoppingCartForUser() throws Exception {
        ShoppingCart mockCart = new ShoppingCart();

        Mockito.when(userDao.getByUserName("testUser")).thenReturn(mockUser);
        Mockito.when(shoppingCartDao.getByUserId(1)).thenReturn(mockCart);

        Principal mockPrincipal = () -> "testUser";

        mockMvc.perform(get("/cart")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testPostCart_AddsProductToCart() throws Exception {
        // Arrange
        Mockito.when(userDao.getByUserName("testUser")).thenReturn(mockUser);
        Mockito.when(shoppingCartDao.existsInCart(1, 15)).thenReturn(false);
        Mockito.when(shoppingCartDao.getByUserId(1)).thenReturn(new ShoppingCart());

        Principal mockPrincipal = () -> "testUser";

        // Act & Assert
        mockMvc.perform(post("/cart/products/15")
                        .principal(mockPrincipal))
                .andExpect(status().isOk());

        Mockito.verify(shoppingCartDao).addProduct(1, 15);
    }


    @Test
    @WithMockUser(username = "testUser")
    public void testDeleteCart_ClearsUserCart() throws Exception {
        // Arrange
        Mockito.when(userDao.getByUserName("testUser")).thenReturn(mockUser);
        Mockito.doNothing().when(shoppingCartDao).clearCart(1);
        Mockito.when(shoppingCartDao.getByUserId(1)).thenReturn(new ShoppingCart());

        // Create a fake Principal that returns "testUser"
        Principal mockPrincipal = () -> "testUser";

        // Act & Assert
        mockMvc.perform(delete("/cart").principal(mockPrincipal))
                .andExpect(status().isOk());

        Mockito.verify(shoppingCartDao).clearCart(1);
    }


    @Test
    @WithMockUser(username = "testUser")
    public void testPutCart_UpdatesQuantityOfCartItem() throws Exception {
        // Arrange
        Mockito.when(userDao.getByUserName("testUser")).thenReturn(mockUser);
        Mockito.when(shoppingCartDao.existsInCart(1, 15)).thenReturn(true);
        Mockito.doNothing().when(shoppingCartDao).updateQuantity(1, 15, 3);

        QuantityUpdateDto updateDto = new QuantityUpdateDto();
        updateDto.setQuantity(3);

        Principal mockPrincipal = () -> "testUser";

        // Act & Assert
        mockMvc.perform(put("/cart/products/15")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        Mockito.verify(shoppingCartDao).updateQuantity(1, 15, 3);
    }

}
