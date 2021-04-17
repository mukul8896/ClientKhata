package services;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import modals.Client;
import modals.Transection;
import utils.ProjectUtils;

public class AnnualSummeryServices {
    private Document document;
    private List<Client> clientList;
    private List<Transection> transectionList;
    public AnnualSummeryServices(List<Client> clinetList, List<Transection> transectionlist) {
        this.clientList=clinetList;
        this.transectionList=transectionlist;
    }

    public File generateYearReport(String year,String file_type) throws IOException, DocumentException {
        File folder = new File(ProjectUtils.getBillFolders().getPath() + File.separator + year);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File report_file = null;
        if(file_type.equals("PDF")) {
            report_file = new File(folder, "Report_" + year.split("-")[0] + "_" + year.split("-")[1] + ".pdf");
            if(initializePdfDocument(report_file)){
                addHearder();
                addClientListTable();
                document.close();
            }
        }else {
            report_file = new File(folder, "Report_" + year.split("-")[0] + "_" + year.split("-")[1] + ".xls");
            writeExcelFile(report_file);
        }
        return report_file;
    }

    public void writeExcelFile(File report_file) throws IOException {
        Workbook workbook=new HSSFWorkbook();
        Sheet sheet=workbook.createSheet("NewSheet");
        Row row=sheet.createRow(0);

        HSSFFont font= (HSSFFont) workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        HSSFCellStyle header_style= (HSSFCellStyle) workbook.createCellStyle();
        header_style.setAlignment(CellStyle.ALIGN_CENTER);
        header_style.setBorderBottom(CellStyle.BORDER_THIN);
        header_style.setBorderLeft(CellStyle.BORDER_THIN);
        header_style.setBorderRight(CellStyle.BORDER_THIN);
        header_style.setBorderTop(CellStyle.BORDER_THIN);
        header_style.setFont(font);
        header_style.setWrapText(true);

        Cell cell=row.createCell(0);
        cell.setCellStyle(header_style);
        cell.setCellValue("S.No.");

        Cell name_head=row.createCell(1);
        name_head.setCellStyle(header_style);
        name_head.setCellValue("Name");

        Cell debit_head=row.createCell(2);
        debit_head.setCellStyle(header_style);
        debit_head.setCellValue("Credit");

        Cell credit_head=row.createCell(3);
        credit_head.setCellStyle(header_style);
        credit_head.setCellValue("Debit");

        Cell due_head=row.createCell(4);
        due_head.setCellStyle(header_style);
        due_head.setCellValue("Due");

        HSSFCellStyle style= (HSSFCellStyle) workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        for(Client client:clientList){

            Row client_row=sheet.createRow(clientList.indexOf(client)+1);
            Cell serial_no=client_row.createCell(0);
            serial_no.setCellStyle(style);
            serial_no.setCellValue((clientList.indexOf(client)+1)+"");

            HSSFCellStyle name_style= (HSSFCellStyle) workbook.createCellStyle();
            style.setAlignment(CellStyle.ALIGN_LEFT);
            Cell name=client_row.createCell(1);
            name.setCellStyle(name_style);
            name.setCellValue(client.getName());

            int total_debit=0;
            int total_credit=0;
            for(Transection transection:transectionList){
                if(transection.getTransecType().equals("Credit") && transection.getClientId().equals(client.getId()))
                    total_credit+=transection.getAmount();
                if(transection.getTransecType().equals("Debit") && transection.getClientId().equals(client.getId()))
                    total_debit+=transection.getAmount();
            }

            style.setAlignment(CellStyle.ALIGN_CENTER);
            Cell debit=client_row.createCell(2);
            debit.setCellStyle(style);
            debit.setCellValue(total_debit);

            Cell credit=client_row.createCell(3);
            credit.setCellStyle(style);
            credit.setCellValue(total_credit);

            Cell due=client_row.createCell(4);
            due.setCellStyle(style);
            due.setCellValue((total_debit-total_credit)+"");
        }

        int total_debit=0;
        int total_credit=0;
        for(Transection transection:transectionList){
            if(transection.getTransecType().equals("Credit"))
                total_credit+=transection.getAmount();
            if(transection.getTransecType().equals("Debit"))
                total_debit+=transection.getAmount();
        }

        Row final_row=sheet.createRow(clientList.size()+1);
        Cell total=final_row.createCell(1);
        total.setCellStyle(style);
        total.setCellValue("Total");

        Cell totalDebit=final_row.createCell(2);
        totalDebit.setCellStyle(style);
        totalDebit.setCellValue(total_debit+"");

        Cell totalCredit=final_row.createCell(3);
        totalCredit.setCellStyle(style);
        totalCredit.setCellValue(total_credit+"");

        Cell totalDue=final_row.createCell(4);
        totalDue.setCellStyle(style);
        totalDue.setCellValue((total_debit-total_credit)+"");

        FileOutputStream stream=new FileOutputStream(report_file);
        workbook.write(stream);
        stream.close();
    }
    private boolean initializePdfDocument(File report_file) throws FileNotFoundException, DocumentException {
        this.document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(report_file));
        document.open();
        return true;
    }

    private void addHearder() throws DocumentException {
        Paragraph paragraph = new Paragraph(20);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);

        Font headerfont = new Font(Font.FontFamily.HELVETICA);
        headerfont.setStyle(Font.BOLD);
        headerfont.setColor(31, 154, 199);
        headerfont.setSize(14);

        Chunk chunk = new Chunk("SHUBHAM  KUMAR TAX CONSULTANCY");
        chunk.setFont(headerfont);
        paragraph.add(chunk);

        paragraph.add(Chunk.NEWLINE);

        Chunk chunk2 = new Chunk("A-11, KAMAL VHIAR, BURARI, DELHI-110084, INDIA");
        chunk2.setFont(headerfont);
        paragraph.add(chunk2);

        paragraph.add(Chunk.NEWLINE);

        Font panfont = new Font(Font.FontFamily.HELVETICA);
        panfont.setStyle(Font.BOLD);
        panfont.setSize(14);

        Chunk chunk3 = new Chunk("PAN NO:- EKIPK1251B");
        chunk3.setFont(panfont);
        paragraph.add(chunk3);

        document.add(paragraph);

        LineSeparator separator = new LineSeparator();
        separator.setPercentage(80f);
        separator.setAlignment(LineSeparator.ALIGN_CENTER);

        document.add(Chunk.NEWLINE);

        document.add(separator);

    }

    private void addClientListTable() throws DocumentException {
        Paragraph paragraph = new Paragraph();

        PdfPTable table = new PdfPTable(new float[]{0.55f,3,1,1,1});

        Font header_font = new Font();
        header_font.setSize(12);
        header_font.setStyle(Font.BOLD);

        Phrase serial_no_tag = new Phrase("S.No.");
        serial_no_tag.setFont(header_font);
        PdfPCell serial_cell = new PdfPCell(serial_no_tag);
        serial_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        serial_cell.setVerticalAlignment(Element.ALIGN_CENTER);
        serial_cell.setBorderWidth(1);
        table.addCell(serial_cell);

        Phrase client_name_phrase_tag = new Phrase("Name");
        client_name_phrase_tag.setFont(header_font);
        PdfPCell perticular_for_previous_balance = new PdfPCell(client_name_phrase_tag);
        perticular_for_previous_balance.setHorizontalAlignment(Element.ALIGN_CENTER);
        perticular_for_previous_balance.setVerticalAlignment(Element.ALIGN_CENTER);
        perticular_for_previous_balance.setBorderWidth(1);
        table.addCell(perticular_for_previous_balance);

        Phrase debit_phrase_tag = new Phrase("Debit");
        debit_phrase_tag.setFont(header_font);
        PdfPCell perticular_for_previous_amount = new PdfPCell(debit_phrase_tag);
        perticular_for_previous_amount.setHorizontalAlignment(Element.ALIGN_CENTER);
        perticular_for_previous_amount.setVerticalAlignment(Element.ALIGN_CENTER);
        perticular_for_previous_amount.setBorderWidth(1);
        table.addCell(perticular_for_previous_amount);

        Phrase credit_phrase_tag = new Phrase("Credit");
        credit_phrase_tag.setFont(header_font);
        PdfPCell credit_cell_tag = new PdfPCell(credit_phrase_tag);
        credit_cell_tag.setHorizontalAlignment(Element.ALIGN_CENTER);
        credit_cell_tag.setVerticalAlignment(Element.ALIGN_CENTER);
        credit_cell_tag.setBorderWidth(1);
        table.addCell(credit_cell_tag);

        Phrase due_phrase_tag = new Phrase("Due");
        due_phrase_tag.setFont(header_font);
        PdfPCell due_amount = new PdfPCell(due_phrase_tag);
        due_amount.setHorizontalAlignment(Element.ALIGN_CENTER);
        due_amount.setVerticalAlignment(Element.ALIGN_CENTER);
        due_amount.setBorderWidth(1);
        table.addCell(due_amount);

        Font font = new Font();
        header_font.setSize(9);
        for(Client client:clientList){
            Phrase serial_no = new Phrase((clientList.indexOf(client)+1)+".");
            serial_no.setFont(font);
            PdfPCell serial_cell_count = new PdfPCell(serial_no);
            serial_cell_count.setHorizontalAlignment(Element.ALIGN_CENTER);
            serial_cell_count.setVerticalAlignment(Element.ALIGN_CENTER);
            serial_cell_count.setBorderWidth(1);
            table.addCell(serial_cell_count);

            Phrase client_name_phrase = new Phrase(" "+client.getName());
            client_name_phrase.setFont(font);
            PdfPCell client_name_cell = new PdfPCell(client_name_phrase);
            client_name_cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            client_name_cell.setVerticalAlignment(Element.ALIGN_CENTER);
            client_name_cell.setBorderWidth(1);
            table.addCell(client_name_cell);

            int total_debit=0;
            int total_credit=0;
            for(Transection transection:transectionList){
                if(transection.getTransecType().equals("Credit") && transection.getClientId().equals(client.getId()))
                    total_credit+=transection.getAmount();
                if(transection.getTransecType().equals("Debit") && transection.getClientId().equals(client.getId()))
                    total_debit+=transection.getAmount();
            }
            Phrase debit_phrase = new Phrase(total_debit+"");
            debit_phrase.setFont(font);
            PdfPCell debit_cell = new PdfPCell(debit_phrase);
            debit_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            debit_cell.setVerticalAlignment(Element.ALIGN_CENTER);
            debit_cell.setBorderWidth(1);
            table.addCell(debit_cell);

            Phrase credit_phrase = new Phrase(total_credit+"");
            credit_phrase.setFont(font);
            PdfPCell credit_cell = new PdfPCell(credit_phrase);
            credit_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            credit_cell.setVerticalAlignment(Element.ALIGN_CENTER);
            credit_cell.setBorderWidth(1);
            table.addCell(credit_cell);

            Phrase due_phrase = new Phrase((total_debit-total_credit)+"");
            due_phrase.setFont(font);
            PdfPCell due_cell = new PdfPCell(due_phrase);
            due_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            due_cell.setVerticalAlignment(Element.ALIGN_CENTER);
            due_cell.setBorderWidth(1);
            table.addCell(due_cell);
        }

        int total_debit=0;
        int total_credit=0;
        for(Transection transection:transectionList){
            if(transection.getTransecType().equals("Credit"))
                total_credit+=transection.getAmount();
            if(transection.getTransecType().equals("Debit"))
                total_debit+=transection.getAmount();
        }

        Phrase total_tag = new Phrase("Total");
        total_tag.setFont(font);
        PdfPCell total_cell = new PdfPCell(total_tag);
        total_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        total_cell.setVerticalAlignment(Element.ALIGN_CENTER);
        total_cell.setBorderWidth(1);
        total_cell.setColspan(2);
        table.addCell(total_cell);

        Phrase debit_phrase = new Phrase(total_debit+"");
        debit_phrase.setFont(font);
        PdfPCell debit_cell = new PdfPCell(debit_phrase);
        debit_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        debit_cell.setVerticalAlignment(Element.ALIGN_CENTER);
        debit_cell.setBorderWidth(1);
        table.addCell(debit_cell);

        Phrase credit_phrase = new Phrase(total_credit+"");
        credit_phrase.setFont(font);
        PdfPCell credit_cell = new PdfPCell(credit_phrase);
        credit_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        credit_cell.setVerticalAlignment(Element.ALIGN_CENTER);
        credit_cell.setBorderWidth(1);
        table.addCell(credit_cell);

        Phrase due_phrase = new Phrase((total_debit-total_credit)+"");
        due_phrase.setFont(font);
        PdfPCell due_cell = new PdfPCell(due_phrase);
        due_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        due_cell.setVerticalAlignment(Element.ALIGN_CENTER);
        due_cell.setBorderWidth(1);
        table.addCell(due_cell);

        paragraph.add(table);

        document.add(paragraph);
    }
}
