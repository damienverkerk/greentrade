package com.greentrade.greentrade.services;

import java.math.BigDecimal;
import java.util.Arrays;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greentrade.greentrade.dto.ProductDTO;
import com.greentrade.greentrade.exception.product.InvalidProductDataException;
import com.greentrade.greentrade.exception.product.ProductNotFoundException;
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

    @InjectMocks
    private ProductService productService;

    private User testVerkoper;
    private Product testProduct;
    private ProductDTO testProductDTO;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testVerkoper = User.builder()
                .id(1L)
                .naam("Test Verkoper")
                .email("verkoper@test.com")
                .build();

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setNaam("Test Product");
        testProduct.setBeschrijving("Test Beschrijving");
        testProduct.setPrijs(new BigDecimal("99.99"));
        testProduct.setDuurzaamheidsScore(85);
        testProduct.setVerkoper(testVerkoper);

        testProductDTO = new ProductDTO(
            1L,
            "Test Product",
            "Test Beschrijving",
            new BigDecimal("99.99"),
            85,
            null,
            1L
        );
    }

    @Test
    void getProductById_ExistingProduct_ReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getNaam(), result.getNaam());
        assertEquals(testProduct.getPrijs(), result.getPrijs());
    }

    @Test
    void getProductById_NonExistingProduct_ThrowsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ProductNotFoundException thrown = assertThrows(
            ProductNotFoundException.class,
            () -> productService.getProductById(999L)
        );
        assertEquals("Product niet gevonden met ID: 999", thrown.getMessage());
    }
    
    @Test
    void createProduct_ValidData_CreatesProduct() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testVerkoper));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductDTO result = productService.createProduct(testProductDTO);

        assertNotNull(result);
        assertEquals(testProductDTO.getNaam(), result.getNaam());
        assertEquals(testProductDTO.getPrijs(), result.getPrijs());
        assertEquals(testProductDTO.getVerkoperId(), result.getVerkoperId());
    }

    @Test
    void createProduct_InvalidPrice_ThrowsException() {
        testProductDTO.setPrijs(new BigDecimal("-10.00"));

        InvalidProductDataException thrown = assertThrows(
            InvalidProductDataException.class,
            () -> productService.createProduct(testProductDTO)
        );
        assertTrue(thrown.getMessage().contains("prijs"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllProducts_ReturnsAllProducts() {
        Product product2 = new Product();
        product2.setId(2L);
        product2.setNaam("Test Product 2");
        product2.setPrijs(new BigDecimal("199.99"));
        product2.setVerkoper(testVerkoper);

        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));

        List<ProductDTO> results = productService.getAllProducts();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(testProduct.getNaam(), results.get(0).getNaam());
        assertEquals(product2.getNaam(), results.get(1).getNaam());
    }

    @Test
    void deleteProduct_ExistingProduct_DeletesProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_NonExistingProduct_ThrowsException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        ProductNotFoundException thrown = assertThrows(
            ProductNotFoundException.class,
            () -> productService.deleteProduct(999L)
        );
        assertEquals("Product niet gevonden met ID: 999", thrown.getMessage());
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateProduct_ValidData_UpdatesProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testVerkoper));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        testProductDTO.setNaam("Updated Name");
        ProductDTO result = productService.updateProduct(1L, testProductDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getNaam());
        assertEquals(testProductDTO.getPrijs(), result.getPrijs());
    }

    @Test
    void updateProduct_NonExistingProduct_ThrowsException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        ProductNotFoundException thrown = assertThrows(
            ProductNotFoundException.class,
            () -> productService.updateProduct(999L, testProductDTO)
        );
        assertEquals("Product niet gevonden met ID: 999", thrown.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void searchProductsByName_ValidName_ReturnsProducts() {
        when(productRepository.findByNaamContainingIgnoreCase("Test"))
            .thenReturn(Arrays.asList(testProduct));

        List<ProductDTO> results = productService.searchProductsByName("Test");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testProduct.getNaam(), results.get(0).getNaam());
    }

    @Test
    void searchProductsByName_EmptyName_ThrowsException() {
        InvalidProductDataException thrown = assertThrows(
            InvalidProductDataException.class,
            () -> productService.searchProductsByName("")
        );
        assertTrue(thrown.getMessage().contains("naam"));
        verify(productRepository, never()).findByNaamContainingIgnoreCase(anyString());
    }
}