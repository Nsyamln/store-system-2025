package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.response.ProfitDto;
import tokoibuelin.storesystem.model.response.ProfittDto;
import tokoibuelin.storesystem.model.response.StockProductDto;
import tokoibuelin.storesystem.service.ProfitSharingService;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/secured/profit-sharing")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:3000"})
public class ProfitSharingController {

    @Autowired
    private ProfitSharingService profitSharingService;

    @GetMapping("/pending-payments-summary/{supplierId}")
    public ResponseEntity<Map<String, Object>> getPendingPaymentsSummaryBySupplier(
            @PathVariable String supplierId) {
        Map<String, Object> pendingPaymentsSummary = profitSharingService
                .getPendingPaymentsSummaryBySupplier(supplierId);
        return ResponseEntity.ok(pendingPaymentsSummary);
    }

    @GetMapping("/paid-payments-summary/{supplierId}")
    public ResponseEntity<List<ProfittDto>> getPaidPaymentsSummaryBySupplier(
            @PathVariable String supplierId) {
        List<ProfittDto> profitDtos = profitSharingService.getPaidPaymentsSummaryBySupplier(supplierId);
        return ResponseEntity.ok(profitDtos);
    }


    @PostMapping("/update-payment-status/{supplierId}")
    public ResponseEntity<String> updatePaymentStatus(@PathVariable String supplierId) {
        profitSharingService.updatePaymentStatus(supplierId);
        return ResponseEntity.ok("Payment status updated successfully.");
    }

    @GetMapping("/getBySupplier/{supplierId}")
    public Response<Object> getBySupplier(@PathVariable String supplierId) {
        List<ProfitDto> listProfit =  profitSharingService.getAll(supplierId);
        return Response.create("09","00","Succes",listProfit);
    }

    @GetMapping("/getStockProduct/{supplierId}")
    public Response<Object> getStockProduct(@PathVariable String supplierId) {
        List<StockProductDto> listProduct=  profitSharingService.getStockProductBySupplierId(supplierId);
        return Response.create("09","00","Succes",listProduct);
    }
}
