package shopping;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product create(String name, Integer price, String imageUrl) {
        Product product = new Product(name, price, imageUrl);
        return productRepository.save(product);

    }

    @Transactional(readOnly = true)
    public Product findById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional
    public Product update(
            Long productId, String name, Integer price, String imageUrl) {
        Product product = findById(productId);
        product.update(name, price, imageUrl);
        return product;
    }

    @Transactional
    public void delete(Long proudctId) {
        Product product = findById(proudctId);
        productRepository.delete(product);
    }
}
