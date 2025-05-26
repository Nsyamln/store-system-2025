package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.response.ProfitDto;
import tokoibuelin.storesystem.model.response.ProfittDto;
import tokoibuelin.storesystem.model.response.StockProductDto;
import tokoibuelin.storesystem.service.ConsignmentService;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/secured/consignment")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:3000"})
public class ConsignmentController {

    @Autowired
    private ConsignmentService consignmentService;

    @GetMapping("/pending-payments-summary/{supplierId}")
    public ResponseEntity<Map<String, Object>> getPendingPaymentsSummaryBySupplier(@PathVariable String supplierId) {
        Map<String, Object> pendingPaymentsSummary = consignmentService.getPendingPaymentsSummaryBySupplier(supplierId);
        return ResponseEntity.ok(pendingPaymentsSummary);
    }

    @GetMapping("/paid-payments-summary/{supplierId}")
    public ResponseEntity<List<ProfittDto>> getPaidPaymentsSummaryBySupplier(
            @PathVariable String supplierId) {
        List<ProfittDto> profitDtos = consignmentService.getPaidPaymentsSummaryBySupplier(supplierId);
        return ResponseEntity.ok(profitDtos);
    }

    @PostMapping("/update-payment-status/{supplierId}")
    public ResponseEntity<String> updatePaymentStatus(@PathVariable String supplierId) {
        consignmentService.updatePaymentStatus(supplierId);
        return ResponseEntity.ok("Payment status updated successfully.");
    }

    @GetMapping("/getBySupplier/{supplierId}")
    public Response<Object> getBySupplier(@PathVariable String supplierId) {
        List<ProfitDto> listProfit =  consignmentService.getAll(supplierId);
        return Response.create("09","00","Succes",listProfit);
    }

    @GetMapping("/getStockProduct/{supplierId}")
    public Response<Object> getStockProduct(@PathVariable String supplierId) {
        List<StockProductDto> listProduct=  consignmentService.getStockProductBySupplierId(supplierId);
        return Response.create("09","00","Succes",listProduct);
    }
}
