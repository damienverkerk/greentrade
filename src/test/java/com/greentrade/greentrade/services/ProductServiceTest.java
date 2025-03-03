package com.greentrade.greentrade.services;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greentrade.greentrade.dto.product.ProductCreateRequest;
import com.greentrade.greentrade.dto.product.ProductResponse;
import com.greentrade.greentrade.dto.product.ProductUpdateRequest;
import com.greentrade.greentrade.exception.product.InvalidProductDataException;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.mappers.ProductMapper;
import com.greentrade.greentrade.models.Product;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.ProductRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private User testSeller;
    private Product testProduct;
    private ProductResponse testProductResponse;
    private ProductCreateRequest createRequest;
    private ProductUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Arrange - Create test seller
        testSeller = User.builder()
                .id(1L)
                .name("Test Seller")
                .email("seller@test.com")
                .build();

        // Arrange - Create test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setSustainabilityScore(85);
        testProduct.setSeller(testSeller);
        
        // Arrange - Create test response
        testProductResponse = ProductResponse.builder()
            .id(1L)
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("99.99"))
            .sustainabilityScore(85)
            .sellerId(1L)
            .build();
            
        // Arrange - Create test requests
        createRequest = ProductCreateRequest.builder()
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("99.99"))
            .sustainabilityScore(85)
            .sellerId(1L)
            .build();
            
        updateRequest = ProductUpdateRequest.builder()
            .name("Updated Product")
            .description("Updated Description")
            .price(new BigDecimal("129.99"))
            .sustainabilityScore(90)
            .sellerId(1L)
            .build();
    }

    @Test
    void getProductById_ExistingProduct_ReturnsProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getPrice(), result.getPrice());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_NonExistingProduct_ThrowsException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException thrown = assertThrows(
            ProductNotFoundException.class,
            () -> productService.getProductById(999L)
        );
        assertEquals("Product not found with ID: 999", thrown.getMessage());
    }
    
    @Test
    void createProduct_ValidData_CreatesProduct() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testSeller));
        when(productMapper.createRequestToEntity(createRequest, testSeller)).thenReturn(testProduct);
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.createProduct(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testProductResponse.getName(), result.getName());
        assertEquals(testProductResponse.getPrice(), result.getPrice());
        assertEquals(testProductResponse.getSellerId(), result.getSellerId());
        verify(userRepository).findById(1L);
        verify(productRepository).save(testProduct);
    }

    @Test
    void createProduct_InvalidPrice_ThrowsException() {
        // Arrange
        createRequest.setPrice(new BigDecimal("-10.00"));

        // Act & Assert
        InvalidProductDataException thrown = assertThrows(
            InvalidProductDataException.class,
            () -> productService.createProduct(createRequest)
        );
        assertTrue(thrown.getMessage().contains("price"));
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    void createProduct_InvalidName_ThrowsException() {
        // Arrange
        createRequest.setName("");

        // Act & Assert
        InvalidProductDataException thrown = assertThrows(
            InvalidProductDataException.class,
            () -> productService.createProduct(createRequest)
        );
        assertTrue(thrown.getMessage().contains("name"));
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    void createProduct_NullName_ThrowsException() {
        // Arrange
        createRequest.setName(null);

        // Act & Assert
        InvalidProductDataException thrown = assertThrows(
            InvalidProductDataException.class,
            () -> productService.createProduct(createRequest)
        );
        assertTrue(thrown.getMessage().contains("name"));
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    void createProduct_NullPrice_ThrowsException() {
        // Arrange
        createRequest.setPrice(null);

        // Act & Assert
        InvalidProductDataException thrown = assertThrows(
            InvalidProductDataException.class,
            () -> productService.createProduct(createRequest)
        );
        assertTrue(thrown.getMessage().contains("price"));
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    void createProduct_ZeroPrice_ThrowsException() {
        // Arrange
        createRequest.setPrice(BigDecimal.ZERO);

        // Act & Assert
        InvalidProductDataException thrown = assertThrows(
            InvalidProductDataException.class,
            () -> productService.createProduct(createRequest)
        );
        assertTrue(thrown.getMessage().contains("price"));
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    void createProduct_InvalidSustainabilityScore_ThrowsException() {
        // Arrange
        createRequest.setSustainabilityScore(101); // Above maximum of 100

// Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.createProduct(createRequest)
);
assertTrue(thrown.getMessage().contains("sustainabilityScore"));
verify(productRepository, never()).save(any(Product.class));
}

@Test
void createProduct_NegativeSustainabilityScore_ThrowsException() {
// Arrange
createRequest.setSustainabilityScore(-1); // Below minimum of 0

// Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.createProduct(createRequest)
);
assertTrue(thrown.getMessage().contains("sustainabilityScore"));
verify(productRepository, never()).save(any(Product.class));
}

@Test
void createProduct_UserNotFound_ThrowsException() {
// Arrange
when(userRepository.findById(999L)).thenReturn(Optional.empty());
createRequest.setSellerId(999L);

// Act & Assert
UserNotFoundException thrown = assertThrows(
    UserNotFoundException.class,
    () -> productService.createProduct(createRequest)
);
assertEquals("User not found with ID: 999", thrown.getMessage());
verify(productRepository, never()).save(any(Product.class));
}

@Test
void getAllProducts_ReturnsAllProducts() {
// Arrange
Product product2 = new Product();
product2.setId(2L);
product2.setName("Test Product 2");
product2.setPrice(new BigDecimal("199.99"));
product2.setSeller(testSeller);

ProductResponse product2Response = ProductResponse.builder()
    .id(2L)
    .name("Test Product 2")
    .price(new BigDecimal("199.99"))
    .sellerId(1L)
    .build();

when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));
when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);
when(productMapper.toResponse(product2)).thenReturn(product2Response);

// Act
List<ProductResponse> results = productService.getAllProducts();

// Assert
assertNotNull(results);
assertEquals(2, results.size());
assertEquals(testProduct.getName(), results.get(0).getName());
assertEquals(product2.getName(), results.get(1).getName());
verify(productRepository).findAll();
}

