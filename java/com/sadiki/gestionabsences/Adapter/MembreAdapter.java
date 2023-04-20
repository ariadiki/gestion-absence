package com.sadiki.gestionabsences.Adapter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sadiki.gestionabsences.Firebase.FirebaseHelper;
import com.sadiki.gestionabsences.Model.Membre;
import com.sadiki.gestionabsences.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MembreAdapter extends RecyclerView.Adapter<ViewHolder> {
    ArrayList<Membre> membres;
    Context context;
    Dialog dialog;
    TextView txt_id, txt_nom,txt_prenom, txt_genre, txt_date_naissance;
    RadioButton genre_m, genre_f;
    ImageView img_membre;
    DatePickerDialog datePickerDialog;
    FirebaseHelper db;
    final String URL_USER = "https://firebasestorage.googleapis.com/v0/b/gestionabsence-zszh.appspot.com/o/images%2Fuser.png?alt=media&token=b9ba06af-8214-46ae-8e05-95da844fc8c8",
            URL_FUSER = "https://firebasestorage.googleapis.com/v0/b/gestionabsence-zszh.appspot.com/o/images%2Fuserf.png?alt=media&token=d2352853-729f-4db0-aa0e-3ebb3ecb7f8d";

    public MembreAdapter(Context context, ArrayList<Membre> membres) {
        this.membres = membres;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.membres_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Membre membre = membres.get(position);
        //get image from Firebase using Glide
        Glide.with(context)
                .load(membre.getImgMembre())
                .into(holder.img_membre);
        holder.nom_membre.setText(membre.nomComplet());
        //Dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        //afficher les informations de membre
        holder.nom_membre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setContentView(R.layout.affiche_membre);

                img_membre = dialog.findViewById(R.id.affiche_img_membre);
                txt_id = dialog.findViewById(R.id.affiche_id_membre);
                txt_nom = dialog.findViewById(R.id.affiche_nom_membre);
                txt_genre = dialog.findViewById(R.id.affiche_genre_membre);
                txt_date_naissance = dialog.findViewById(R.id.affiche_date_membre);
                ImageButton annuler_affiche = dialog.findViewById(R.id.cancel_affiche);

                annuler_affiche.setOnClickListener(v1 -> dialog.cancel());

                //set data
                Glide.with(context)
                        .load(membre.getImgMembre())
                        .into(img_membre);
                txt_id.setText(String.valueOf(membre.getIdMembre()));
                txt_nom.setText(membre.nomComplet());
                txt_genre.setText(membre.getGenre());
                txt_date_naissance.setText(membre.getDateNaissance());

                dialog.show();
            }
        });

        //Firebase
        db = FirebaseHelper.getInstance();
        DocumentReference docRef = db.getCollectionReference("groupes").document(membre.getIdGroup());

        //Modifier ou supprimer membre control
        holder.control_membre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.control_membre);
                popupMenu.getMenuInflater().inflate(R.menu.membre_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.edit_membre:
                                //Alert Edit membre
                                dialog.setContentView(R.layout.alert_edit_membre);
                                img_membre = dialog.findViewById(R.id.modifier_img_membre);
                                txt_id = dialog.findViewById(R.id.modifier_id_membre);
                                txt_nom = dialog.findViewById(R.id.modifier_nom_membre);
                                txt_prenom = dialog.findViewById(R.id.modifier_prenom_membre);
                                txt_date_naissance = dialog.findViewById(R.id.modifier_naissance_membre);
                                genre_m = dialog.findViewById(R.id.modifier_m);
                                genre_f = dialog.findViewById(R.id.modifier_f);
                                RadioGroup genre = dialog.findViewById(R.id.modifier_genre);
                                Button modifier_membre = dialog.findViewById(R.id.modifier_membre);
                                ImageButton annuler_edit = dialog.findViewById(R.id.cancel_edit_membre);

                                //en changement de genre
                                genre.setOnCheckedChangeListener((group, checkedId) -> {
                                    switch (checkedId) {
                                        case R.id.modifier_m:
                                            img_membre.setImageResource(R.drawable.user);
                                            break;
                                        case R.id.modifier_f:
                                            img_membre.setImageResource(R.drawable.userf);
                                            break;
                                    }
                                });
                                //prendre date naissance de membre
                                String[] dateN = membre.getDateNaissance().split("/");
                                int anne = Integer.valueOf(dateN[2]);
                                int mois = Integer.valueOf(dateN[1]) - 1;
                                int jour = Integer.valueOf(dateN[0]);

                                //creer datedialog pour dateNaissance
                                datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                                    month += 1;//car le mois commence par 0
                                    String date = dayOfMonth + "/" + month + "/" + year;
                                    txt_date_naissance.setText(date);//pique date et l'afficher dans EditText
                                }, anne, mois, jour);
                                datePickerDialog.setCancelable(true);

                                //affiche le datedialog ou on choisie date de naissance
                                txt_date_naissance.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        datePickerDialog.show();
                                    }
                                });

                                //en click sur annuler
                                annuler_edit.setOnClickListener(v1 -> dialog.cancel());
                                //set data
                                Glide.with(context)
                                        .load(membre.getImgMembre())
                                        .into(img_membre);
                                txt_id.setText(String.valueOf(membre.getIdMembre()));
                                txt_nom.setText(membre.getNomMembre());
                                txt_prenom.setText(membre.getPrenomMembre());
                                txt_date_naissance.setText(membre.getDateNaissance());
                                if (membre.getGenre().equals("M"))
                                    genre_m.setChecked(true);
                                else
                                    genre_f.setChecked(true);

                                //Modifier Membre
                                modifier_membre.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!txt_nom.getText().toString().isEmpty()) {
                                            if (!txt_date_naissance.getText().toString().isEmpty()) {
                                                // Get the Firestore document reference for the group
                                                DocumentReference groupRef = db.getCollectionReference("groupes").document(membre.getIdGroup());

                                                // Build the member data to update
                                                Map<String, Object> memberData = new HashMap<>();
                                                memberData.put("nomMembre", txt_nom.getText().toString());
                                                memberData.put("prenomMembre", txt_prenom.getText().toString());
                                                RadioButton checked = dialog.findViewById(genre.getCheckedRadioButtonId());
                                                memberData.put("genreMembre",String.valueOf(checked.getText().toString().charAt(0)));
                                                memberData.put("dateNaissance", txt_date_naissance.getText().toString());

                                                // Get the member ID field path and remove the backticks
                                                FieldPath memberPath = FieldPath.of("membresGroup", String.valueOf(membre.getIdMembre()));
                                                String[] IDmembre = String.valueOf(memberPath).split("\\.");
                                                IDmembre[1] = IDmembre[1].replaceAll("`", "");

                                                // Update the member data in Firestore
                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("membresGroup." + IDmembre[1], memberData);
                                                groupRef.update(updates).addOnSuccessListener(aVoid -> Toast.makeText(context, "Membre modifié avec succès", Toast.LENGTH_SHORT).show());
                                                if (genre_m.isChecked()) {
                                                    membre.setImgMembre(URL_USER);
                                                    membre.setGenre("M");
                                                } else {
                                                    membre.setImgMembre(URL_FUSER);
                                                    membre.setGenre("F");
                                                }
                                                membre.setNomMembre(txt_nom.getText().toString());
                                                membre.setPrenomMembre(txt_prenom.getText().toString());
                                                membre.setDateNaissance(txt_date_naissance.getText().toString());
                                                notifyDataSetChanged();
                                                dialog.cancel();
                                            } else
                                                txt_date_naissance.setError("entrer date de naissace");
                                        } else
                                            txt_nom.setError("svp entrer le nom de membre");
                                    }
                                });

                                dialog.show();
                                break;
                            case R.id.delete_membre:
                                //Supprimer Membre
                                FieldPath memberPath = FieldPath.of("membresGroup", String.valueOf(membre.getIdMembre()));
                                //get Id membre
                                String[] IDmembre = String.valueOf(memberPath).split("\\.");
                                IDmembre[1] = IDmembre[1].replaceAll("`", "");//remove ´´

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("membresGroup." + membre.getIdMembre(), FieldValue.delete());

                                docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "membre est supprimer", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "Error deleting member field.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                membres.remove(membre);
                                notifyDataSetChanged();
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return membres.size();
    }
}

class ViewHolder extends RecyclerView.ViewHolder {
    ImageView img_membre;
    TextView nom_membre;
    ImageButton control_membre;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        img_membre = itemView.findViewById(R.id.img_membre);
        nom_membre = itemView.findViewById(R.id.nom_membre);
        control_membre = itemView.findViewById(R.id.control_membre);
    }
}
