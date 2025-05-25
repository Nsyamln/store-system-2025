package tokoibuelin.storesystem.model.request;

public record UpdateProductReq(String productId,String productName, String description, Long unit, Long price, String productImage) {
}
