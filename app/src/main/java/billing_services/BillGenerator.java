package billing_services;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import BeanClasses.Bill;
import BeanClasses.Client;
import BeanClasses.Transection;
import db_services.DBServices;
import utils.BillUtils;

public class BillGenerator {
    private Document document;
    public static void generateBill(Bill bill) throws Exception {

        BillGenerator generator=new BillGenerator();
        BillUtils utils=new BillUtils(bill);

        generator.initializeDocument(utils.getFile(bill.getBill_year()));

        generator.addHearder();

        generator.addBillDetails(utils.getBillDetails());

        Client client=DBServices.getClient(bill.getClient_id());
        generator.addClientDetails(client.getName(),client.getAddress());

        generator.addBillPertucilerHeader();

        String previous=DBServices.getPreviousBalance(bill.getClient_id(),bill.getFrom_date())+"";
        generator.addParticulers(previous,utils.getParticulars(),bill);

        generator.addFooterInfo();

        generator.closeDocument();
    }

    private void closeDocument() {
        document.close();
    }

    private void addFooterInfo() throws DocumentException{
        Paragraph paragraph=new Paragraph();
        paragraph.setIndentationLeft(50);

        paragraph.add(Chunk.NEWLINE);

        Chunk chunk=new Chunk("PLEASE ISSUE CHEQUE IN THE NAME OF 'SHUBHAM KUMAR'");
        Font font=new Font();
        font.setSize(11);
        font.setStyle(Font.BOLD);
        chunk.setFont(font);
        paragraph.add(chunk);

        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);

        font.setSize(11);
        Chunk chunk2=new Chunk("MY BANK ACCOUNT DETAILS IS AS FOLLOWS:-");
        chunk2.setFont(font);
        paragraph.add(chunk2);

        LineSeparator separator=new LineSeparator();
        separator.setPercentage(50f);
        separator.setAlignment(LineSeparator.ALIGN_LEFT);
        separator.setOffset(-3f);
        separator.setLineWidth(0.5f);
        paragraph.add(separator);

        paragraph.add(Chunk.NEWLINE);

        Font font2=new Font();
        font2.setSize(11);
        font.setSize(10);
        Chunk chunk3=new Chunk("A/C NO:- ");
        chunk3.setFont(font2);
        paragraph.add(chunk3);

        Chunk chunk4=new Chunk("218810100027989");
        chunk4.setFont(font);
        paragraph.add(chunk4);

        paragraph.add(Chunk.NEWLINE);

        Chunk chunk5=new Chunk("IFSC CODE:- ");
        chunk5.setFont(font2);
        paragraph.add(chunk5);

        Chunk chunk6=new Chunk("ANDB0002188");
        chunk6.setFont(font);
        paragraph.add(chunk6);

        paragraph.add(Chunk.NEWLINE);

        Chunk chunk7=new Chunk("BANK NAME:- ");
        chunk7.setFont(font2);
        paragraph.add(chunk7);

        Chunk chunk8=new Chunk("ANDHRA BANK");
        chunk8.setFont(font);
        paragraph.add(chunk8);

        paragraph.add(Chunk.NEWLINE);

        Chunk chunk9=new Chunk("BANK ADDRESS:-  ");
        chunk9.setFont(font2);
        paragraph.add(chunk9);

        Chunk chunk10=new Chunk("SHALIMAR BAGH, DELHI - 110088");
        chunk10.setFont(font);
        paragraph.add(chunk10);

        paragraph.add(Chunk.NEWLINE);

        Chunk chunk11=new Chunk("A/C HOLDER NAME:- ");
        chunk11.setFont(font2);
        paragraph.add(chunk11);

