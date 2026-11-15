package shopping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.endsWith;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import java.util.List;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(ProductService.class)
class ProductServiceTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createSaveProduct() {
        Product saved = productService.create("키보드", 50000, "https://example.com/keyboard.jpg");

        entityManager.flush();
        entityManager.clear();

        Product found = productRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("키보드");
        assertThat(found.getPrice()).isEqualTo(50000);
        assertThat(found.getImageUrl()).isEqualTo("https://example.com/keyboard.jpg");
    }

    @Test
    void findByIdReturnSavedPruduct() {
        Product saved = productService.create("키보드", 50000, "https://example.com/keyboard.jpg");
        entityManager.flush();
        entityManager.clear();

        Product found = productService.findById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("키보드");
        assertThat(found.getPrice()).isEqualTo(50000);
        assertThat(found.getImageUrl()).isEqualTo("https://example.com/keyboard.jpg");
    }

    @Test
    void findByIdThrowWhenProductDoesNotExist() {
        assertThatThrownBy(() -> productService.findById(-1L)).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void findAllReturnsSavedProducts() {
        Product first = productService.create("키보드", 50000, "https://example.com/keyboard.jpg");
        Product second = productService.create("마우스", 40000, "https://example.com/mouse.jpg");

        entityManager.flush();
        entityManager.clear();

       List<Product> products = productService.findAll();

       assertThat(products).extracting(Product::getId).containsExactlyInAnyOrder(first.getId(),second.getId());
    }

    @Test
    void findAllReturnsEmptyListWhenNoProductsExist() {
        assertThat(productService.findAll()).isEmpty();
    }
}
