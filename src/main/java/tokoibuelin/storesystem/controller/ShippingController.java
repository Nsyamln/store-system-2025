package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.model.response.ShippingCostResponse;
import tokoibuelin.storesystem.service.AddressService;
import tokoibuelin.storesystem.service.RajaOngkirService;
@RestController
@CrossOrigin(origins = {"http://127.0.0.1:5500","http://127.0.0.1:3000"})
public class ShippingController {
    @Autowired
    private AddressService addressService;

    @Autowired
    private RajaOngkirService rajaOngkirService;
    private static final String DEFAULT_ORIGIN_ID = "103"; // Ganti dengan ID yang sesuai dari RajaOngkir

    @GetMapping("/shipping-cost")
    public ShippingCostResponse getShippingCost(
            @RequestParam String destination,
            @RequestParam int weight) {

        System.out.println("Destination : " + destination);
        System.out.println("Weight : " + weight);
        ShippingCostResponse shippingCost = rajaOngkirService.getShippingCost("103", destination, weight, "jne");

        // Cetak estimasi pengiriman ke terminal
        System.out.println("Estimated Delivery Date: " + shippingCost.estimatedDeliveryDate());

        return shippingCost;
    }


}
