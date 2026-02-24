package com.localmart.shop_service.service;

import com.localmart.shop_service.dto.AddProductRequest;
import com.localmart.shop_service.dto.CreateShopRequest;
import com.localmart.shop_service.dto.ProductResponse;
import com.localmart.shop_service.dto.ShopResponse;
import com.localmart.shop_service.exception.DuplicateResourceException;
import com.localmart.shop_service.exception.ResourceNotFoundException;
import com.localmart.shop_service.model.Product;
import com.localmart.shop_service.model.Shop;
import com.localmart.shop_service.repository.ProductRepository;
import com.localmart.shop_service.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    // ─── SHOP OPERATIONS ────────────────────────────────────────────────────

    public ShopResponse createShop(CreateShopRequest request) {
        log.info("Creating shop: {}", request.getName());

        // Service-level duplicate check (Option A) — runs before DB save.
        // Checks all 4 fields together: a shop is a duplicate only when
        // name + ownerName + phone + location are ALL identical.
        if (shopRepository.existsByNameAndOwnerNameAndPhoneAndLocation(
                request.getName(), request.getOwnerName(),
                request.getPhone(), request.getLocation())) {
            throw new DuplicateResourceException(
                    "A shop with the same name, owner, phone and location already exists.");
        }

        Shop shop = Shop.builder()
                .name(request.getName())
                .ownerName(request.getOwnerName())
                .location(request.getLocation())
                .phone(request.getPhone())
                .build();

        Shop saved = shopRepository.save(shop);
        log.info("Shop created with id: {}", saved.getId());
        return toShopResponse(saved);
    }

    public List<ShopResponse> getAllShops() {
        return shopRepository.findByActiveTrue()
                .stream()
                .map(shop -> toShopResponse(shop))
                .toList();
    }

    public ShopResponse getShopById(String id) {
        Shop shop = findActiveShop(id);
        return toShopResponse(shop);
    }

    public void deleteShop(String id) {
        Shop shop = findActiveShop(id);
        shop.setActive(false);
        shopRepository.save(shop);
        log.info("Shop soft-deleted: {}", id);
    }

    // ─── PRODUCT OPERATIONS ─────────────────────────────────────────────────

    public ProductResponse addProduct(String shopId, AddProductRequest request) {
        findActiveShop(shopId);  // verify shop exists

        // Service-level duplicate check (Option A).
        // A shop cannot have two products with the same name.
        if (productRepository.existsByShopIdAndName(shopId, request.getName())) {
            throw new DuplicateResourceException(
                    "Product '" + request.getName() + "' already exists in this shop.");
        }

        log.info("Adding product '{}' to shop: {}", request.getName(), shopId);

        Product product = Product.builder()
                .shopId(shopId)
                .name(request.getName())
                .price(request.getPrice())
                .unit(request.getUnit())
                .stockQuantity(request.getStockQuantity())
                .build();

        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    public List<ProductResponse> getProductsByShop(String shopId) {
        findActiveShop(shopId);  // verify shop exists
        return productRepository.findByShopId(shopId)
                .stream()
                .map(product -> toProductResponse(product))
                .toList();
    }

    public ProductResponse getProductById(String shopId, String productId) {
        // Used by order-service via Feign to validate product exists and get price snapshot.
        findActiveShop(shopId);
        Product product = productRepository
                .findByShopIdAndId(shopId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        return toProductResponse(product);
    }

    public ProductResponse updateStock(String shopId, String productId, int newQuantity) {
        Product product = productRepository
                .findByShopIdAndId(shopId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        log.info("Updating stock for product {}: {} → {}", productId, product.getStockQuantity(), newQuantity);
        product.setStockQuantity(newQuantity);
        return toProductResponse(productRepository.save(product));
    }

    public void deleteProduct(String shopId, String productId) {
        Product product = productRepository
                .findByShopIdAndId(shopId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        // Hard delete — removes the document from MongoDB entirely.
        // This allows the same product name to be re-added to the shop later.
        productRepository.delete(product);
        log.info("Product hard-deleted: {} from shop: {}", productId, shopId);
    }

    // ─── PRIVATE HELPERS ────────────────────────────────────────────────────

    /**
     * Internal helper — returns the raw Shop model (not the DTO).
     * Used inside this class only: other methods need the model to call setActive(), save(), etc.
     * Public methods above return ShopResponse (the DTO) to callers.
     */
    private Shop findActiveShop(String id) {
        return shopRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", id));
    }

    /**
     * Maps Shop model → ShopResponse DTO.
     * 'active' field is intentionally excluded.
     */
    private ShopResponse toShopResponse(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .ownerName(shop.getOwnerName())
                .location(shop.getLocation())
                .phone(shop.getPhone())
                .createdAt(shop.getCreatedAt())
                .build();
    }

    /**
     * Maps Product model → ProductResponse DTO.
     * 'active' field is intentionally excluded.
     */
    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .shopId(product.getShopId())
                .name(product.getName())
                .price(product.getPrice())
                .unit(product.getUnit())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
