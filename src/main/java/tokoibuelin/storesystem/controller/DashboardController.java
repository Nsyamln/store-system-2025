package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.service.DashboardService;
import tokoibuelin.storesystem.util.SecurityContextHolder;


@RestController
@RequestMapping("/secured/dashboard")
@CrossOrigin(origins = {"http://127.0.0.1:5500","http://127.0.0.1:5173"})
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    @GetMapping("/count/{tableName}")
    public Response<Object> getCount(@PathVariable String tableName) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        System.out.println("Cek AUTH : " + authentication);
        if (authentication == null) {
            return Response.create("01", "00", "Unauthorized", null);
        }
        //untuk product & pengguna
        return dashboardService.countAll(authentication, tableName);
    }

    @GetMapping("/countUserByRole/{role}")
    public Response<Object> getCountUserByRole(@PathVariable String role) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        System.out.println("Cek AUTH : " + authentication);
        if (authentication == null) {
            return Response.create("01", "00", "Unauthorized", null);
        }
        return dashboardService.countUserByRole(authentication, role);
    }

    @GetMapping("/count-sale")
    public Response<Object> getCountSale() {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        System.out.println("Cek AUTH : " + authentication);
        if (authentication == null) {
            return Response.create("01", "00", "Unauthorized", null);
        }
        return dashboardService.countSales(authentication);
    }

}
