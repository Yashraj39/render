package com.project.render.Service;

import com.lowagie.text.*;
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

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.text.NumberFormat;
import java.util.Locale;
import com.lowagie.text.Image;
import java.io.InputStream;

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
                .userDeleted(false)
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
        List<Booking> bookings = bookingRepository.findVisibleByUserId(userId);

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
        Booking booking = bookingRepository.findVisibleByIdAndUserId(bookingId, userId)
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
        Booking booking = bookingRepository.findVisibleByIdAndUserId(bookingId, userId)
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
            Document document = new Document(com.lowagie.text.PageSize.A4, 28, 28, 24, 28);
            PdfWriter.getInstance(document, out);
            document.open();

            NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("en", "IN"));

            java.awt.Color dark = new java.awt.Color(17, 24, 39);
            java.awt.Color navy = new java.awt.Color(15, 23, 42);
            java.awt.Color slate = new java.awt.Color(71, 85, 105);
            java.awt.Color lightText = new java.awt.Color(100, 116, 139);
            java.awt.Color border = new java.awt.Color(226, 232, 240);
            java.awt.Color softBg = new java.awt.Color(248, 250, 252);
            java.awt.Color tableHeader = new java.awt.Color(241, 245, 249);
            java.awt.Color accent = new java.awt.Color(37, 99, 235);
            java.awt.Color paidBg = new java.awt.Color(220, 252, 231);
            java.awt.Color paidText = new java.awt.Color(22, 101, 52);
            java.awt.Color grandBg = new java.awt.Color(239, 246, 255);

            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, java.awt.Color.WHITE);
            Font whiteSmall = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.WHITE);
            Font invoiceTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, dark);
            Font sectionTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, dark);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, dark);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, slate);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, lightText);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, dark);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, navy);
            Font totalLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, slate);
            Font paidFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, paidText);
            Font invoiceMetaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, dark);
            Font whiteSection = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.WHITE);

            Image logo = null;

            try (InputStream is = getClass().getResourceAsStream("/static/logo.png")) {
                if (is != null) {
                    byte[] logoBytes = is.readAllBytes();
                    logo = Image.getInstance(logoBytes);
                    logo.scaleToFit(48, 48);
                    logo.setAlignment(Image.ALIGN_LEFT);
                }
            } catch (Exception e) {
                System.out.println("Logo not found or failed to load");
            }

            // =========================
            // TOP PREMIUM HEADER
            // =========================
            PdfPTable hero = new PdfPTable(2);
            hero.setWidthPercentage(100);
            hero.setWidths(new float[]{2.7f, 1.4f});
            hero.setSpacingAfter(18f);

            PdfPCell brandCell = new PdfPCell();
            brandCell.setBackgroundColor(navy);
            brandCell.setPadding(22f);
            brandCell.setBorder(Rectangle.NO_BORDER);

            PdfPTable brandHeader = new PdfPTable(2);
            brandHeader.setWidthPercentage(100);
            brandHeader.setWidths(new float[]{0.9f, 4.1f});
            brandHeader.setSpacingAfter(16f);

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setBackgroundColor(navy);
            logoCell.setVerticalAlignment(Element.ALIGN_TOP);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            logoCell.setPadding(0f);
            logoCell.setPaddingTop(18f);
            logoCell.setPaddingRight(10f);

            if (logo != null) {
                logoCell.addElement(logo);
            }

            PdfPCell brandTextCell = new PdfPCell();
            brandTextCell.setBorder(Rectangle.NO_BORDER);
            brandTextCell.setBackgroundColor(navy);
            brandTextCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            brandTextCell.setPadding(0f);

            Paragraph appName = new Paragraph("SlotMyStyle", brandFont);
            appName.setSpacingAfter(4f);
            brandTextCell.addElement(appName);

            Paragraph appTag = new Paragraph("Elevating Your Grooming Experience", whiteSmall);
            appTag.setSpacingAfter(0f);
            brandTextCell.addElement(appTag);

            brandHeader.addCell(logoCell);
            brandHeader.addCell(brandTextCell);

            brandCell.addElement(brandHeader);

            Paragraph salonName = new Paragraph(
                    safe(salon.getName()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, java.awt.Color.WHITE)
            );
            salonName.setSpacingAfter(6f);
            brandCell.addElement(salonName);

            Paragraph address = new Paragraph(
                    safe(salon.getAddress()) + ", " + safe(salon.getCity()),
                    whiteSmall
            );
            address.setSpacingAfter(4f);
            brandCell.addElement(address);

            Paragraph contact = new Paragraph(
                    "Contact: " + safe(salon.getContact()) + "   |   Email: " + safe(salon.getSalonEmail()),
                    whiteSmall
            );
            brandCell.addElement(contact);

            PdfPCell invoiceCell = new PdfPCell();
            invoiceCell.setBackgroundColor(new java.awt.Color(245, 247, 250));
            invoiceCell.setPadding(18f);
            invoiceCell.setBorder(Rectangle.NO_BORDER);

            Paragraph invoiceHeading = new Paragraph("INVOICE", invoiceTitle);
            invoiceHeading.setAlignment(Element.ALIGN_CENTER);
            invoiceHeading.setSpacingAfter(14f);
            invoiceCell.addElement(invoiceHeading);

            invoiceCell.addElement(new Paragraph("Invoice No: " + booking.getId(), invoiceMetaFont));
            invoiceCell.addElement(new Paragraph("Generated On: " + formatPdfDateTimeIst(), invoiceMetaFont));
            invoiceCell.addElement(new Paragraph("Payment Status: Paid", invoiceMetaFont));
            invoiceCell.addElement(new Paragraph("Booking Status: " + safe(booking.getStatus()), invoiceMetaFont));

            hero.addCell(brandCell);
            hero.addCell(invoiceCell);
            document.add(hero);

            // =========================
            // BOOKING + SALON CARDS
            // =========================
            PdfPTable infoCards = new PdfPTable(2);
            infoCards.setWidthPercentage(100);
            infoCards.setWidths(new float[]{1, 1});
            infoCards.setSpacingAfter(18f);

            PdfPCell bookingCard = createCardCell("Booking Details", sectionTitle, border, softBg);
            bookingCard.addElement(new Paragraph("Customer: " + safe(booking.getCustomerName()), normalFont));
            bookingCard.addElement(new Paragraph("Barber: " + (barber != null ? safe(barber.getName()) : "-"), normalFont));
            bookingCard.addElement(new Paragraph("Booking Date: " + formatPdfDate(booking.getBookingDate()), normalFont));
            bookingCard.addElement(new Paragraph(
                    "Time: " + formatPdfTime(booking.getStartTime()) + " to " + formatPdfTime(booking.getEndTime()),
                    normalFont
            ));

            PdfPCell salonCard = createCardCell("Salon Information", sectionTitle, border, softBg);
            salonCard.addElement(new Paragraph("Salon: " + safe(salon.getName()), normalFont));
            salonCard.addElement(new Paragraph("City: " + safe(salon.getCity()), normalFont));
            salonCard.addElement(new Paragraph("Contact: " + safe(salon.getContact()), normalFont));
            salonCard.addElement(new Paragraph("Email: " + safe(salon.getSalonEmail()), normalFont));

            infoCards.addCell(bookingCard);
            infoCards.addCell(salonCard);
            document.add(infoCards);

            // =========================
            // SERVICES TITLE STRIP
            // =========================
            PdfPTable strip = new PdfPTable(1);
            strip.setWidthPercentage(100);
            strip.setSpacingAfter(8f);

            PdfPCell stripCell = new PdfPCell(new Phrase("Services Summary", whiteSection));
            stripCell.setBackgroundColor(accent);
            stripCell.setPadding(10f);
            stripCell.setBorder(Rectangle.NO_BORDER);
            strip.addCell(stripCell);

            document.add(strip);

            // =========================
            // SERVICE TABLE
            // =========================
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4.8f, 1.8f, 1.4f, 2f});
            table.setSpacingAfter(18f);

            addFancyHeaderCell(table, "Service", headerFont, tableHeader, border);
            addFancyHeaderCell(table, "Duration", headerFont, tableHeader, border);
            addFancyHeaderCell(table, "Qty", headerFont, tableHeader, border);
            addFancyHeaderCell(table, "Amount", headerFont, tableHeader, border);

            if (booking.getServices() != null && !booking.getServices().isEmpty()) {
                for (CartItem item : booking.getServices()) {
                    addFancyBodyCell(table, safe(item.getServiceName()), normalFont, border);
                    addFancyBodyCell(table, item.getTime() + " min", normalFont, border);
                    addFancyBodyCell(table, "1", normalFont, border);
                    addFancyBodyCell(table, "₹ " + currencyFormat.format(item.getPrice()), normalFont, border);
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No services found", normalFont));
                emptyCell.setColspan(4);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(12f);
                emptyCell.setBorderColor(border);
                table.addCell(emptyCell);
            }

            document.add(table);

            // =========================
            // PAYMENT + SUMMARY SECTION
            // =========================
            PdfPTable bottom = new PdfPTable(2);
            bottom.setWidthPercentage(100);
            bottom.setWidths(new float[]{1.7f, 1f});
            bottom.setSpacingAfter(18f);

            PdfPCell paymentBox = createCardCell("Payment Information", sectionTitle, border, java.awt.Color.WHITE);
            paymentBox.addElement(new Paragraph("Payment Status", labelFont));
            paymentBox.addElement(new Paragraph(" ", normalFont));

            PdfPTable paidBadgeWrap = new PdfPTable(1);
            paidBadgeWrap.setWidthPercentage(38);

            PdfPCell paidBadge = new PdfPCell(new Phrase("PAID", paidFont));
            paidBadge.setHorizontalAlignment(Element.ALIGN_CENTER);
            paidBadge.setPadding(8f);
            paidBadge.setBackgroundColor(paidBg);
            paidBadge.setBorderColor(new java.awt.Color(187, 247, 208));
            paidBadgeWrap.addCell(paidBadge);

            paymentBox.addElement(paidBadgeWrap);
            paymentBox.addElement(new Paragraph(" ", normalFont));
            paymentBox.addElement(new Paragraph("Booking ID: " + booking.getId(), normalFont));

            PdfPCell summaryBox = new PdfPCell();
            summaryBox.setPadding(16f);
            summaryBox.setBorderColor(border);
            summaryBox.setBackgroundColor(grandBg);

            Paragraph summaryTitle = new Paragraph("Invoice Summary", sectionTitle);
            summaryTitle.setSpacingAfter(12f);
            summaryBox.addElement(summaryTitle);

            summaryBox.addElement(new Paragraph(
                    "Total Services: " + (booking.getServices() != null ? booking.getServices().size() : 0),
                    totalLabelFont
            ));
            summaryBox.addElement(new Paragraph(
                    "Total Duration: " + booking.getTotalTime() + " min",
                    totalLabelFont
            ));
            summaryBox.addElement(new Paragraph(" ", normalFont));

            Paragraph grandTotal = new Paragraph(
                    "Grand Total: ₹ " + currencyFormat.format(booking.getTotalPrice()),
                    totalFont
            );
            grandTotal.setSpacingBefore(4f);
            summaryBox.addElement(grandTotal);

            bottom.addCell(paymentBox);
            bottom.addCell(summaryBox);
            document.add(bottom);

            // =========================
            // FOOTER
            // =========================
            PdfPTable footer = new PdfPTable(1);
            footer.setWidthPercentage(100);

            PdfPCell footerCell = new PdfPCell();
            footerCell.setPadding(14f);
            footerCell.setBorderColor(border);
            footerCell.setBackgroundColor(softBg);

            Paragraph thanks = new Paragraph("Thank you for booking with SlotMyStyle.", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, slate));
            thanks.setAlignment(Element.ALIGN_CENTER);
            thanks.setSpacingAfter(6f);
            footerCell.addElement(thanks);

            Paragraph note = new Paragraph(
                    "This is a system-generated invoice for your salon booking.",
                    smallFont
            );
            note.setAlignment(Element.ALIGN_CENTER);
            footerCell.addElement(note);

            footer.addCell(footerCell);
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

        Booking booking = bookingRepository
                .findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to cancel this booking");
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Booking already cancelled");
        }

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

    private boolean canDeleteFromHistory(Booking booking) {
        if (booking == null) return false;

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            return true;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        return booking.getBookingDate().isBefore(today) ||
                (booking.getBookingDate().isEqual(today) && booking.getEndTime().isBefore(now));
    }

    public void userDeleteBookingHistory(String bookingId, String userId) {

        Booking booking = bookingRepository
                .findVisibleByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this booking");
        }

        if (!canDeleteFromHistory(booking)) {
            throw new RuntimeException("Only completed or cancelled booking history can be deleted");
        }

        booking.setUserDeleted(true);
        booking.setUserDeletedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }

    public int userDeleteAllBookingHistory(String userId) {

        List<Booking> bookings = bookingRepository.findVisibleByUserId(userId);

        List<Booking> deletable = bookings.stream()
                .filter(this::canDeleteFromHistory)
                .toList();

        if (deletable.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();

        deletable.forEach(booking -> {
            booking.setUserDeleted(true);
            booking.setUserDeletedAt(now);
        });

        bookingRepository.saveAll(deletable);

        return deletable.size();
    }

    private PdfPCell createCardCell(String title, Font titleFont, java.awt.Color borderColor, java.awt.Color bgColor) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(16f);
        cell.setBorderColor(borderColor);
        cell.setBackgroundColor(bgColor);

        Paragraph heading = new Paragraph(title, titleFont);
        heading.setSpacingAfter(10f);
        cell.addElement(heading);

        return cell;
    }

    private void addFancyHeaderCell(
            PdfPTable table,
            String text,
            Font font,
            java.awt.Color bg,
            java.awt.Color borderColor
    ) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(10f);
        cell.setBackgroundColor(bg);
        cell.setBorderColor(borderColor);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addFancyBodyCell(
            PdfPTable table,
            String text,
            Font font,
            java.awt.Color borderColor
    ) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(10f);
        cell.setBorderColor(borderColor);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private String formatPdfDate(LocalDate date) {
        if (date == null) return "-";
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private String formatPdfTime(LocalTime time) {
        if (time == null) return "-";
        return time.format(DateTimeFormatter.ofPattern("h:mm a")).toLowerCase();
    }

    private String formatPdfDateTimeIst() {
        return ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a"))
                .toLowerCase();
    }

}