@Test
void getAllProducts_EmptyList_ReturnsEmptyList() {
// Arrange
when(productRepository.findAll()).thenReturn(Collections.emptyList());

// Act
List<ProductResponse> results = productService.getAllProducts();

// Assert
assertNotNull(results);
assertTrue(results.isEmpty());
verify(productRepository).findAll();
}

@Test
void deleteProduct_ExistingProduct_DeletesProduct() {
// Arrange
when(productRepository.existsById(1L)).thenReturn(true);
doNothing().when(productRepository).deleteById(1L);

// Act & Assert
assertDoesNotThrow(() -> productService.deleteProduct(1L));
verify(productRepository).deleteById(1L);
}

@Test
void deleteProduct_NonExistingProduct_ThrowsException() {
// Arrange
when(productRepository.existsById(999L)).thenReturn(false);

// Act & Assert
ProductNotFoundException thrown = assertThrows(
    ProductNotFoundException.class,
    () -> productService.deleteProduct(999L)
);
assertEquals("Product not found with ID: 999", thrown.getMessage());
verify(productRepository, never()).deleteById(anyLong());
}

@Test
void updateProduct_ValidData_UpdatesProduct() {
// Arrange
when(productRepository.existsById(1L)).thenReturn(true);
when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
when(userRepository.findById(1L)).thenReturn(Optional.of(testSeller));

Product updatedProduct = new Product();
updatedProduct.setId(1L);
updatedProduct.setName("Updated Product");
updatedProduct.setDescription("Updated Description");
updatedProduct.setPrice(new BigDecimal("129.99"));
updatedProduct.setSustainabilityScore(90);
updatedProduct.setSeller(testSeller);

ProductResponse updatedResponse = ProductResponse.builder()
    .id(1L)
    .name("Updated Product")
    .description("Updated Description")
    .price(new BigDecimal("129.99"))
    .sustainabilityScore(90)
    .sellerId(1L)
    .build();

when(productRepository.save(testProduct)).thenReturn(updatedProduct);
when(productMapper.toResponse(updatedProduct)).thenReturn(updatedResponse);

// Act
ProductResponse result = productService.updateProduct(1L, updateRequest);

// Assert
assertNotNull(result);
assertEquals("Updated Product", result.getName());
assertEquals(new BigDecimal("129.99"), result.getPrice());
verify(productRepository).existsById(1L);
verify(productRepository).findById(1L);
verify(productMapper).updateEntityFromRequest(testProduct, updateRequest, testSeller);
verify(productRepository).save(testProduct);
}

