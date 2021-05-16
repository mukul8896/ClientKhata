package services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.mukul.companyAccounts.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import driveBackup.GoogleDriveHandler;
import modals.Bill;
import modals.Client;
import modals.Transection;
import dbServices.BillDbServices;
import dbServices.ClientDbServices;
import dbServices.TransectionDbServices;
import utils.BillUtils;
import utils.ProjectUtils;

public class BillGenerationServices {
    private Document document;

    public void generateBill(Bill bill, Context context) throws Exception {
        BillUtils utils = new BillUtils(bill);
        initializeDocument(utils.getFile(bill.getBill_year()));

        addHearder(context);

        addBillDetails(utils.getBillDetails());

        Client client = ClientDbServices.getClient(bill.getClient_id());
        addClientDetails(client.getName(), client.getAddress());


        String previous = BillDbServices.getPreviousBalance(bill.getClient_id(), bill.getFrom_date()) + "";

        String bill_detail=bill.getBill_year() + " | Bill No-" + bill.getBill_no();
        addParticulers(previous, BillDbServices.getBillParticulars(bill.getClient_id(), bill.getFrom_date(), bill.getTo_date()), bill_detail);

        addFooterInfo(context);

        closeDocument();
    }

    private void closeDocument() {
        document.close();
    }

    private void addFooterInfo(Context context) throws DocumentException {
        Paragraph paragraph = new Paragraph();
        paragraph.setIndentationLeft(50);

        paragraph.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(new float[]{3, 2});
        table.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
        table.setWidthPercentage(90);
        Font font_head = new Font();
        font_head.setSize(10);
        font_head.setStyle(Font.BOLD);

        Phrase phrase_bill_heading = new Phrase("PLEASE ISSUE CHEQUE IN THE NAME OF 'RACHIT BHATIA'",font_head);
        PdfPCell cell_bill_heading = new PdfPCell(phrase_bill_heading);
        cell_bill_heading.setBorderWidth(0);
        cell_bill_heading.setFixedHeight(20);
        cell_bill_heading.setColspan(2);
        table.addCell(cell_bill_heading);

        PdfPCell empty = new PdfPCell(new Phrase(""));
        empty.setBorderWidth(0);
        empty.setFixedHeight(20);
        empty.setColspan(2);
        table.addCell(empty);

        Font font_bank_detail_header = new Font();
        font_bank_detail_header.setStyle(Font.BOLD|Font.UNDERLINE);
        font_bank_detail_header.setSize(10);
        Phrase phrase_bank_detail_header = new Phrase("MY BANK ACCOUNT DETAILS IS AS FOLLOWS:-",font_bank_detail_header);
        PdfPCell cell_bank_detail_header = new PdfPCell(phrase_bank_detail_header);
        cell_bank_detail_header.setBorderWidth(0);
        table.addCell(cell_bank_detail_header);

        Font font = new Font();
        font.setStyle(Font.BOLD);
        font.setSize(10);
        Font font1=new Font();
        font1.setSize(10);

        PdfPCell cell_empty = new PdfPCell(new Phrase(""));
        cell_empty.setBorderWidth(0);
        table.addCell(cell_empty);

        List<Chunk> list=new ArrayList<>();
        Chunk chunk=new Chunk("A/C NO:- ",font1);
        Chunk chunk1=new Chunk("50100384065528",font);
        list.add(chunk);list.add(chunk1);
        Phrase phrase_acount_details=new Phrase();
        phrase_acount_details.addAll(list);
        PdfPCell cell_account_no = new PdfPCell(phrase_acount_details);
        cell_account_no.setBorderWidth(0);
        cell_account_no.setColspan(2);
        table.addCell(cell_account_no);

        Chunk chunk_ifsc1=new Chunk("IFSC CODE:- ",font1);
        Chunk chunk_ifsc2=new Chunk("HDFC0004074",font);
        Phrase phrase_ifsc=new Phrase();
        phrase_ifsc.addAll(Arrays.asList(chunk_ifsc1,chunk_ifsc2));
        PdfPCell cell_ifsc = new PdfPCell(phrase_ifsc);
        cell_ifsc.setBorderWidth(0);
        cell_ifsc.setColspan(2);
        table.addCell(cell_ifsc);

        Chunk chunk_bank1=new Chunk("BANK NAME:- ",font1);
        Chunk chunk_bank2=new Chunk("HDFC BANK",font);
        Phrase phrase_bank=new Phrase();
        phrase_bank.addAll(Arrays.asList(chunk_bank1,chunk_bank2));
        PdfPCell cell_bank = new PdfPCell(phrase_bank);
        cell_bank.setBorderWidth(0);
        cell_bank.setColspan(2);
        table.addCell(cell_bank);


        Chunk chunk_bank_address1=new Chunk("BANK ADDRESS:-  ",font1);
        Chunk chunk_bank_address2=new Chunk("DERAWAL NAGAR, DELHI - 110009",font);
        Phrase phrase_bank_address=new Phrase();
        phrase_bank_address.addAll(Arrays.asList(chunk_bank_address1,chunk_bank_address2));
        PdfPCell cell_bank_address = new PdfPCell(phrase_bank_address);
        cell_bank_address.setBorderWidth(0);
        cell_bank_address.setColspan(2);
        table.addCell(cell_bank_address);


        Chunk chunk_ac_holder1=new Chunk("A/C HOLDER NAME:- ",font1);
        Chunk chunk_ac_holder2=new Chunk("RACHIT BHATIA",font);
        Phrase phrase_ac_dolder=new Phrase();
        phrase_ac_dolder.addAll(Arrays.asList(chunk_ac_holder1,chunk_ac_holder2));
        PdfPCell cell_ac_holder = new PdfPCell(phrase_ac_dolder);
        cell_ac_holder.setBorderWidth(0);
        table.addCell(cell_ac_holder);


        Chunk signature=new Chunk("SIGNATURE",font);
        Phrase phrase_signature=new Phrase(signature);
        PdfPCell cell_signature = new PdfPCell(phrase_signature);
        cell_signature.setBorderWidth(0);
        cell_signature.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell_signature);

