package com.sadiki.gestionabsences.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sadiki.gestionabsences.Model.Membre;
import com.sadiki.gestionabsences.R;

import java.util.ArrayList;

public class AbsentAdapter extends RecyclerView.Adapter<myHolder> {
    ArrayList<Membre> membres;
    Context context;
    Dialog dialog;
    TextView txt_id, txt_nom, txt_genre, txt_date_naissance;
    ImageView img_membre;

    public AbsentAdapter(Context context, ArrayList<Membre> membres) {
        this.membres = membres;
        this.context = context;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.absent_item, parent, false);
        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        final Membre membre = membres.get(position);
        //get image from Firebase using Glide
        Glide.with(context)
                .load(membre.getImgMembre())
                .into(holder.img_membre);
        holder.nom_membre.setText(membre.nomComplet());

        //affichage les absence
        switch (membre.getPresence()) {
            case "P":
                holder.absent.setBackgroundColor(context.getResources().getColor(R.color.light_red));
                holder.present.setBackgroundColor(context.getResources().getColor(R.color.green));
                break;
            case "A":
                holder.absent.setBackgroundColor(context.getResources().getColor(R.color.red));
                holder.present.setBackgroundColor(context.getResources().getColor(R.color.light_green));
                break;
            default://null
                holder.absent.setBackgroundColor(context.getResources().getColor(R.color.light_red));
                holder.present.setBackgroundColor(context.getResources().getColor(R.color.light_green));
                break;
        }

        //membre absent
        holder.absent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!membre.getDatePresence().isEmpty()) {
                    holder.absent.setBackgroundColor(context.getResources().getColor(R.color.red));
                    holder.present.setBackgroundColor(context.getResources().getColor(R.color.light_green));
                    membre.setPresence("A");
                } else
                    Toast.makeText(context, "Vous devez d'abord choisir une date", Toast.LENGTH_LONG).show();
            }
        });
        //membre present
        holder.present.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!membre.getDatePresence().isEmpty()) {
                    holder.present.setBackgroundColor(context.getResources().getColor(R.color.green));
                    holder.absent.setBackgroundColor(context.getResources().getColor(R.color.light_red));
                    membre.setPresence("P");
                } else
                    Toast.makeText(context, "Vous devez d'abord choisir une date", Toast.LENGTH_LONG).show();
            }
        });

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
    }

    @Override
    public int getItemCount() {
        return membres.size();
    }
}

class myHolder extends RecyclerView.ViewHolder {
    ImageView img_membre;
    TextView nom_membre;
    Button absent, present;

    public myHolder(@NonNull View itemView) {
        super(itemView);
        img_membre = itemView.findViewById(R.id.img_absent);
        nom_membre = itemView.findViewById(R.id.nom_absent);
        absent = itemView.findViewById(R.id.btn_absent);
        present = itemView.findViewById(R.id.btn_present);
    }
}