@Test
void updateProduct_WithoutSeller_UpdatesProduct() {
// Arrange
when(productRepository.existsById(1L)).thenReturn(true);
when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

updateRequest.setSellerId(null);

Product updatedProduct = new Product();
updatedProduct.setId(1L);
updatedProduct.setName("Updated Product");
updatedProduct.setDescription("Updated Description");
updatedProduct.setPrice(new BigDecimal("129.99"));
updatedProduct.setSustainabilityScore(90);
updatedProduct.setSeller(testSeller); // Keep the original seller

ProductResponse updatedResponse = ProductResponse.builder()
    .id(1L)
    .name("Updated Product")
    .description("Updated Description")
    .price(new BigDecimal("129.99"))
    .sustainabilityScore(90)
    .sellerId(1L)
    .build();

when(productRepository.save(testProduct)).thenReturn(updatedProduct);
when(productMapper.toResponse(updatedProduct)).thenReturn(updatedResponse);

// Act
ProductResponse result = productService.updateProduct(1L, updateRequest);

// Assert
assertNotNull(result);
assertEquals("Updated Product", result.getName());
verify(userRepository, never()).findById(anyLong()); // Shouldn't call userRepository
verify(productMapper).updateEntityFromRequest(testProduct, updateRequest, null);
verify(productRepository).save(testProduct);
}

@Test
void updateProduct_NonExistingProduct_ThrowsException() {
// Arrange
when(productRepository.existsById(999L)).thenReturn(false);

// Act & Assert
ProductNotFoundException thrown = assertThrows(
    ProductNotFoundException.class,
    () -> productService.updateProduct(999L, updateRequest)
);
assertEquals("Product not found with ID: 999", thrown.getMessage());
verify(productRepository, never()).save(any(Product.class));
}

@Test
void updateProduct_InvalidName_ThrowsException() {
// Arrange
when(productRepository.existsById(1L)).thenReturn(true);
updateRequest.setName("");

// Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.updateProduct(1L, updateRequest)
);
assertTrue(thrown.getMessage().contains("name"));
verify(productRepository, never()).save(any(Product.class));
}

@Test
void updateProduct_NullName_ThrowsException() {
// Arrange
when(productRepository.existsById(1L)).thenReturn(true);
updateRequest.setName(null);

// Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.updateProduct(1L, updateRequest)
);
assertTrue(thrown.getMessage().contains("name"));
verify(productRepository, never()).save(any(Product.class));
}

@Test
void updateProduct_InvalidPrice_ThrowsException() {
// Arrange
when(productRepository.existsById(1L)).thenReturn(true);
updateRequest.setPrice(new BigDecimal("0"));

// Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.updateProduct(1L, updateRequest)
);
assertTrue(thrown.getMessage().contains("price"));
verify(productRepository, never()).save(any(Product.class));
}

@Test
void updateProduct_NegativePrice_ThrowsException() {
// Arrange
when(productRepository.existsById(1L)).thenReturn(true);
updateRequest.setPrice(new BigDecimal("-1.00"));

// Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.updateProduct(1L, updateRequest)
);
assertTrue(thrown.getMessage().contains("price"));
verify(productRepository, never()).save(any(Product.class));
}

@Test
void updateProduct_InvalidSustainabilityScore_ThrowsException() {
// Arrange
when(productRepository.existsById(1L)).thenReturn(true);
updateRequest.setSustainabilityScore(101); // Above maximum

// Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.updateProduct(1L, updateRequest)
);
assertTrue(thrown.getMessage().contains("sustainabilityScore"));
verify(productRepository, never()).save(any(Product.class));
}

@Test
void updateProduct_NegativeSustainabilityScore_ThrowsException() {
// Arrange
when(productRepository.existsById(1L)).thenReturn(true);
updateRequest.setSustainabilityScore(-1); // Below minimum

// Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.updateProduct(1L, updateRequest)
);
assertTrue(thrown.getMessage().contains("sustainabilityScore"));
verify(productRepository, never()).save(any(Product.class));
}

@Test
void searchProductsByName_ValidName_ReturnsProducts() {
// Arrange
when(productRepository.findByNameContainingIgnoreCase("Test"))
    .thenReturn(Arrays.asList(testProduct));
when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);

// Act
List<ProductResponse> results = productService.searchProductsByName("Test");

// Assert
assertNotNull(results);
assertEquals(1, results.size());
assertEquals(testProduct.getName(), results.get(0).getName());
verify(productRepository).findByNameContainingIgnoreCase("Test");
}