        paragraph.add(table);

        document.add(paragraph);

    }

    private void addParticulers(String previous_balance, List<Transection> particulars, String billdetails) throws Exception {
        Paragraph paragraph = new Paragraph();
        paragraph.setIndentationLeft(50);
        PdfPTable table = new PdfPTable(new float[]{3, 1});
        table.setWidthPercentage(75);
        table.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
        table.setHeaderRows(1);

        Font font_head = new Font();
        font_head.setSize(12);
        font_head.setStyle(Font.BOLD|Font.UNDERLINE);

        Phrase phrase_bill_heading = new Phrase("Bill for Professional Fee",font_head);
        PdfPCell cell_bill_heading = new PdfPCell(phrase_bill_heading);
        cell_bill_heading.setBorderWidth(0);
        cell_bill_heading.setFixedHeight(20);
        cell_bill_heading.setVerticalAlignment(Element.ALIGN_TOP);
        cell_bill_heading.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell_bill_heading.setColspan(2);
        table.addCell(cell_bill_heading);


        Font new_font = new Font();
        new_font.setStyle(Font.BOLD);
        new_font.setSize(10);
        Phrase phrase_perticular = new Phrase("PARTICULARS",new_font);
        phrase_perticular.setFont(new_font);
        PdfPCell cell_perticular = new PdfPCell(phrase_perticular);
        cell_perticular.setBorderWidthLeft(0);
        cell_perticular.setBorderWidthRight(0);
        cell_perticular.setBorderWidthTop(0.8f);
        cell_perticular.setBorderWidthBottom(0.8f);
        table.addCell(cell_perticular);
        cell_perticular.setFixedHeight(18);


        Phrase phrase_total = new Phrase("AMOUNT(Rs)",new_font);
        phrase_total.setFont(new_font);
        PdfPCell cell_amount = new PdfPCell(phrase_total);
        cell_amount.setBorderWidthLeft(0);
        cell_amount.setBorderWidthRight(0);
        cell_amount.setBorderWidthTop(0.8f);
        cell_amount.setBorderWidthBottom(0.8f);
        cell_amount.setFixedHeight(18);
        cell_amount.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell_amount);



        PdfPCell empty = new PdfPCell(new Phrase(" "));
        empty.setBorder(0);
        table.addCell(empty);
        table.addCell(empty);


        Font font = new Font();
        font.setSize(10);
        if (Integer.parseInt(previous_balance) > 0) {
            Phrase phrase = new Phrase("OPENING BALANCE",font);
            PdfPCell perticular_for_previous_balance = new PdfPCell(phrase);
            perticular_for_previous_balance.setHorizontalAlignment(Element.ALIGN_LEFT);
            perticular_for_previous_balance.setBorder(0);
            perticular_for_previous_balance.setVerticalAlignment(Element.ALIGN_CENTER);
            perticular_for_previous_balance.setFixedHeight(30);
            table.addCell(perticular_for_previous_balance);

            Phrase phrase1 = new Phrase(previous_balance,font);
            PdfPCell perticular_for_previous_amount = new PdfPCell(phrase1);
            perticular_for_previous_amount.setHorizontalAlignment(Element.ALIGN_CENTER);
            perticular_for_previous_amount.setVerticalAlignment(Element.ALIGN_CENTER);
            perticular_for_previous_amount.setBorder(0);
            perticular_for_previous_amount.setFixedHeight(30);
            table.addCell(perticular_for_previous_amount);
        }
        Integer total = Integer.parseInt(previous_balance);
        Collections.reverse(particulars);
        for (Transection transection : particulars) {
            Phrase phrase = new Phrase(transection.getDesc(),font);
            phrase.setFont(font);
            PdfPCell perticular = new PdfPCell(phrase);
            perticular.setHorizontalAlignment(Element.ALIGN_LEFT);
            perticular.setBorder(0);
            perticular.setVerticalAlignment(Element.ALIGN_CENTER);
            perticular.setFixedHeight(30);
            table.addCell(perticular);

            Phrase amount_phrase = null;
            if (transection.getTransecType().equals("Credit")) {
                amount_phrase=new Phrase("- " + transection.getAmount() + "",font);
                total -= transection.getAmount();
            } else {
                amount_phrase=new Phrase(transection.getAmount() + "",font);
                total += transection.getAmount();
            }
            PdfPCell amount = new PdfPCell(amount_phrase);
            amount.setHorizontalAlignment(Element.ALIGN_CENTER);
            amount.setVerticalAlignment(Element.ALIGN_CENTER);
            amount.setBorder(0);
            amount.setFixedHeight(30);
            table.addCell(amount);
            TransectionDbServices.addBillDetailsToTransection(transection, billdetails);
        }

        System.out.println("Total Balance " + total);
        table.addCell(empty);

        table.addCell(empty);

        Phrase phrase1 = new Phrase("TOTAL",new_font);
        phrase1.setFont(font);
        PdfPCell total_lbl_cell = new PdfPCell(phrase1);
        total_lbl_cell.setBorderWidthLeft(0);
        total_lbl_cell.setBorderWidthRight(0);
        total_lbl_cell.setBorderWidthTop(0.8f);
        total_lbl_cell.setBorderWidthBottom(0.8f);
        table.addCell(total_lbl_cell);
        total_lbl_cell.setFixedHeight(18);


        Phrase phrase = new Phrase(total + "",new_font);
        phrase.setFont(font);
        PdfPCell total_value = new PdfPCell(phrase);
        total_value.setBorderWidthLeft(0);
        total_value.setBorderWidthRight(0);
        total_value.setBorderWidthTop(0.8f);
        total_value.setBorderWidthBottom(0.8f);
        total_value.setFixedHeight(18);
        total_value.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(total_value);

        PdfPCell bottom_line = new PdfPCell(new Phrase(""));
        bottom_line.setBorderWidthBottom(0.8f);
        bottom_line.setBorderWidthLeft(0);
        bottom_line.setBorderWidthRight(0);
        bottom_line.setBorderWidthTop(0);
        bottom_line.setFixedHeight(3);
        bottom_line.setColspan(2);
        table.addCell(bottom_line);

        paragraph.add(table);

        document.add(paragraph);
    }

    private void addHearder(Context context) throws DocumentException, IOException {
        Paragraph paragraph = new Paragraph(20);
        paragraph.setIndentationLeft(50);

        PdfPTable table = new PdfPTable(new float[]{1, 3});
        table.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
        table.setWidthPercentage(90);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ca_logo);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitMapData = stream.toByteArray();
        Image img = Image.getInstance(bitMapData);
        PdfPCell ca_logo_cell = new PdfPCell(img,false);
        ca_logo_cell.setFixedHeight(80);
        ca_logo_cell.setBorderWidth(0);
        ca_logo_cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        ca_logo_cell.setVerticalAlignment(Element.ALIGN_CENTER);
        ca_logo_cell.setRowspan(4);
        table.addCell(ca_logo_cell);

        Font headerfont = new Font(FontFamily.HELVETICA);
        headerfont.setStyle(Font.BOLD);
        headerfont.setColor(0, 112, 192);
        headerfont.setSize(12);

        Phrase phrase_bill_heading = new Phrase("Rachit Bhatia & Associates",headerfont);
        PdfPCell cell_bill_heading = new PdfPCell(phrase_bill_heading);
        cell_bill_heading.setBorderWidth(0);
        cell_bill_heading.setFixedHeight(20);
        cell_bill_heading.setColspan(2);
        cell_bill_heading.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell_bill_heading);

        Phrase phrase_charted_accountant = new Phrase("Chartered Accountants",headerfont);
        PdfPCell cell_charted_accountant = new PdfPCell(phrase_charted_accountant);
        cell_charted_accountant.setBorderWidth(0);
        cell_charted_accountant.setFixedHeight(20);
        cell_charted_accountant.setColspan(2);
        cell_charted_accountant.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell_charted_accountant);

        Phrase phrase_address = new Phrase("23-A Gujrawala Town Part-2, Delhi-110009, INDIA",headerfont);
        PdfPCell cell_address = new PdfPCell(phrase_address);
        cell_address.setBorderWidth(0);
        cell_address.setFixedHeight(20);
        cell_address.setColspan(2);
        cell_address.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell_address);

        Font panfont = new Font(FontFamily.HELVETICA);
        panfont.setStyle(Font.BOLD);
        panfont.setSize(10);

        Phrase phrase_pan = new Phrase("PAN NO:- APPPB8231A",panfont);
        PdfPCell cell_pan = new PdfPCell(phrase_pan);
        cell_pan.setBorderWidth(0);
        cell_pan.setFixedHeight(20);
        cell_pan.setColspan(2);
        cell_pan.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell_pan);

        paragraph.add(table);

        document.add(paragraph);

        LineSeparator separator = new LineSeparator();
        separator.setPercentage(80f);
        separator.setAlignment(LineSeparator.ALIGN_CENTER);

        document.add(Chunk.NEWLINE);

        document.add(separator);

    }

    private void addBillDetails(String details) throws DocumentException {
        Font font = new Font();
        font.setSize(12);
        Paragraph paragraph = new Paragraph();
        paragraph.setFont(font);
        paragraph.add(details);
        paragraph.setAlignment(Paragraph.ALIGN_RIGHT);
        paragraph.setIndentationRight(50);
        paragraph.add(Chunk.NEWLINE);
        document.add(paragraph);
    }

    private void addClientDetails(String clientName, String address) throws DocumentException {
        Font font = new Font();
        font.setSize(10);
        font.setStyle(Font.BOLD);

        Paragraph paragraph = new Paragraph();
        paragraph.setIndentationLeft(50);
        paragraph.add(Chunk.NEWLINE);
        Chunk chunk = new Chunk("To");
        chunk.setFont(font);
        paragraph.add(chunk);
        paragraph.add(Chunk.NEWLINE);
        Chunk chunk1 = new Chunk(clientName);
        chunk1.setFont(font);
        paragraph.add(chunk1);
        paragraph.add(Chunk.NEWLINE);
        Chunk chunk2 = new Chunk(address);
        chunk2.setFont(font);
        paragraph.add(chunk2);
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);
        document.add(paragraph);
    }

    private void initializeDocument(File file) throws FileNotFoundException, DocumentException {
        this.document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
    }
}
