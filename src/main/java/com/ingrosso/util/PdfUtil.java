package com.ingrosso.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.ingrosso.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.*;
import java.math.BigDecimal;

public class PdfUtil {
    private static final Logger logger = LoggerFactory.getLogger(PdfUtil.class);

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 9, Font.BOLD);

    private static final Color HEADER_BG_COLOR = new Color(240, 240, 240);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);

    private PdfUtil() {}

    public static byte[] generateDdtPdf(Ddt ddt, ConfigAzienda azienda) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header with company info
            addCompanyHeader(document, azienda);

            // DDT title and number
            Paragraph title = new Paragraph("DOCUMENTO DI TRASPORTO", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(20);
            document.add(title);

            Paragraph ddtNumber = new Paragraph(
                    String.format("N. %d del %s", ddt.getNumero(), FormatUtil.formatDate(ddt.getDataDocumento())),
                    HEADER_FONT);
            ddtNumber.setAlignment(Element.ALIGN_CENTER);
            ddtNumber.setSpacingAfter(20);
            document.add(ddtNumber);

            // Recipient info
            addRecipientSection(document, ddt);

            // Products table
            addProductsTable(document, ddt);

            // Transport info
            addTransportSection(document, ddt);

            // Footer with signatures
            addSignaturesSection(document);

            document.close();
            logger.info("DDT PDF generated successfully for DDT {}/{}", ddt.getNumero(), ddt.getAnno());

        } catch (DocumentException e) {
            logger.error("Error generating DDT PDF: {}", e.getMessage());
        }

        return baos.toByteArray();
    }

    private static void addCompanyHeader(Document document, ConfigAzienda azienda) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 2});

        // Logo cell (placeholder)
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        if (azienda != null && azienda.getLogo() != null) {
            try {
                Image logo = Image.getInstance(azienda.getLogo());
                logo.scaleToFit(100, 60);
                logoCell.addElement(logo);
            } catch (Exception e) {
                logoCell.addElement(new Paragraph(""));
            }
        }
        headerTable.addCell(logoCell);

        // Company info cell
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        if (azienda != null) {
            Paragraph companyName = new Paragraph(azienda.getNome() != null ? azienda.getNome() : "", HEADER_FONT);
            companyName.setAlignment(Element.ALIGN_RIGHT);
            infoCell.addElement(companyName);

            if (azienda.getIndirizzo() != null) {
                Paragraph address = new Paragraph(azienda.getIndirizzoCompleto(), SMALL_FONT);
                address.setAlignment(Element.ALIGN_RIGHT);
                infoCell.addElement(address);
            }

            if (azienda.getPiva() != null) {
                Paragraph piva = new Paragraph("P.IVA: " + azienda.getPiva(), SMALL_FONT);
                piva.setAlignment(Element.ALIGN_RIGHT);
                infoCell.addElement(piva);
            }

            if (azienda.getTelefono() != null || azienda.getEmail() != null) {
                String contact = "";
                if (azienda.getTelefono() != null) contact += "Tel: " + azienda.getTelefono();
                if (azienda.getEmail() != null) {
                    if (!contact.isEmpty()) contact += " - ";
                    contact += "Email: " + azienda.getEmail();
                }
                Paragraph contactPara = new Paragraph(contact, SMALL_FONT);
                contactPara.setAlignment(Element.ALIGN_RIGHT);
                infoCell.addElement(contactPara);
            }
        }
        headerTable.addCell(infoCell);

        document.add(headerTable);
    }

    private static void addRecipientSection(Document document, Ddt ddt) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(15);

        // Destinatario
        PdfPCell destCell = new PdfPCell();
        destCell.setBorder(Rectangle.BOX);
        destCell.setBorderColor(BORDER_COLOR);
        destCell.setPadding(8);

        Paragraph destTitle = new Paragraph("DESTINATARIO", BOLD_FONT);
        destCell.addElement(destTitle);

        Destinatario dest = ddt.getDestinatario();
        if (dest != null) {
            destCell.addElement(new Paragraph(dest.getRagioneSociale(), NORMAL_FONT));
            if (dest.getIndirizzo() != null) {
                destCell.addElement(new Paragraph(dest.getIndirizzoCompleto(), SMALL_FONT));
            }
            if (dest.getPiva() != null) {
                destCell.addElement(new Paragraph("P.IVA: " + dest.getPiva(), SMALL_FONT));
            }
        }
        table.addCell(destCell);

        // Destinazione diversa
        PdfPCell destDivCell = new PdfPCell();
        destDivCell.setBorder(Rectangle.BOX);
        destDivCell.setBorderColor(BORDER_COLOR);
        destDivCell.setPadding(8);

        Paragraph destDivTitle = new Paragraph("DESTINAZIONE (se diversa)", BOLD_FONT);
        destDivCell.addElement(destDivTitle);

        if (ddt.getDestinazioneDiversa() != null && !ddt.getDestinazioneDiversa().isEmpty()) {
            destDivCell.addElement(new Paragraph(ddt.getDestinazioneDiversa(), NORMAL_FONT));
        }
        table.addCell(destDivCell);

        document.add(table);
    }

    private static void addProductsTable(Document document, Ddt ddt) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 4, 1.5f, 1.5f, 2});
        table.setSpacingAfter(15);

        // Header
        addTableHeader(table, "N.");
        addTableHeader(table, "Descrizione");
        addTableHeader(table, "Q.ta");
        addTableHeader(table, "U.M.");
        addTableHeader(table, "Prezzo");

        // Rows
        int rowNum = 1;
        for (DdtRiga riga : ddt.getRighe()) {
            addTableCell(table, String.valueOf(rowNum++));
            addTableCell(table, riga.getDescrizione() != null ? riga.getDescrizione() :
                    (riga.getProdotto() != null ? riga.getProdotto().getNome() : ""));
            addTableCell(table, FormatUtil.formatQuantity(riga.getQuantita()));
            addTableCell(table, riga.getUnitaMisura() != null ? riga.getUnitaMisura() : "");
            addTableCell(table, riga.getPrezzoUnitario() != null ?
                    FormatUtil.formatCurrencyNoSymbol(riga.getPrezzoUnitario()) : "");
        }

        // Add empty rows to fill space
        for (int i = ddt.getRighe().size(); i < 15; i++) {
            addTableCell(table, "");
            addTableCell(table, "");
            addTableCell(table, "");
            addTableCell(table, "");
            addTableCell(table, "");
        }

        document.add(table);
    }

    private static void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(HEADER_BG_COLOR);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private static void addTransportSection(Document document, Ddt ddt) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addLabelValueCell(table, "Causale trasporto:", ddt.getCausaleTrasporto());
        addLabelValueCell(table, "Aspetto beni:", ddt.getAspettoBeni());
        addLabelValueCell(table, "Colli:", ddt.getColli() > 0 ? String.valueOf(ddt.getColli()) : "");
        addLabelValueCell(table, "Peso (Kg):", ddt.getPesoKg() != null ?
                FormatUtil.formatQuantity(ddt.getPesoKg()) : "");

        addLabelValueCell(table, "Porto:", ddt.getPorto());
        addLabelValueCell(table, "Vettore:", ddt.getVettore());
        addLabelValueCell(table, "Data trasporto:",
                ddt.getDataTrasporto() != null ? FormatUtil.formatDateTime(ddt.getDataTrasporto()) : "");
        addLabelValueCell(table, "", "");

        document.add(table);

        if (ddt.getNote() != null && !ddt.getNote().isEmpty()) {
            Paragraph notes = new Paragraph("Note: " + ddt.getNote(), SMALL_FONT);
            notes.setSpacingAfter(10);
            document.add(notes);
        }
    }

    private static void addLabelValueCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(4);

        if (!label.isEmpty()) {
            cell.addElement(new Paragraph(label, BOLD_FONT));
        }
        cell.addElement(new Paragraph(value != null ? value : "", NORMAL_FONT));
        table.addCell(cell);
    }

    private static void addSignaturesSection(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(30);

        String[] labels = {"Firma conducente", "Firma vettore", "Firma destinatario"};
        for (String label : labels) {
            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setMinimumHeight(60);

            Paragraph p = new Paragraph(label, SMALL_FONT);
            cell.addElement(p);

            Paragraph line = new Paragraph("_______________________", SMALL_FONT);
            line.setSpacingBefore(30);
            cell.addElement(line);

            table.addCell(cell);
        }

        document.add(table);
    }

    public static void savePdfToFile(byte[] pdfData, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfData);
        }
        logger.info("PDF saved to: {}", filePath);
    }
}
