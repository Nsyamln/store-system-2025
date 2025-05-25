package tokoibuelin.storesystem.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.element.*;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tokoibuelin.storesystem.entity.SaleDetails;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.response.CashFlowDto;
import tokoibuelin.storesystem.model.response.SaleDto;
import tokoibuelin.storesystem.repository.ProfitSharingRepository;
import tokoibuelin.storesystem.repository.SaleRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService extends AbstractService{

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProfitSharingRepository profitSharingRepository;

    public ByteArrayOutputStream createSaleReportByMethod(final String paymentMethod, String startDate, String endDate) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            List<SaleDto> sales = saleRepository.getSalesReportByPaymentMethod(paymentMethod,startDate, endDate);

            // Inisialisasi PDF dengan orientasi landscape
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            // Load font
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont smallFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Header
            Paragraph header = new Paragraph("Laporan Transaksi Penjualan")
                    .setFont(font)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(header);

            Paragraph storeInfo = new Paragraph("Toko Oleh Oleh Ibu Elin")
                    .setFont(regularFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(storeInfo);

            // Format periode
            LocalDate startLocalDate = LocalDate.parse(startDate);
            LocalDate endLocalDate = LocalDate.parse(endDate);
            DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            String formattedStartDate = startLocalDate.format(headerFormatter);
            String formattedEndDate = endLocalDate.format(headerFormatter);

            Paragraph dateRange = new Paragraph(String.format("Periode: %s - %s", formattedStartDate, formattedEndDate))
                    .setFont(regularFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(dateRange);

            // Add space between header and table
            document.add(new Paragraph("\n"));

            // Create Table with landscape orientation
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 3, 2, 4, 1, 2, 2, 3}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);

            // Add Table Header
            table.addHeaderCell(new Cell().add(new Paragraph("Tanggal").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nomor Transaksi").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nama Pelanggan").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Kode Produk").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nama Produk").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Jumlah").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Harga Satuan").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Total Harga").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Metode Pembayaran").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy/HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
            for (SaleDto sale : sales) {
                // Data untuk setiap detail penjualan
                for (SaleDetails detail : sale.saleDetails()) {
                    BigDecimal price = BigDecimal.valueOf(detail.price().doubleValue());
                    BigDecimal quantity = BigDecimal.valueOf(detail.quantity());
                    BigDecimal totalPrice = price.multiply(quantity);
                    String formattedDate = formatter.format(sale.saleDate());

                    table.addCell(new Cell().add(new Paragraph(formattedDate).setFont(smallFont).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(sale.saleId()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(sale.customerName()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(detail.productId()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Kode Produk
                    table.addCell(new Cell().add(new Paragraph(detail.productName()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Nama Produk
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.quantity())).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Jumlah
                    table.addCell(new Cell().add(new Paragraph(String.format("Rp%,.2f", price)).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Harga Satuan
                    table.addCell(new Cell().add(new Paragraph(String.format("Rp%,.2f", totalPrice)).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Total Harga
                    table.addCell(new Cell().add(new Paragraph(sale.paymentMethod() != null ? sale.paymentMethod().toString() : "").setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Metode Pembayaran
                }
            }

            document.add(table);
            document.close();
            return outputStream;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public ByteArrayOutputStream createSaleReportByMethodAll(final String startDate, final String endDate) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            List<SaleDto> sales = saleRepository.getSalesReportByPaymentMethodAll(startDate, endDate);

            // Inisialisasi PDF dengan orientasi landscape
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            // Load font
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont smallFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Header
            Paragraph header = new Paragraph("Laporan Transaksi Penjualan")
                    .setFont(font)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(header);

            Paragraph storeInfo = new Paragraph("Toko Oleh Oleh Ibu Elin")
                    .setFont(regularFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(storeInfo);

            // Format periode
            LocalDate startLocalDate = LocalDate.parse(startDate);
            LocalDate endLocalDate = LocalDate.parse(endDate);
            DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            String formattedStartDate = startLocalDate.format(headerFormatter);
            String formattedEndDate = endLocalDate.format(headerFormatter);

            Paragraph dateRange = new Paragraph(String.format("Periode: %s - %s", formattedStartDate, formattedEndDate))
                    .setFont(regularFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(dateRange);

            // Add space between header and table
            document.add(new Paragraph("\n"));

            // Create Table with landscape orientation
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 3, 2, 4, 1, 2, 2, 3}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);

            // Add Table Header
            table.addHeaderCell(new Cell().add(new Paragraph("Tanggal").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nomor Transaksi").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nama Pelanggan").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Kode Produk").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Nama Produk").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Jumlah").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Harga Satuan").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Total Harga").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Metode Pembayaran").setFont(font).setFontSize(12)).setTextAlignment(TextAlignment.CENTER));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy/HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
            for (SaleDto sale : sales) {
                // Data untuk setiap detail penjualan
                for (SaleDetails detail : sale.saleDetails()) {
                    BigDecimal price = BigDecimal.valueOf(detail.price().doubleValue());
                    BigDecimal quantity = BigDecimal.valueOf(detail.quantity());
                    BigDecimal totalPrice = price.multiply(quantity);
                    String formattedDate = formatter.format(sale.saleDate());

                    table.addCell(new Cell().add(new Paragraph(formattedDate).setFont(smallFont).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(sale.saleId()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(sale.customerName()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
                    table.addCell(new Cell().add(new Paragraph(detail.productId()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Kode Produk
                    table.addCell(new Cell().add(new Paragraph(detail.productName()).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Nama Produk
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.quantity())).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Jumlah
                    table.addCell(new Cell().add(new Paragraph(String.format("Rp%,.2f", price)).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Harga Satuan
                    table.addCell(new Cell().add(new Paragraph(String.format("Rp%,.2f", totalPrice)).setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Total Harga
                    table.addCell(new Cell().add(new Paragraph(sale.paymentMethod() != null ? sale.paymentMethod().toString() : "").setFont(smallFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER))); // Metode Pembayaran
                }
            }

            document.add(table);
            document.close();
            return outputStream;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


//    public ByteArrayOutputStream createSaleReportByMethodAll(final String startDate, final String endDate) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        try {
//            List<SaleDto> sales = saleRepository.getSalesReportByPaymentMethodAll(startDate, endDate);
//            System.out.println("Sales size: " + sales.size());
//
//            // Inisialisasi PDF
//            PdfWriter writer = new PdfWriter(outputStream);
//            PdfDocument pdf = new PdfDocument(writer);
//            Document document = new Document(pdf);
//
//            // Load font
//            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//
//            // Header
//            Paragraph header = new Paragraph("Laporan Transaksi Penjualan")
//                    .setFont(font)
//                    .setFontSize(18)
//                    .setTextAlignment(TextAlignment.CENTER)
//                    .setBold();
//            document.add(header);
//
//            Paragraph storeInfo = new Paragraph("Toko Oleh Oleh Ibu Elin")
//                    .setFont(regularFont)
//                    .setFontSize(14)
//                    .setTextAlignment(TextAlignment.CENTER);
//            document.add(storeInfo);
//
//            // Format periode
//            LocalDate startLocalDate = LocalDate.parse(startDate);
//            LocalDate endLocalDate = LocalDate.parse(endDate);
//            DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
//            String formattedStartDate = startLocalDate.format(headerFormatter);
//            String formattedEndDate = endLocalDate.format(headerFormatter);
//
//            Paragraph dateRange = new Paragraph(String.format("Periode: %s - %s", formattedStartDate, formattedEndDate))
//                    .setFont(regularFont)
//                    .setFontSize(12)
//                    .setTextAlignment(TextAlignment.CENTER);
//            document.add(dateRange);
//
//            // Add space between header and table
//            document.add(new Paragraph("\n"));
//
//            // Create Table
//            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 3, 2, 3, 2, 2, 3, 3}))
//                    .setWidth(UnitValue.createPercentValue(100));
//
//            // Add Table Header
//            table.addHeaderCell(new Cell().add(new Paragraph("Tanggal")).setTextAlignment(TextAlignment.CENTER));
//            table.addHeaderCell(new Cell().add(new Paragraph("Nomor Transaksi")).setTextAlignment(TextAlignment.CENTER));
//            table.addHeaderCell(new Cell().add(new Paragraph("Nama Pelanggan")).setTextAlignment(TextAlignment.CENTER));
//            table.addHeaderCell(new Cell().add(new Paragraph("Kode Produk")).setTextAlignment(TextAlignment.CENTER));
//            table.addHeaderCell(new Cell().add(new Paragraph("Nama Produk")).setTextAlignment(TextAlignment.CENTER));
//            table.addHeaderCell(new Cell().add(new Paragraph("Jumlah")).setTextAlignment(TextAlignment.CENTER));
//            table.addHeaderCell(new Cell().add(new Paragraph("Harga Satuan")).setTextAlignment(TextAlignment.CENTER));
//            table.addHeaderCell(new Cell().add(new Paragraph("Total Harga")).setTextAlignment(TextAlignment.CENTER));
//            table.addHeaderCell(new Cell().add(new Paragraph("Metode Pembayaran")).setTextAlignment(TextAlignment.CENTER));
//
//            for (SaleDto sale : sales) {
//                System.out.println("Processing Sale ID: " + sale.saleId());
//                boolean hasDetails = false;
//                // Data untuk setiap detail penjualan
//                for (SaleDetails detail : sale.saleDetails()) {
//                    BigDecimal price = BigDecimal.valueOf(detail.price());
//                    BigDecimal quantity = BigDecimal.valueOf(detail.quantity());
//                    BigDecimal totalPrice = price.multiply(quantity);
//
//                    System.out.println("  Product ID: " + detail.productId());
//                    System.out.println("  Quantity: " + detail.quantity());
//                    System.out.println("  Price: " + price);
//                    System.out.println("  Total Price: " + totalPrice);
//
//                    table.addCell(sale.saleDate() != null ? sale.saleDate().toString() : "");
//                    table.addCell(sale.saleId() != null ? sale.saleId() : "");
//                    table.addCell(sale.customerName() != null ? sale.customerName() : "");
//                    table.addCell(detail.productId()); // Kode Produk
//                    table.addCell(detail.productName()); // Nama Produk
//                    table.addCell(String.valueOf(detail.quantity())); // Jumlah
//                    table.addCell(String.format("Rp%,.2f", price)); // Harga Satuan
//                    table.addCell(String.format("Rp%,.2f", totalPrice)); // Total Harga
//                    table.addCell(sale.paymentMethod() != null ? sale.paymentMethod().toString() : "");
//
//                    hasDetails = true;
//                }
//
//                // Add a row for sales with no details
//                if (!hasDetails) {
//                    table.addCell(sale.saleDate() != null ? sale.saleDate().toString() : "");
//                    table.addCell(sale.saleId() != null ? sale.saleId() : "");
//                    table.addCell(sale.customerName() != null ? sale.customerName() : "");
//                    table.addCell(""); // Kode Produk
//                    table.addCell(""); // Nama Produk
//                    table.addCell(""); // Jumlah
//                    table.addCell(""); // Harga Satuan
//                    table.addCell(""); // Total Harga
//                    table.addCell(sale.paymentMethod() != null ? sale.paymentMethod().toString() : "");
//                }
//            }
//
//            document.add(table);
//            document.close();
//            return outputStream;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }



    public Response getCashFlow(final String startDate, final String endDate) {
        try {
            // Mengambil data dari repository
            Long totalPenjualan = saleRepository.sumSales(startDate, endDate);
            Long totalBRI = saleRepository.sumByPaymentMethod(startDate, endDate, "BANK_BRI");
            Long totalCash = saleRepository.sumByPaymentMethod(startDate, endDate, "CASH");
            Long totalShopeepay = saleRepository.sumByPaymentMethod(startDate, endDate, "SHOPEEPAY");
            Long totalPenerimaan = totalPenjualan;
            Long totalSharing = profitSharingRepository.sumSharing(startDate, endDate);
            Long kasMasuk = totalPenjualan;
            Long kasKeluar = totalSharing;
            Long kasBersih = kasMasuk - kasKeluar;

            // Membuat DTO dengan data yang diambil
            CashFlowDto cashFlowDto = new CashFlowDto(
                    totalPenjualan,
                    totalBRI,
                    totalCash,
                    totalShopeepay,
                    totalPenerimaan,
                    totalSharing,
                    kasMasuk,
                    kasKeluar,
                    kasBersih
            );

            // Mengembalikan response sukses dengan DTO
            return Response.create("09", "00", "Sukses", cashFlowDto);

        } catch (Exception e) {
            // Menangani kesalahan dan mengembalikan response error
            e.printStackTrace();
            return Response.create("01", "00", "Database error", null);
        }
    }


    public ByteArrayOutputStream generateCashFlow(String startDate, String endDate) throws DocumentException, IOException {
        Long totalPenjualan = saleRepository.sumSales(startDate, endDate);
        Long totalBRI = saleRepository.sumByPaymentMethod(startDate, endDate, "BANK_BRI");
        Long totalCash = saleRepository.sumByPaymentMethod(startDate, endDate, "CASH");
        Long totalShopeepay = saleRepository.sumByPaymentMethod(startDate, endDate, "SHOPEEPAY");
        Long totalPenerimaan = totalPenjualan;
        Long totalSharing = profitSharingRepository.sumSharing(startDate, endDate);
        Long kasMasuk = totalPenjualan;
        Long kasKeluar = totalSharing;
        Long kasBersih = kasMasuk - kasKeluar;

        CashFlowDto cashFlowDto = new CashFlowDto(
                totalPenjualan,
                totalBRI,
                totalCash,
                totalShopeepay,
                totalPenerimaan,
                totalSharing,
                kasMasuk,
                kasKeluar,
                kasBersih
        );

        // Generate PDF
        return generateCashFlowPdf(cashFlowDto,startDate,endDate);
    }


    public ByteArrayOutputStream generateCashFlowPdf(CashFlowDto cashFlowDto, String startDate, String endDate) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Initialize PdfWriter and PdfDocument
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4);
        Document document = new Document(pdfDoc);

        // Font setup
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Title
        document.add(new Paragraph("Laporan Arus Kas")
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold());

        // Add store information
        document.add(new Paragraph("Toko Oleh Oleh Ibu Elin")
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));

        // Format tanggal
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        LocalDate startLocalDate = LocalDate.parse(startDate);
        LocalDate endLocalDate = LocalDate.parse(endDate);
        String formattedStartDate = startLocalDate.format(formatter);
        String formattedEndDate = endLocalDate.format(formatter);

        // Add report details
        document.add(new Paragraph(String.format("Periode: %s - %s", formattedStartDate, formattedEndDate))
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(" "));

        // Add line separator
        LineSeparator lineSeparator = new LineSeparator(new SolidLine());
        lineSeparator.setWidth(UnitValue.createPercentValue(100));
        document.add(lineSeparator);

        // Add section header
        document.add(new Paragraph("Arus Kas dari Aktivitas Operasional")
                .setFont(boldFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.LEFT));
        document.add(new Paragraph(" "));

        // Add table for "Pendapatan dari Penjualan"
        document.add(new Paragraph("Pendapatan dari Penjualan")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT));
        Table table1 = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100));
        table1.addCell(createCell("Total Penjualan:", TextAlignment.LEFT));
        table1.addCell(createCell(String.format("Rp %,d", cashFlowDto.totalPenjualan()), TextAlignment.RIGHT));
        document.add(table1);

        // Add table for "Metode Pembayaran"
        document.add(new Paragraph("Metode Pembayaran")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT));
        Table table2 = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100));
        table2.addCell(createCell("Bank Transfer:", TextAlignment.LEFT));
        table2.addCell(createCell(String.format("Rp %,d", cashFlowDto.bankBRI()), TextAlignment.RIGHT));
        table2.addCell(createCell("Tunai:", TextAlignment.LEFT));
        table2.addCell(createCell(String.format("Rp %,d", cashFlowDto.cash()), TextAlignment.RIGHT));
        table2.addCell(createCell("ShopeePay:", TextAlignment.LEFT));
        table2.addCell(createCell(String.format("Rp %,d", cashFlowDto.shopeepay()), TextAlignment.RIGHT));
        document.add(table2);

        // Add table for "Penerimaan dari Pelanggan"
        document.add(new Paragraph("Penerimaan dari Pelanggan")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT));
        Table table3 = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100));
        table3.addCell(createCell("Jumlah Penerimaan:", TextAlignment.LEFT));
        table3.addCell(createCell(String.format("Rp %,d", cashFlowDto.jumlahPenerimaan()), TextAlignment.RIGHT));
        document.add(table3);

        // Add table for "Pembayaran kepada Pemasok"
        document.add(new Paragraph("Pembayaran kepada Pemasok")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT));
        Table table4 = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100));
        table4.addCell(createCell("Total Pembayaran:", TextAlignment.LEFT));
        table4.addCell(createCell(String.format("Rp %,d", cashFlowDto.bayarPemasok()), TextAlignment.RIGHT));
        document.add(table4);

        // Add table for "Arus Kas Bersih dari Aktivitas Operasional"
        document.add(new Paragraph("Arus Kas Bersih dari Aktivitas Operasional")
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT));
        Table table5 = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100));
        table5.addCell(createCell("Total Kas Masuk:", TextAlignment.LEFT));
        table5.addCell(createCell(String.format("Rp %,d", cashFlowDto.kasMasuk()), TextAlignment.RIGHT));
        table5.addCell(createCell("Total Kas Keluar:", TextAlignment.LEFT));
        table5.addCell(createCell(String.format("Rp %,d", cashFlowDto.kasKeluar()), TextAlignment.RIGHT));
        table5.addCell(createCell("Arus Kas Bersih:", TextAlignment.LEFT));
        table5.addCell(createCell(String.format("Rp %,d", cashFlowDto.kasBersih()), TextAlignment.RIGHT));
        document.add(table5);

        // Close document
        document.close();

        return baos;
    }

    private Cell createCell(String content, TextAlignment alignment) {
        return new Cell().add(new Paragraph(content).setTextAlignment(alignment));
    }



}
