package tokoibuelin.storesystem.model.request;

public record UpdateProfileReq(
        String name,
        String email,
        String phone,
        String street,
        String rt,
        String rw,
        String village,
        String district,
        String city,
        String postalCode
) {
}
