package com.olklein.choreo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.support.v4.content.FileProvider.getUriForFile;

/**
 * Created by olklein on 06/07/2017.
 *
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects
 *    for all of the code used other than as permitted herein. If you modify
 *    file(s) with this exception, you may extend this exception to your
 *    version of the file(s), but you are not obligated to do so. If you do not
 *    wish to do so, delete this exception statement from your version. If you
 *    delete this exception statement from all source files in the program,
 *    then also delete it in the license file.
 */

class PDFCreator {
    private final float left = 30;
    private final float right = 30;
    private final float top = 50;
    private final float bottom = 40;
    private ArrayList<DanceFigure> mItemArray;
    private String headerTitle;

    public void createPdf(Context ctxt, String filePath, String title, boolean withComment, Drawable drawable, ArrayList<DanceFigure> items, FileOutputStream outFile) {
        mItemArray = items;
        new GetResults(ctxt, withComment, drawable, outFile ).execute(filePath,title );
    }

    class MyFooter extends PdfPageEventHelper {
        final Drawable logoDrawable;

        public MyFooter(Drawable drawable) {
            logoDrawable = drawable;
        }
        final Font footerFont = new Font(Font.FontFamily.UNDEFINED, 10, Font.ITALIC);
        final Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);

        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            PdfPTable headerLine = new PdfPTable(2);
            headerLine.setTotalWidth(document.getPageSize().getWidth()-2*30);



            try {
                headerLine.setWidths(new int[]{1, 14});
            } catch (DocumentException e1) {
                e1.printStackTrace();
            }

            headerLine.setLockedWidth(true);
            headerLine.getDefaultCell().setFixedHeight(30);
            headerLine.getDefaultCell().setBorder(Rectangle.BOTTOM);
            headerLine.getDefaultCell().setBorderColor(new BaseColor(0xFF4285f4));
            Phrase phrase = new Phrase(headerTitle, headerFont);
            headerFont.setColor(new BaseColor(0xFF4285f4));

            // add image
            Image logo;
            Drawable d = logoDrawable;

            BitmapDrawable bitDw = ((BitmapDrawable) d);
            Bitmap bmp = bitDw.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

