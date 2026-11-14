package shopping;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
        @RequestBody ProductCreateRequest request) {
            Product product = productService.create(request.name(), request.price(), request.imageUrl());
            URI location = URI.create("/api/products/" + product.getId());
            return ResponseEntity.created(location).body(ProductResponse.from(product));
        }
}
