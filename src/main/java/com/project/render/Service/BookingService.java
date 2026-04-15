package com.project.render.Service;

import com.project.render.DTO.AvailableSlotResponse;
import com.project.render.DTO.BookingDetailsResponse;
import com.project.render.DTO.ConfirmBookingRequest;
import com.project.render.DTO.UserBookingCardResponse;
import com.project.render.Entity.*;
import com.project.render.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingCartRepository bookingCartRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private BookingCartService bookingCartService;

    @Autowired
    private BookingAvailabilityValidator bookingAvailabilityValidator;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    private void validateSalonBookingAllowed(String salonId) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (salon.getSalonOwnerId() == null || salon.getSalonOwnerId().isBlank()) {
            throw new RuntimeException("Salon owner not found");
        }

        User owner = userRepository.findByUserId(salon.getSalonOwnerId())
                .orElseThrow(() -> new RuntimeException("Salon owner not found"));

        if (Boolean.TRUE.equals(owner.getOwnerFrozen())) {
            throw new RuntimeException("Bookings are temporarily unavailable for this salon");
        }
    }

    public List<AvailableSlotResponse> getAvailableSlots(
            String userId,
            String salonId,
            String barberId,
            String customerName,
            LocalDate date
    ) {

        validateSalonBookingAllowed(salonId);

        BookingCart cart = getCart(userId, salonId, customerName);

        return bookingCartService.showAvailableTimes(
                barberId,
                cart.getTotalTime(),
                date
        );
    }

    public Booking confirmBooking(ConfirmBookingRequest request) {

        validateSalonBookingAllowed(request.getSalonId());

        BookingCart cart = getCart(
                request.getUserId(),
                request.getSalonId(),
                request.getCustomerName()
        );

        Barber barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        Salon salon = salonRepository.findById(request.getSalonId())
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        bookingAvailabilityValidator.validateDayAvailability(barber, salon, request.getBookingDate());
        bookingAvailabilityValidator.validateTimeWithinShift(barber, request.getStartTime(), request.getEndTime());
        validateTemporaryInactiveAvailability(
                barber,
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        List<Booking> bookings = bookingRepository.findByBarberIdAndBookingDateAndStatus(
                request.getBarberId(), request.getBookingDate(), "CONFIRMED"
        );

        boolean overlaps = bookings.stream().anyMatch(b ->
                request.getStartTime().isBefore(b.getEndTime()) &&
                        request.getEndTime().isAfter(b.getStartTime())
        );

        if (overlaps) throw new RuntimeException("Slot already booked");

        String normalizedCustomerName =
                request.getCustomerName() == null ? "" : request.getCustomerName().trim().toLowerCase();

        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .salonId(request.getSalonId())
                .barberId(request.getBarberId())
                .customerName(normalizedCustomerName)
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .services(cart.getItems())
                .totalPrice(cart.getTotalPrice())
                .totalTime(cart.getTotalTime())
                .status("CONFIRMED")
                .paymentStatus("UNPAID")
                .build();

        bookingRepository.save(booking);
        bookingCartRepository.delete(cart);

        return booking;
    }

    public Booking ownerCancelBooking(String bookingId, String ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Booking already cancelled");
        }

        Salon salon = salonRepository.findById(booking.getSalonId())
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (salon.getSalonOwnerId() == null || !salon.getSalonOwnerId().equals(ownerId)) {
            throw new RuntimeException("You are not allowed to cancel this booking");
        }

        booking.setStatus("CANCELLED");
        booking.setCancellationReason("Cancelled by owner");
        booking.setCancelledBy("OWNER");
        booking.setCancelledAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        String bookingInfo = "for " + booking.getBookingDate() + " at " +
                booking.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));

        notificationService.createBookingCancelledNotification(
                booking.getUserId(),
                booking.getId(),
                "Cancelled by owner",
                bookingInfo
        );

        return savedBooking;
    }

    private void validateTemporaryInactiveAvailability(
            Barber barber,
            LocalDate bookingDate,
            LocalTime bookingStart,
            LocalTime bookingEnd
    ) {
        if (barber.getTemporaryInactiveSlots() == null || barber.getTemporaryInactiveSlots().isEmpty()) {
            return;
        }

        boolean overlaps = barber.getTemporaryInactiveSlots().stream()
                .filter(slot -> slot.getDate() != null && slot.getDate().equals(bookingDate))
                .anyMatch(slot ->
                        slot.getStartTime() != null &&
                                slot.getEndTime() != null &&
                                bookingStart.isBefore(slot.getEndTime()) &&
                                bookingEnd.isAfter(slot.getStartTime())
                );

        if (overlaps) {
            throw new RuntimeException("Barber is temporarily unavailable for the selected time");
        }
    }

    private BookingCart getCart(String userId, String salonId, String customerName) {
        String normalizedCustomerName =
                customerName == null ? "" : customerName.trim().toLowerCase();

        return bookingCartRepository
                .findByUserIdAndSalonIdAndCustomerName(userId, salonId, normalizedCustomerName)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    public List<UserBookingCardResponse> getUserBookings(String userId, String filter, String sort) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        List<UserBookingCardResponse> response = new ArrayList<>();

        for (Booking booking : bookings) {
            Salon salon = salonRepository.findById(booking.getSalonId()).orElse(null);

            String derivedStatus;

            if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
                derivedStatus = "CANCELLED";
            } else if (
                    booking.getBookingDate().isBefore(today) ||
                            (booking.getBookingDate().isEqual(today) && booking.getEndTime().isBefore(nowTime))
            ) {
                derivedStatus = "COMPLETED";
            } else {
                derivedStatus = "UPCOMING";
            }

            boolean include = switch ((filter == null ? "ALL" : filter.toUpperCase())) {
                case "UPCOMING" -> derivedStatus.equals("UPCOMING");
                case "COMPLETED" -> derivedStatus.equals("COMPLETED");
                case "CANCELLED" -> derivedStatus.equals("CANCELLED");
                default -> true;
            };

            if (!include) continue;

            response.add(UserBookingCardResponse.builder()
                    .bookingId(booking.getId())
                    .salonId(booking.getSalonId())
                    .salonName(salon != null ? salon.getName() : "Salon")
                    .salonImageUrl(salon != null ? salon.getImageUrl() : null)
                    .customerName(booking.getCustomerName())
                    .serviceCount(booking.getServices() != null ? booking.getServices().size() : 0)
                    .totalPrice(booking.getTotalPrice())
                    .totalTime(booking.getTotalTime())
                    .bookingDate(booking.getBookingDate())
                    .startTime(booking.getStartTime())
                    .endTime(booking.getEndTime())
                    .bookingStatus(derivedStatus)
                    .paymentStatus(
                            booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "UNPAID"
                    )
                    .build());
        }

        if ("ASC".equalsIgnoreCase(sort)) {
            response.sort(Comparator.comparing(UserBookingCardResponse::getBookingDate)
                    .thenComparing(UserBookingCardResponse::getStartTime));
        } else {
            response.sort(Comparator.comparing(UserBookingCardResponse::getBookingDate)
                    .thenComparing(UserBookingCardResponse::getStartTime)
                    .reversed());
        }

        return response;
    }

    public BookingDetailsResponse getBookingDetails(String bookingId, String userId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Salon salon = salonRepository.findById(booking.getSalonId())
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        Barber barber = barberRepository.findById(booking.getBarberId())
                .orElse(null);

        List<BookingDetailsResponse.ServiceInfo> serviceList =
                booking.getServices() == null ? new ArrayList<>() :
                        booking.getServices().stream()
                                .map(item -> BookingDetailsResponse.ServiceInfo.builder()
                                        .categoryId(item.getCategoryId())
                                        .serviceId(item.getServiceId())
                                        .serviceName(item.getServiceName())
                                        .price(item.getPrice())
                                        .time(item.getTime())
                                        .imageUrl(item.getImageUrl())
                                        .build())
                                .toList();

        return BookingDetailsResponse.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .customerName(booking.getCustomerName())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .bookingStatus(booking.getStatus())
                .paymentStatus(
                        booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "UNPAID"
                )
                .totalPrice(booking.getTotalPrice())
                .totalTime(booking.getTotalTime())
                .serviceCount(serviceList.size())
                .salon(BookingDetailsResponse.SalonInfo.builder()
                        .salonId(salon.getId())
                        .name(salon.getName())
                        .city(salon.getCity())
                        .address(salon.getAddress())
                        .contact(salon.getContact())
                        .salonEmail(salon.getSalonEmail())
                        .imageUrl(salon.getImageUrl())
                        .interiorImageUrl(salon.getInteriorImageUrl())
                        .exteriorImageUrl(salon.getExteriorImageUrl())
                        .mapLink(salon.getMapLink())
                        .opentime(salon.getOpentime())
                        .closetime(salon.getClosetime())
                        .build())
                .barber(BookingDetailsResponse.BarberInfo.builder()
                        .barberId(barber != null ? barber.getId() : null)
                        .name(barber != null ? barber.getName() : "Barber")
                        .build())
                .services(serviceList)
                .build();
    }

    public byte[] generateBookingBillPdf(String bookingId, String userId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getPaymentStatus() == null || !booking.getPaymentStatus().equalsIgnoreCase("PAID")) {
            throw new RuntimeException("Bill available only for paid bookings");
        }

        Salon salon = salonRepository.findById(booking.getSalonId())
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        Barber barber = barberRepository.findById(booking.getBarberId())
                .orElse(null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font smallGrayFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph appName = new Paragraph("SlotMyStyle", titleFont);
            appName.setSpacingAfter(4f);
            document.add(appName);

            Paragraph invoiceText = new Paragraph("Booking Bill / Invoice", subTitleFont);
            invoiceText.setSpacingAfter(12f);
            document.add(invoiceText);

            PdfPTable topTable = new PdfPTable(2);
            topTable.setWidthPercentage(100);
            topTable.setWidths(new float[]{3, 2});
            topTable.setSpacingAfter(18f);

            PdfPCell salonCell = new PdfPCell();
            salonCell.setBorder(0);
            salonCell.setPadding(8f);
            salonCell.setBackgroundColor(new java.awt.Color(245, 245, 245));
            salonCell.addElement(new Paragraph("Salon Details", sectionTitleFont));
            salonCell.addElement(new Paragraph("Salon: " + safe(salon.getName()), normalFont));
            salonCell.addElement(new Paragraph("City: " + safe(salon.getCity()), normalFont));
            salonCell.addElement(new Paragraph("Address: " + safe(salon.getAddress()), normalFont));
            salonCell.addElement(new Paragraph("Contact: " + safe(salon.getContact()), normalFont));

            PdfPCell invoiceCell = new PdfPCell();
            invoiceCell.setBorder(0);
            invoiceCell.setPadding(8f);
            invoiceCell.setBackgroundColor(new java.awt.Color(245, 245, 245));
            invoiceCell.addElement(new Paragraph("Invoice Info", sectionTitleFont));
            invoiceCell.addElement(new Paragraph("Invoice No: " + booking.getId(), normalFont));
            invoiceCell.addElement(new Paragraph(
                    "Generated On: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                    normalFont
            ));
            invoiceCell.addElement(new Paragraph("Payment Status: " + safe(booking.getPaymentStatus()), normalFont));
            invoiceCell.addElement(new Paragraph("Booking Status: " + safe(booking.getStatus()), normalFont));

            topTable.addCell(salonCell);
            topTable.addCell(invoiceCell);
            document.add(topTable);

            Paragraph bookingSection = new Paragraph("Booking Details", sectionTitleFont);
            bookingSection.setSpacingAfter(8f);
            document.add(bookingSection);

            PdfPTable detailTable = new PdfPTable(2);
            detailTable.setWidthPercentage(100);
            detailTable.setWidths(new float[]{1, 1});
            detailTable.setSpacingAfter(18f);

            addDetailCell(detailTable, "Customer Name", safe(booking.getCustomerName()), boldFont, normalFont);
            addDetailCell(detailTable, "Barber", barber != null ? safe(barber.getName()) : "-", boldFont, normalFont);
            addDetailCell(detailTable, "Booking Date", String.valueOf(booking.getBookingDate()), boldFont, normalFont);
            addDetailCell(detailTable, "Time",
                    booking.getStartTime() + " - " + booking.getEndTime(), boldFont, normalFont);

            document.add(detailTable);

            Paragraph servicesSection = new Paragraph("Services", sectionTitleFont);
            servicesSection.setSpacingAfter(8f);
            document.add(servicesSection);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{5, 2, 2});
            table.setSpacingAfter(18f);

            PdfPCell h1 = new PdfPCell(new Phrase("Service", boldFont));
            PdfPCell h2 = new PdfPCell(new Phrase("Duration", boldFont));
            PdfPCell h3 = new PdfPCell(new Phrase("Price", boldFont));

            java.awt.Color headerBg = new java.awt.Color(230, 230, 230);

            h1.setBackgroundColor(headerBg);
            h2.setBackgroundColor(headerBg);
            h3.setBackgroundColor(headerBg);

            h1.setPadding(8f);
            h2.setPadding(8f);
            h3.setPadding(8f);

            table.addCell(h1);
            table.addCell(h2);
            table.addCell(h3);

            if (booking.getServices() != null) {
                for (CartItem item : booking.getServices()) {
                    PdfPCell s1 = new PdfPCell(new Phrase(safe(item.getServiceName()), normalFont));
                    PdfPCell s2 = new PdfPCell(new Phrase(item.getTime() + " min", normalFont));
                    PdfPCell s3 = new PdfPCell(new Phrase("Rs. " + item.getPrice(), normalFont));

                    s1.setPadding(8f);
                    s2.setPadding(8f);
                    s3.setPadding(8f);

                    table.addCell(s1);
                    table.addCell(s2);
                    table.addCell(s3);
                }
            }

            document.add(table);

            PdfPTable totalTable = new PdfPTable(1);
            totalTable.setWidthPercentage(38);
            totalTable.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);

            PdfPCell totalCell = new PdfPCell();
            totalCell.setPadding(10f);
            totalCell.setBackgroundColor(new java.awt.Color(248, 248, 248));

            totalCell.addElement(new Paragraph("Summary", sectionTitleFont));
            totalCell.addElement(new Paragraph("Total Services: " +
                    (booking.getServices() != null ? booking.getServices().size() : 0), normalFont));
            totalCell.addElement(new Paragraph("Total Time: " + booking.getTotalTime() + " min", normalFont));
            totalCell.addElement(new Paragraph("Total Amount: Rs. " + booking.getTotalPrice(), boldFont));

            totalTable.addCell(totalCell);
            document.add(totalTable);

            Paragraph footerSpace = new Paragraph(" ");
            footerSpace.setSpacingBefore(12f);
            document.add(footerSpace);

            Paragraph footer = new Paragraph(
                    "Thank you for booking with SlotMyStyle.",
                    smallGrayFont
            );
            footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF bill", e);
        }

        return out.toByteArray();
    }

    private void addDetailCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8f);
        cell.setBorderColor(new java.awt.Color(220, 220, 220));
        cell.addElement(new Paragraph(label, labelFont));
        cell.addElement(new Paragraph(value, valueFont));
        table.addCell(cell);
    }

    private String safe(String value) {
        return value != null ? value : "-";
    }

    public Booking userCancelBooking(String bookingId, String userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to cancel this booking");
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Booking already cancelled");
        }

        // Optional: prevent cancelling completed bookings
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (
                booking.getBookingDate().isBefore(today) ||
                        (booking.getBookingDate().isEqual(today) && booking.getEndTime().isBefore(now))
        ) {
            throw new RuntimeException("Cannot cancel completed booking");
        }

        booking.setStatus("CANCELLED");
        booking.setCancellationReason("Cancelled by user");
        booking.setCancelledBy("USER");
        booking.setCancelledAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        // Optional notification
        String bookingInfo = "for " + booking.getBookingDate() + " at " +
                booking.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));

        notificationService.createBookingCancelledNotification(
                booking.getUserId(),
                booking.getId(),
                "Cancelled by user",
                bookingInfo
        );

        return savedBooking;
    }
}