        Chunk chunk12=new Chunk("SHUBHAM KUMAR");
        chunk12.setFont(font);
        paragraph.add(chunk12);

        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);

        document.add(paragraph);

        Paragraph paragraph2=new Paragraph();
        paragraph2.setAlignment(Element.ALIGN_BOTTOM);
        paragraph2.setIndentationLeft(400);
        Chunk chunk13=new Chunk("SIGNATURE");
        chunk13.setFont(font);
        paragraph2.add(chunk13);

        document.add(paragraph2);

    }

    private void addParticulers(String previous_balance,Set<Transection> particulars,Bill bill) throws Exception {
        Paragraph paragraph=new Paragraph();

        PdfPTable table = new PdfPTable(new float[] { 3, 1 });

        PdfPCell empty=new PdfPCell(new Phrase(" "));
        empty.setHorizontalAlignment(Element.ALIGN_LEFT);
        empty.setBorder(0);
        empty.setVerticalAlignment(Element.ALIGN_CENTER);
        table.addCell(empty);
        table.addCell(empty);

        Font font=new Font();
        font.setSize(10);
        if(Integer.parseInt(previous_balance)>0) {
            Phrase phrase=new Phrase("Opening Balance");
            phrase.setFont(font);
            PdfPCell perticular_for_previous_balance = new PdfPCell(phrase);
            perticular_for_previous_balance.setHorizontalAlignment(Element.ALIGN_LEFT);
            perticular_for_previous_balance.setBorder(0);
            perticular_for_previous_balance.setVerticalAlignment(Element.ALIGN_CENTER);
            perticular_for_previous_balance.setFixedHeight(30);
            table.addCell(perticular_for_previous_balance);

            Phrase phrase1=new Phrase(previous_balance);
            phrase.setFont(font);
            PdfPCell perticular_for_previous_amount = new PdfPCell(phrase1);
            perticular_for_previous_amount.setHorizontalAlignment(Element.ALIGN_CENTER);
            perticular_for_previous_amount.setVerticalAlignment(Element.ALIGN_CENTER);
            perticular_for_previous_amount.setBorder(0);
            perticular_for_previous_amount.setFixedHeight(30);
            table.addCell(perticular_for_previous_amount);
        }
        Integer total=Integer.parseInt(previous_balance);

        for (Transection transection:particulars){
            Phrase phrase=new Phrase(transection.getDesc());
            phrase.setFont(font);
            PdfPCell perticular=new PdfPCell(phrase);
            perticular.setHorizontalAlignment(Element.ALIGN_LEFT);
            perticular.setBorder(0);
            perticular.setVerticalAlignment(Element.ALIGN_CENTER);
            perticular.setFixedHeight(30);
            table.addCell(perticular);

            Phrase amount_phrase=new Phrase();
            amount_phrase.setFont(font);
            if(transection.getTransecType().equals("Credit")){
                amount_phrase.add("- "+transection.getAmount()+"");
                total-=transection.getAmount();
            }else{
                amount_phrase.add(transection.getAmount()+"");
                total+=transection.getAmount();
            }

            PdfPCell amount=new PdfPCell(amount_phrase);
            amount.setHorizontalAlignment(Element.ALIGN_CENTER);
            amount.setVerticalAlignment(Element.ALIGN_CENTER);
            amount.setBorder(0);
            amount.setFixedHeight(30);
            table.addCell(amount);

            DBServices.addBillDetailsToTransection(transection,bill);

        }

        System.out.println("Total Balance "+total);
        table.addCell(empty);

        table.addCell(empty);

        Phrase phrase1=new Phrase("TOTAL");
        phrase1.setFont(font);
        PdfPCell total_lbl_cell=new PdfPCell(phrase1);
        total_lbl_cell.setBorderWidth(1);
        table.addCell(total_lbl_cell);
        total_lbl_cell.setFixedHeight(18);



        Phrase phrase=new Phrase(total+"");
        phrase.setFont(font);
        PdfPCell total_value=new PdfPCell(phrase);
        total_value.setBorderWidth(1);
        total_value.setFixedHeight(18);

        total_value.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(total_value);

        table.setHeaderRows(1);

        paragraph.add(table);

        document.add(paragraph);

    }

    private void addHearder() throws DocumentException {
        Paragraph paragraph=new Paragraph(20);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);

        Font headerfont=new Font(FontFamily.HELVETICA);
        headerfont.setStyle(Font.BOLD);
        headerfont.setColor(31,154,199);
        headerfont.setSize(14);

        Chunk chunk=new Chunk("SHUBHAM  KUMAR TAX CONSULTANCY");
        chunk.setFont(headerfont);
        paragraph.add(chunk);

        paragraph.add(Chunk.NEWLINE);

        Chunk chunk2=new Chunk("A-11, KAMAL VHIAR, BURARI, DELHI-110084, INDIA");
        chunk2.setFont(headerfont);
        paragraph.add(chunk2);

        paragraph.add(Chunk.NEWLINE);

        Font panfont=new Font(FontFamily.HELVETICA);
        panfont.setStyle(Font.BOLD);
        panfont.setSize(14);

        Chunk chunk3=new Chunk("PAN NO:- EKIPK1251B");
        chunk3.setFont(panfont);
        paragraph.add(chunk3);

        document.add(paragraph);

        LineSeparator separator=new LineSeparator();
        separator.setPercentage(80f);
        separator.setAlignment(LineSeparator.ALIGN_CENTER);

        document.add(Chunk.NEWLINE);

        document.add(separator);

    }

    private void addBillDetails(String details) throws DocumentException{
        Font font=new Font();
        font.setSize(12);
        Paragraph paragraph=new Paragraph();
        paragraph.setFont(font);
        paragraph.add(details);
        paragraph.setAlignment(Paragraph.ALIGN_RIGHT);
        paragraph.setIndentationRight(50);
        paragraph.add(Chunk.NEWLINE);
        document.add(paragraph);
    }

    private void addClientDetails(String clientName,String address) throws DocumentException{
        Font font=new Font();
        font.setSize(10);
        Paragraph paragraph=new Paragraph();
        paragraph.setIndentationLeft(50);
        paragraph.add(Chunk.NEWLINE);
        Chunk chunk=new Chunk("To");
        chunk.setFont(font);
        paragraph.add(chunk);
        paragraph.add(Chunk.NEWLINE);
        Chunk chunk1=new Chunk(clientName);
        chunk1.setFont(font);
        paragraph.add(chunk1);
        paragraph.add(Chunk.NEWLINE);
        Chunk chunk2=new Chunk(address);
        chunk2.setFont(font);
        paragraph.add(chunk2);
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(Chunk.NEWLINE);
        document.add(paragraph);
    }

    private void initializeDocument(File file) throws FileNotFoundException, DocumentException {
        this.document=new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
    }
    private void addBillPertucilerHeader() throws DocumentException{
        Paragraph paragraph=new Paragraph();
        paragraph.setIndentationLeft(50);
        Font font=new Font();
        font.setStyle(Font.BOLD);
        font.setSize(10);
        Chunk chunk=new Chunk("Bill for Professional Fee");
        chunk.setFont(font);
        paragraph.add(chunk);


        LineSeparator separator=new LineSeparator();
        separator.setPercentage(90f);
        separator.setAlignment(LineSeparator.ALIGN_LEFT);
        separator.setOffset(-4f);
        paragraph.add(separator);

        paragraph.add(Chunk.NEWLINE);

        Chunk ch=new Chunk();
        ch.append("PERTICULERS                                                                                            AMOUNT(Rs)");
        ch.setFont(font);
        paragraph.add(ch);

        LineSeparator separator1=new LineSeparator();
        separator1.setPercentage(90f);
        separator1.setAlignment(LineSeparator.ALIGN_LEFT);
        separator1.setOffset(-4f);
        paragraph.add(separator1);

        document.add(paragraph);
    }
}
