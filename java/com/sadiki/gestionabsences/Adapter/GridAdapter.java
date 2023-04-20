package com.sadiki.gestionabsences.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.sadiki.gestionabsences.Firebase.FirebaseHelper;
import com.sadiki.gestionabsences.GroupActivity;
import com.sadiki.gestionabsences.MainActivity;
import com.sadiki.gestionabsences.Model.Group;
import com.sadiki.gestionabsences.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GridAdapter extends BaseAdapter {
    Context context;
    ArrayList<Group> groups;
    LayoutInflater layoutInflater;
    Dialog dialog;
    FirebaseHelper firebaseHelper;
    CollectionReference collectionReference;

    public GridAdapter(Context context, ArrayList<Group> groups) {
        this.context = context;
        this.groups = groups;
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Object getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (layoutInflater == null)
            layoutInflater = LayoutInflater.from(context);
        if (view == null)
            view = layoutInflater.inflate(R.layout.grid_item, parent, false);

        ImageView img = view.findViewById(R.id.img_grid);
        TextView txt = view.findViewById(R.id.txt_grid);

        firebaseHelper = FirebaseHelper.getInstance();
        collectionReference = firebaseHelper.getCollectionReference("groupes");

        final Group group = groups.get(position);
        txt.setText(group.getNomGroup());
        Glide.with(context)
                .load(group.getImage())
                .fallback(R.drawable.group)
                .into(img);

        //en click sur grid item
        view.findViewById(R.id.item_grid).setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("group", group);//envoi l'objet group
            context.startActivity(intent);
        });

        //en clic long sur item (modifier ou supprimer)
        view.findViewById(R.id.item_grid).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.group_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.edit_group:
                                //Dialog
                                dialog = new Dialog(context);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView(R.layout.alert_edit_group);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.setCancelable(true);

                                TextInputEditText nom_group = dialog.findViewById(R.id.modifier_nom_group);
                                TextInputEditText desc_group = dialog.findViewById(R.id.modifier_desc_group);
                                Button modifier_group = dialog.findViewById(R.id.modifier_group);
                                ImageButton annuler_modification = dialog.findViewById(R.id.cancel_edit);
                                //donnes par defaut
                                nom_group.setText(group.getNomGroup());
                                desc_group.setText(group.getDescription());

                                //Modifier group
                                modifier_group.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!nom_group.getText().toString().isEmpty()) {
                                            DocumentReference documentReference = collectionReference.document(group.getIdGroup());
                                            //Set data
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("nomGroup", nom_group.getText().toString());
                                            updates.put("description", desc_group.getText().toString());
                                            //Modification
                                            documentReference.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    group.setNomGroup(nom_group.getText().toString());
                                                    group.setDescription(desc_group.getText().toString());
                                                    notifyDataSetChanged();
                                                    Toast.makeText(context, "Modifié avec succès", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            dialog.cancel();
                                        } else
                                            nom_group.setError("svp entrer le nom de group");
                                    }
                                });
                                annuler_modification.setOnClickListener(v1 -> dialog.cancel());
                                dialog.show();
                                break;

                            //Supprimer group
                            case R.id.delete_group:
                                DocumentReference deleteDocument = collectionReference.document(group.getIdGroup());
                                deleteDocument.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                groups.remove(group);
                                                for (int i = position; i < groups.size() - 1; i++) {
                                                    groups.set(i, groups.get(i + 1));
                                                }
                                                notifyDataSetChanged();
                                                Toast.makeText(context, "group " + group.getNomGroup() + " est supprimer", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });

        return view;
    }
}
