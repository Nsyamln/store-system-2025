package tokoibuelin.storesystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.entity.Address;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.UpdateAddressReq;
import tokoibuelin.storesystem.model.request.addAddressesReq;
import tokoibuelin.storesystem.service.AddressService;
import tokoibuelin.storesystem.util.SecurityContextHolder;

@RestController
@RequestMapping("/secured/address")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:5173"})
public class AddressController {

    private final AddressService addressService;

    public AddressController(final AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/edit-address")
    public Response<Object> editAddress(@RequestBody UpdateAddressReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return addressService.updateAddress(authentication, req);
    }
    @PostMapping("/add-address")
    public Response<Object> addAddress(@RequestBody addAddressesReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return addressService.addAddress(authentication, req);
    }


    @PostMapping("/validate")
    public ResponseEntity<Response<Object>> validateAddress(@RequestBody Address address) {
        Response<Object> response = addressService.validateAddress(address);
        if (response.code().endsWith("200")) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
