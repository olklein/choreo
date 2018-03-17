package com.olklein.choreo;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.itextpdf.text.Annotation;
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
    private static final float left = 30;
    private static final float right = 30;
    private static final float top = 50;
    private static final float bottom = 40;

    private static DownloadManager mDownloadManager;

    public void createPdf(Context ctxt, String filePath, String title, boolean withComment,
                          Drawable drawable, ArrayList<DanceFigure> items,
                          FileOutputStream outFile) {
        new GetResults(ctxt, withComment, drawable, items, outFile, filePath, title).execute();
    }

    static class MyFooter extends PdfPageEventHelper {
        final Drawable logoDrawable;
        final String headerTitle;


        public MyFooter(Drawable drawable, String headertitle) {
            logoDrawable = drawable;
            headerTitle = headertitle;
        }
        final Font footerFont = new Font(Font.FontFamily.UNDEFINED, 10, Font.ITALIC);
        final Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);

        public void onEndPage(PdfWriter writer, Document document) {
            if (writer != null) {
                PdfContentByte cb = writer.getDirectContent();
                PdfPTable headerLine = new PdfPTable(2);
                headerLine.setTotalWidth(document.getPageSize().getWidth() - 2 * 30);

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

                BitmapDrawable bitDw = ((BitmapDrawable) logoDrawable);
                Bitmap bmp = bitDw.getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

                try {
                    logo = Image.getInstance(stream.toByteArray());
                    Annotation annotation = new Annotation(0, 0, 0, 0,
                            "https://play.google.com/store/apps/dev?id=7846443674268659262");
                    logo.setAnnotation(annotation);
                    headerLine.addCell(logo);


                } catch (BadElementException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                headerLine.addCell(phrase);
                // write content
                headerLine.writeSelectedRows(0, -1, document.leftMargin(),
                        document.getPageSize().getHeight() - (document.topMargin() - headerLine.getTotalHeight()) / 2, writer.getDirectContent());

                Phrase footer = new Phrase("" + document.getPageNumber(), footerFont);
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                        footer,
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.bottom() - document.bottomMargin() / 2, 0);
            }
        }
    }
    public static void initDownloadManager(Context ctxt) {
        //mDownloadManager = null;
        mDownloadManager =(DownloadManager) ctxt.getSystemService(Context.DOWNLOAD_SERVICE);
    }


    static class GetResults extends AsyncTask<String, Void, String> {
        final NotificationManager mNotifyManager;
        final String filename;
        final String mFilePath;
        final Drawable drawable;
        final FileOutputStream outFile;
        final boolean withComment;
        final private ArrayList<DanceFigure> mItemArray;
        final Notification.Builder mBuilder;


        GetResults(Context context, Boolean Comment, Drawable d, ArrayList<DanceFigure> items,
                   FileOutputStream out, String filePath, String title){
            Resources mResources = context.getResources();
            mNotifyManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationUtils mNotification = new NotificationUtils(context);
            filename = title;
            drawable = d;
            outFile = out;
            mFilePath = filePath;
            withComment = Comment;
            mItemArray = items;

            Uri fileURI;
            File file = new File(filePath);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileURI = getUriForFile(context, "com.olklein.choreo.fileProvider", file);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setDataAndType(fileURI, "application/pdf");
            }else{
                fileURI= Uri.fromFile(file);
                intent.setDataAndType(fileURI, "application/pdf");
            }

            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION );

            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
            mNotification.createChannels();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder = new Notification.Builder(context,NotificationUtils.CHOREO_CHANNEL_ID);
            }else{
                mBuilder = new Notification.Builder(context);
            }

            int color = mResources.getColor(android.R.color.holo_blue_light);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setContentTitle(mResources.getString(R.string.app_name))
                        .setContentText(mResources.getString(R.string.pdf_file_uploaded,new File(filePath).getName()))
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .setColor(color);
            }else{
                mBuilder.setContentTitle(mResources.getString(R.string.app_name))
                        .setContentText(mResources.getString(R.string.pdf_file_uploaded,new File(filePath).getName()))
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true);
            }
            if (mDownloadManager == null){
               if(ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                   MainActivity.requestPermission(context.getApplicationContext(), Manifest.permission.INTERNET, MainActivity.MYINTERNETREQUEST);
               }else {
                   initDownloadManager(context);
               }
            }
        }

        @Override
        protected String doInBackground(String... params) {

            BaseFont bf = null;
            try {
                bf = BaseFont.createFont("assets/fonts/FreeSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
                if (mNotifyManager != null) mNotifyManager.cancel(1234);
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
                if (mNotifyManager != null) mNotifyManager.cancel(1234);
            }
            document.addTitle(filename);
            if (writer!= null) {
                MyFooter event = new MyFooter(drawable,filename);
                writer.setPageEvent(event);

                PdfPageLabels labels = new PdfPageLabels();
                labels.addPageLabel(1, PdfPageLabels.DECIMAL_ARABIC_NUMERALS);
                writer.setPageLabels(labels);
            }
            document.open();

            PdfPTable table = new PdfPTable(60);
            table.setTotalWidth(document.getPageSize().getWidth());
            table.setWidthPercentage(100);

            for (DanceFigure item : mItemArray) {
                PdfPCell cell = new PdfPCell();
                if (item.getTempo().startsWith("VideoURI-")) {
                    continue;
                }
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
                    if (mNotifyManager != null) mNotifyManager.cancel(1234);
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
                    if (mNotifyManager != null) mNotifyManager.cancel(1234);
                }
            }
            return "Done";
        }

        protected void onPostExecute(String result) {
            if (mNotifyManager != null){
                if (mDownloadManager ==null) mNotifyManager.notify(1235, mBuilder.build());
                mNotifyManager.cancel(1234);
            }
            if (mDownloadManager!= null){
                File file = new File(mFilePath);
                mDownloadManager.addCompletedDownload(file.getName(),
                        "Choreo",
                        false,
                        "application/pdf",
                        mFilePath,
                        file.length(),true);
            }


        }
    }
}