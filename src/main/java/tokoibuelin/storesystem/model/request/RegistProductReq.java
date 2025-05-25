package tokoibuelin.storesystem.model.request;

public record RegistProductReq(
  String productName,
  String description,
  Long unit,
  Long price,
  Long stock,
  String supplierId,
  String productImage,
  Integer categoryId,
  Long purchasePrice

  ){
}
