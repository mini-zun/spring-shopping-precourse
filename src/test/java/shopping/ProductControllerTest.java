package shopping;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ProductService productService;

        @Test
        void createReturnsCreatedProduct() throws Exception {
                Product product = savedProduct();
                given(productService.create(
                                "키보드", 50000, "https://example.com/keyboard.jpg"))
                                .willReturn(product);

                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody()))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", "/api/products/1"))
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("키보드"))
                                .andExpect(jsonPath("$.price").value(50000))
                                .andExpect(jsonPath("$.imageUrl")
                                                .value("https://example.com/keyboard.jpg"));
        }

        private Product savedProduct() {
                Product product = mock(Product.class);
                given(product.getId()).willReturn(1L);
                given(product.getName()).willReturn("키보드");
                given(product.getPrice()).willReturn(50000);
                given(product.getImageUrl()).willReturn(
                                "https://example.com/keyboard.jpg");
                return product;
        }

        private String requestBody() {
                return """
                                {
                                  "name": "키보드",
                                  "price": 50000,
                                  "imageUrl": "https://example.com/keyboard.jpg"
                                }
                                """;
        }

        @Test
        void findByIdReturnsProduct() throws Exception {
                Product product = savedProduct();
                given(productService.findById(1L)).willReturn(product);

                mockMvc.perform(get("/api/products/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("키보드"))
                                .andExpect(jsonPath("$.price").value(50000))
                                .andExpect(jsonPath("$.imageUrl")
                                                .value("https://example.com/keyboard.jpg"));
        }

        @Test
        void findByIdReturnsNotFound() throws Exception {
                given(productService.findById(-1L))
                                .willThrow(new ProductNotFoundException(-1L));

                mockMvc.perform(get("/api/products/-1"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void findAllReturnsProducts() throws Exception {
                Product product = savedProduct();
                given(productService.findAll()).willReturn(List.of(product));

                mockMvc.perform(get("/api/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].name").value("키보드"))
                                .andExpect(jsonPath("$[0].price").value(50000))
                                .andExpect(jsonPath("$[0].imageUrl")
                                                .value("https://example.com/keyboard.jpg"));

        }

        @Test
        void findAllReturnsEmptyArray() throws Exception {
                given(productService.findAll()).willReturn(List.of());

                mockMvc.perform(get("/api/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void updateReturnsUpdatedProduct() throws Exception {
                Product product = savedProduct();
                given(productService.update(
                                1L, "키보드", 50000, "https://example.com/keyboard.jpg"))
                                .willReturn(product);

                mockMvc.perform(put("/api/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("키보드"))
                                .andExpect(jsonPath("$.price").value(50000))
                                .andExpect(jsonPath("$.imageUrl")
                                                .value("https://example.com/keyboard.jpg"));
        }

        @Test
        void updateReturnsNotFound() throws Exception {
                given(productService.update(
                                -1L, "키보드", 50000, "https://example.com/keyboard.jpg"))
                                .willThrow(new ProductNotFoundException(-1L));

                mockMvc.perform(put("/api/products/-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody()))
                                .andExpect(status().isNotFound());
        }

        @Test
        void deleteReturnsNoContent() throws Exception {
                mockMvc.perform(delete("/api/products/1"))
                                .andExpect(status().isNoContent())
                                .andExpect(jsonPath("$").doesNotExist());

                verify(productService).delete(1L);
        }

        @Test
        void deleteReturnsNotFound() throws Exception {
                willThrow(new ProductNotFoundException(-1L))
                                .given(productService).delete(-1L);

                mockMvc.perform(delete("/api/products/-1"))
                                .andExpect(status().isNotFound());
        }

    @ParameterizedTest
    @MethodSource("invalidProductRequests")
    void createRejectsInvalidInput(String body) throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @ParameterizedTest
    @MethodSource("invalidProductRequests")
    void updateRejectsInvalidInput(String body) throws Exception {
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    private static Stream<String> invalidProductRequests() {
        return Stream.of(
                productJson("null", "1000", "\"https://example.com/a.jpg\""),
                productJson("\"\"", "1000", "\"https://example.com/a.jpg\""),
                productJson("\"   \"", "1000", "\"https://example.com/a.jpg\""),
                productJson("\"abcdefghijklmnop\"", "1000", "\"https://example.com/a.jpg\""),
                productJson("\"keyboard\"", "null", "\"https://example.com/a.jpg\""),
                productJson("\"keyboard\"", "-1", "\"https://example.com/a.jpg\""),
                productJson("\"keyboard\"", "1000", "null"),
                productJson("\"keyboard\"", "1000", "\"   \"")
        );
    }

    private static String productJson(String name, String price, String imageUrl) {
        return """
                {"name": %s, "price": %s, "imageUrl": %s}
                """.formatted(name, price, imageUrl);
    }
}
