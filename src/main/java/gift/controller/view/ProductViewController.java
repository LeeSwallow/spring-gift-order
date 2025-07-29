package gift.controller.view;

import gift.common.aop.annotation.PreAuthorize;
import gift.common.mapper.DtoToEntityMapper;
import gift.common.mapper.ModelMapper;
import gift.common.model.CustomAuth;
import gift.common.model.CustomPage;
import gift.dto.CustomPageRequest;
import gift.dto.product.ProductCreateRequest;
import gift.entity.Product;
import gift.entity.type.UserRole;
import gift.service.product.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/products")
public class ProductViewController {
    private final ProductService productService;
    private final Validator validator;

    public ProductViewController(
            ProductService productService,
            Validator validator
    ) {
        this.productService = productService;
        this.validator = validator;
    }

    private void validateRequest(ProductCreateRequest request) {
        var violations = validator.validate(request)
                .stream()
                .findFirst();
        if (violations.isPresent()) {
            throw new IllegalArgumentException(violations.get().getMessage());
        }
    }

    @PreAuthorize(UserRole.ROLE_ADMIN)
    @GetMapping
    public String showProductList(
        Model model,
        @Valid @ModelAttribute CustomPageRequest request
    ) {
        CustomPage<Product> currentPage = productService.findAllBy(ModelMapper.toPageRequest(request));
        int start = Math.max(0, currentPage.getPage() - 2);
        int end = Math.min(currentPage.getTotalPages() - 1, currentPage.getPage() + 2);

        model.addAttribute("title", "관리자 상품 목록");
        model.addAttribute("pageInfo", currentPage);
        model.addAttribute("pageStart", start);
        model.addAttribute("pageEnd", end);

        return "admin/product/product-list";
    }

    @PreAuthorize(UserRole.ROLE_ADMIN)
    @GetMapping("/{id}")
    public String showProductDetails(
            @PathVariable @Min(value = 0, message = "상품 ID는 0 이상이어야 합니다.") Long id,
            Model model
    ) {
        Product product = productService.findById(id);

        model.addAttribute("title", "상품 상세 정보");
        model.addAttribute("product", product);
        return "admin/product/product-detail";
    }

    @PreAuthorize(UserRole.ROLE_ADMIN)
    @GetMapping("/create")
    public String createProductForm(Model model) {
        model.addAttribute("title", "상품 등록");
        return "admin/product/create-product";
    }

    @PreAuthorize(UserRole.ROLE_ADMIN)
    @PostMapping("/create")
    public String createProduct(
            @ModelAttribute ProductCreateRequest request,
            CustomAuth auth,
            Model model
    ) {
        try {
            validateRequest(request);
            Product product = DtoToEntityMapper.toEntity(request);
            Product createdProduct = productService.create(product, auth.role(), auth.userId());
            return "redirect:/admin/products/" + createdProduct.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/product/create-product";
        }
    }

}
