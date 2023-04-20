package com.sadiki.gestionabsences.Export;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.sadiki.gestionabsences.BuildConfig;
import com.sadiki.gestionabsences.Model.Group;
import com.sadiki.gestionabsences.Model.Membre;
import com.sadiki.gestionabsences.R;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PDFile {
    Group group;
    Context context;

    public PDFile(Context context, Group group) {
        this.context = context;
        this.group = group;
    }

    public void creerFichier() throws IOException {
        String date = group.getListMembres().get(0).getDatePresence().replaceAll("/", "");
        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        String pdfName = group.getNomGroup() + "_" + date + "_Rapport.pdf";
        File file = new File(pdfPath, pdfName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PdfWriter pdfWriter = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

        Paragraph title = new Paragraph("Rapport D'absence\n\n").setBold().setFontSize(30f).setTextAlignment(TextAlignment.CENTER);

        float[] widthInfo = {80, 80};
        Table info = new Table(widthInfo);
        //Row 1
        info.addCell(new Cell().add(new Paragraph("Group :").setFontSize(15f)).setBorder(Border.NO_BORDER));
        info.addCell(new Cell().add(new Paragraph(group.getNomGroup() + " (" + group.getDescription() + ")").setFontSize(15f).setBold()).setBorder(Border.NO_BORDER));
        //Row 2
        info.addCell(new Cell().add(new Paragraph("Date :").setFontSize(15f)).setBorder(Border.NO_BORDER));
        info.addCell(new Cell().add(new Paragraph(group.getListMembres().get(0).getDatePresence() + "\n\n").setFontSize(15f).setBold()).setBorder(Border.NO_BORDER));

        float[] width = {150, 150, 150, 150};
        Table table = new Table(width);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //Row 3
        int color = context.getResources().getColor(R.color.blue);
        int color2 = context.getResources().getColor(R.color.light_green);
        int color3 = context.getResources().getColor(R.color.light_red);
        DeviceRgb blue = new DeviceRgb(Color.red(color), Color.green(color), Color.blue(color));
        DeviceRgb white = new DeviceRgb(255, 255, 255);
        DeviceRgb coloring;
        table.addCell(new Cell().add(new Paragraph("Numero").setBold().setFontSize(15f).setBackgroundColor(blue).setFontColor(white)));
        table.addCell(new Cell().add(new Paragraph("Nom").setBold().setFontSize(15f).setBackgroundColor(blue).setFontColor(white)));
        table.addCell(new Cell().add(new Paragraph("Prenom").setBold().setFontSize(15f).setBackgroundColor(blue).setFontColor(white)));
        table.addCell(new Cell().add(new Paragraph("Absence").setBold().setFontSize(15f).setBackgroundColor(blue).setFontColor(white)));

        //Row 4 (list membre)
        for (Membre membre : group.getListMembres()) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(membre.getIdMembre()))).setFontSize(12f));
            table.addCell(new Cell().add(new Paragraph(membre.getNomMembre())).setFontSize(12f));
            table.addCell(new Cell().add(new Paragraph(membre.getPrenomMembre())).setFontSize(12f));
            if (membre.getPresence().equals("P"))
                coloring = new DeviceRgb(Color.red(color2), Color.green(color2), Color.blue(color2));
            else
                coloring = new DeviceRgb(Color.red(color3), Color.green(color3), Color.blue(color3));
            table.addCell(new Cell().add(new Paragraph(membre.getPresence()).setBold().setBackgroundColor(coloring).setFontSize(12f)));
        }
        //Create pdf document
        document.add(title);
        document.add(info);
        document.add(table);
        document.close();

        //Permision de stockage
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        else {
        //Notification
            File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file.getName());
            Uri pdfUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);

            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(pdfUri, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, pdfIntent, PendingIntent.FLAG_IMMUTABLE);

            // Define the notification channel ID
            String channelId = "PDF_DOWNLOAD_CHANNEL";
            // Create a notification builder
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.absentism)
                    .setContentTitle(file.getName())
                    .setContentText("PDF est téléchargé")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent);
            // Define the notification manager
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Create the notification channel (for Android 8.0 and above)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Téléchargement PDF", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
            // Show the notification
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