@Test
void searchProductsByName_NoMatches_ReturnsEmptyList() {
// Arrange
when(productRepository.findByNameContainingIgnoreCase("Nonexistent"))
    .thenReturn(Collections.emptyList());

// Act
List<ProductResponse> results = productService.searchProductsByName("Nonexistent");

// Assert
assertNotNull(results);
assertTrue(results.isEmpty());
verify(productRepository).findByNameContainingIgnoreCase("Nonexistent");
}

@Test
void searchProductsByName_EmptyName_ThrowsException() {
// Arrange & Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.searchProductsByName("")
);
assertTrue(thrown.getMessage().contains("name"));
verify(productRepository, never()).findByNameContainingIgnoreCase(anyString());
}

@Test
void searchProductsByName_NullName_ThrowsException() {
// Arrange & Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.searchProductsByName(null)
);
assertTrue(thrown.getMessage().contains("name"));
verify(productRepository, never()).findByNameContainingIgnoreCase(anyString());
}

@Test
void getProductsBySustainabilityScore_ReturnsFilteredProducts() {
// Arrange
when(productRepository.findBySustainabilityScoreGreaterThanEqual(80))
    .thenReturn(Arrays.asList(testProduct));
when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);

// Act
List<ProductResponse> results = productService.getProductsBySustainabilityScore(80);

// Assert
assertNotNull(results);
assertEquals(1, results.size());
assertEquals(85, results.get(0).getSustainabilityScore());
verify(productRepository).findBySustainabilityScoreGreaterThanEqual(80);
}

@Test
void getProductsBySustainabilityScore_NoMatches_ReturnsEmptyList() {
// Arrange
when(productRepository.findBySustainabilityScoreGreaterThanEqual(90))
    .thenReturn(Collections.emptyList());

// Act
List<ProductResponse> results = productService.getProductsBySustainabilityScore(90);

// Assert
assertNotNull(results);
assertTrue(results.isEmpty());
verify(productRepository).findBySustainabilityScoreGreaterThanEqual(90);
}

@Test
void getProductsBySustainabilityScore_InvalidScore_ThrowsException() {
// Arrange & Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.getProductsBySustainabilityScore(101) // Above maximum
);
assertTrue(thrown.getMessage().contains("Score must be between 0 and 100"));
verify(productRepository, never()).findBySustainabilityScoreGreaterThanEqual(anyInt());
}

@Test
void getProductsBySustainabilityScore_NegativeScore_ThrowsException() {
// Arrange & Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.getProductsBySustainabilityScore(-1) // Below minimum
);
assertTrue(thrown.getMessage().contains("Score must be between 0 and 100"));
verify(productRepository, never()).findBySustainabilityScoreGreaterThanEqual(anyInt());
}

@Test
void getProductsBySustainabilityScore_NullScore_ThrowsException() {
// Arrange & Act & Assert
InvalidProductDataException thrown = assertThrows(
    InvalidProductDataException.class,
    () -> productService.getProductsBySustainabilityScore(null)
);
assertTrue(thrown.getMessage().contains("Score must be between 0 and 100"));
verify(productRepository, never()).findBySustainabilityScoreGreaterThanEqual(anyInt());
}

@Test
void getProductsBySeller_ValidSeller_ReturnsProducts() {
// Arrange
when(userRepository.findById(1L)).thenReturn(Optional.of(testSeller));
when(productRepository.findBySeller(testSeller))
    .thenReturn(Arrays.asList(testProduct));
when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);

// Act
List<ProductResponse> results = productService.getProductsBySeller(1L);

// Assert
assertNotNull(results);
assertEquals(1, results.size());
assertEquals(testProduct.getName(), results.get(0).getName());
verify(userRepository).findById(1L);
verify(productRepository).findBySeller(testSeller);
}

@Test
void getProductsBySeller_NoProducts_ReturnsEmptyList() {
// Arrange
when(userRepository.findById(1L)).thenReturn(Optional.of(testSeller));
when(productRepository.findBySeller(testSeller))
    .thenReturn(Collections.emptyList());

// Act
List<ProductResponse> results = productService.getProductsBySeller(1L);

// Assert
assertNotNull(results);
assertTrue(results.isEmpty());
verify(userRepository).findById(1L);
verify(productRepository).findBySeller(testSeller);
}

@Test
void getProductsBySeller_NonExistingSeller_ThrowsException() {
// Arrange
when(userRepository.findById(999L)).thenReturn(Optional.empty());

// Act & Assert
UserNotFoundException thrown = assertThrows(
    UserNotFoundException.class,
    () -> productService.getProductsBySeller(999L)
);
assertEquals("User not found with ID: 999", thrown.getMessage());
}
}