            try {
                logo = Image.getInstance(stream.toByteArray());
                headerLine.addCell(logo);
            } catch (BadElementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            headerLine.addCell(phrase);
            // write content
            headerLine.writeSelectedRows(0, -1, document.leftMargin(),
                    document.getPageSize().getHeight() -(document.topMargin()-headerLine.getTotalHeight())/2, writer.getDirectContent());

            Phrase footer = new Phrase(""+document.getPageNumber(), footerFont);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - document.bottomMargin()/2, 0);
        }
    }
    // new
    class GetResults extends AsyncTask<String, Void, String> {
        final Context ctxt;
        String filename;
        String filePath;
        final Drawable drawable;
        final FileOutputStream outFile;
        final boolean withComment;


        GetResults(Context context, Boolean Comment, Drawable d, FileOutputStream out){
            ctxt= context;
            drawable = d;
            outFile = out;
            withComment = Comment;
        }

        @Override
        protected String doInBackground(String... params) {

            filePath =params[0];
            filename =params[1];

            headerTitle=filename;
            BaseFont bf = null;
            try {
                bf = BaseFont.createFont("assets/fonts/FreeSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
                NotificationManager mNotifyManager=(NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyManager.cancel(1234);
            }

            Font smallFont = new Font(bf,12);
            Font smallGreyFont = new Font(bf,11,Font.ITALIC);
            BaseColor greyColor = new BaseColor(0xFFAAAAAA);
            smallGreyFont.setColor(greyColor);
            Font font = new Font();
            font.setSize(12);

            Document document = new Document(PageSize.A4, left, right, top, bottom);
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, outFile);
            } catch (DocumentException e) {
                e.printStackTrace();
                NotificationManager mNotifyManager=(NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyManager.cancel(1234);
            }
            document.addTitle(headerTitle);
            MyFooter event = new MyFooter(drawable);
            writer.setPageEvent(event);

            PdfPageLabels labels = new PdfPageLabels();
            labels.addPageLabel(1, PdfPageLabels.DECIMAL_ARABIC_NUMERALS);

            writer.setPageLabels(labels);
            document.open();

            PdfPTable table = new PdfPTable(60);
            table.setTotalWidth(document.getPageSize().getWidth());
            table.setWidthPercentage(100);


            for (DanceFigure item : mItemArray) {
                PdfPCell cell = new PdfPCell();

                if (item.getComment().equals("") && withComment) {
                    cell.setBorderColor(greyColor);
                    cell.setBorder(Rectangle.BOTTOM);
                }else {
                    cell.setBorder(Rectangle.NO_BORDER);
                }

                Paragraph p = new Paragraph();
                Phrase phrase = new Phrase("\u2022\u00a0"+item.getName(), font);
                p.add(phrase);
                cell.addElement(p);
                cell.setColspan(46);

                table.addCell(cell);

                cell = new PdfPCell();
                if (item.getComment().equals("") && withComment) {
                    cell.setBorderColor(greyColor);
                    cell.setBorder(Rectangle.BOTTOM);
                }else {
                    cell.setBorder(Rectangle.NO_BORDER);
                }
                Paragraph prg = new Paragraph(""+item.getTempo(), smallFont);
                //cell.addElement(new Phrase(""+item.getTempo(), smallFont));
                prg.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(prg);

                cell.setColspan(14);
                table.addCell(cell);
                if (!item.getComment().equals("")&& withComment){
                    cell = new PdfPCell();
                    cell.setColspan(2);
                    cell.setBorder(Rectangle.BOTTOM);
                    Phrase ph =new Phrase("",smallGreyFont);
                    cell.addElement(ph);
                    cell.setBorderColor(greyColor);
                    cell.setFixedHeight(24f);
                    table.addCell(cell);

                    cell = new PdfPCell();
                    cell.setVerticalAlignment(Element.ALIGN_TOP);
                    cell.setBorder(Rectangle.BOTTOM);
                    cell.setBorderColor(greyColor);

                    cell.setColspan(58);
                    cell.addElement(new Phrase(""+item.getComment(), smallGreyFont));
                    cell.setFixedHeight(24f);
                    table.addCell(cell);
                }
            }

            if(!mItemArray.isEmpty()) {
                try {
                    document.add(table);
                    document.close();
                } catch (DocumentException e) {
                    e.printStackTrace();
                    NotificationManager mNotifyManager=(NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotifyManager.cancel(1234);
                }
            }else{
                PdfPCell cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                cell.addElement(new Phrase("", smallFont));
                cell.setColspan(60);
                table.addCell(cell);
                try {
                    document.add(table);
                    document.close();
                } catch (DocumentException e) {
                    e.printStackTrace();
                    NotificationManager mNotifyManager=(NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotifyManager.cancel(1234);
                }
            }


            return "Done";
        }

//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }

        protected void onPostExecute(String result) {
            Resources resources = ctxt.getResources();
            Uri fileURI;
            File file = new File(filePath);
            NotificationManager mNotifyManager;
            NotificationCompat.Builder mBuilder;
            mNotifyManager = (NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent intent = new Intent(Intent.ACTION_VIEW);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileURI = getUriForFile(ctxt, "com.olklein.choreo.fileProvider", file);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setDataAndType(fileURI, "application/*");
            }else{
                fileURI= Uri.fromFile(file);
                intent.setDataAndType(fileURI, "application/pdf");
            }



            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION );

            PendingIntent pIntent = PendingIntent.getActivity(ctxt, 0, intent, 0);

            mBuilder = new NotificationCompat.Builder(ctxt);
            int color = resources.getColor(android.R.color.holo_blue_light);
            mBuilder.setContentTitle(resources.getString(R.string.app_name))
                    .setContentText(resources.getString(R.string.pdf_file_uploaded,filename))
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setColor(color);

            mNotifyManager.notify(1235, mBuilder.build());
            mNotifyManager.cancel(1234);
        }

    }
}