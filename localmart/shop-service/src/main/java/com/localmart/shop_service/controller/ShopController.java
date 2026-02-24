package com.localmart.shop_service.controller;

import com.localmart.shop_service.dto.AddProductRequest;
import com.localmart.shop_service.dto.CreateShopRequest;
import com.localmart.shop_service.dto.ProductResponse;
import com.localmart.shop_service.dto.ShopResponse;
import com.localmart.shop_service.dto.UpdateStockRequest;
import com.localmart.shop_service.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Shops", description = "Shop registration and product management")
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Slf4j
public class ShopController {

    private final ShopService shopService;

    // ─── SHOP ENDPOINTS ─────────────────────────────────────────────────────

    @Operation(summary = "Register a new shop")
    @ApiResponse(responseCode = "201", description = "Shop registered successfully")
    @ApiResponse(responseCode = "400", description = "Validation failed — check errors field in response")
    @PostMapping
    public ResponseEntity<ShopResponse> createShop(@Valid @RequestBody CreateShopRequest request) {
        log.debug("POST /api/shops - name: {}", request.getName());
        ShopResponse response = shopService.createShop(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List all active shops")
    @ApiResponse(responseCode = "200", description = "List returned (empty array if none exist)")
    @GetMapping
    public ResponseEntity<List<ShopResponse>> getAllShops() {
        log.debug("GET /api/shops");
        return ResponseEntity.ok(shopService.getAllShops());
    }

    @Operation(summary = "Get a shop by ID")
    @ApiResponse(responseCode = "200", description = "Shop found")
    @ApiResponse(responseCode = "404", description = "Shop not found")
    @GetMapping("/{id}")
    public ResponseEntity<ShopResponse> getShopById(
            @Parameter(description = "MongoDB shop ID") @PathVariable String id) {
        log.debug("GET /api/shops/{}", id);
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    @Operation(summary = "Soft-delete a shop")
    @ApiResponse(responseCode = "204", description = "Shop deleted (active set to false)")
    @ApiResponse(responseCode = "404", description = "Shop not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(
            @Parameter(description = "MongoDB shop ID") @PathVariable String id) {
        log.debug("DELETE /api/shops/{}", id);
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }

    // ─── PRODUCT ENDPOINTS ──────────────────────────────────────────────────

    @Operation(summary = "Add a product to a shop")
    @ApiResponse(responseCode = "201", description = "Product added successfully")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Shop not found")
    @PostMapping("/{shopId}/products")
    public ResponseEntity<ProductResponse> addProduct(
            @Parameter(description = "MongoDB shop ID") @PathVariable String shopId,
            @Valid @RequestBody AddProductRequest request) {
        log.debug("POST /api/shops/{}/products - product: {}", shopId, request.getName());
        ProductResponse response = shopService.addProduct(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List all active products for a shop")
    @ApiResponse(responseCode = "200", description = "Products returned")
    @ApiResponse(responseCode = "404", description = "Shop not found")
    @GetMapping("/{shopId}/products")
    public ResponseEntity<List<ProductResponse>> getProductsByShop(
            @Parameter(description = "MongoDB shop ID") @PathVariable String shopId) {
        log.debug("GET /api/shops/{}/products", shopId);
        return ResponseEntity.ok(shopService.getProductsByShop(shopId));
    }

    @Operation(summary = "Get a single product by ID — used internally by order-service")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Shop or product not found")
    @GetMapping("/{shopId}/products/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "MongoDB shop ID") @PathVariable String shopId,
            @Parameter(description = "MongoDB product ID") @PathVariable String productId) {
        log.debug("GET /api/shops/{}/products/{}", shopId, productId);
        return ResponseEntity.ok(shopService.getProductById(shopId, productId));
    }

    @Operation(summary = "Update product stock quantity")
    @ApiResponse(responseCode = "200", description = "Stock updated")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Shop or product not found")
    @PatchMapping("/{shopId}/products/{productId}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @Parameter(description = "MongoDB shop ID") @PathVariable String shopId,
            @Parameter(description = "MongoDB product ID") @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest request) {
        log.debug("PATCH /api/shops/{}/products/{}/stock - quantity: {}", shopId, productId, request.getQuantity());
        ProductResponse response = shopService.updateStock(shopId, productId, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Soft-delete a product from a shop")
    @ApiResponse(responseCode = "204", description = "Product deleted")
    @ApiResponse(responseCode = "404", description = "Shop or product not found")
    @DeleteMapping("/{shopId}/products/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "MongoDB shop ID") @PathVariable String shopId,
            @Parameter(description = "MongoDB product ID") @PathVariable String productId) {
        log.debug("DELETE /api/shops/{}/products/{}", shopId, productId);
        shopService.deleteProduct(shopId, productId);
        return ResponseEntity.noContent().build();
    }
}
