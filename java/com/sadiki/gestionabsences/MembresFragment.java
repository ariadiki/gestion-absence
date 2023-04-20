package com.sadiki.gestionabsences;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sadiki.gestionabsences.Adapter.MembreAdapter;
import com.sadiki.gestionabsences.Firebase.FirebaseHelper;
import com.sadiki.gestionabsences.Model.Group;
import com.sadiki.gestionabsences.Model.Membre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MembresFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    Group group;
    Dialog dialog;
    RecyclerView listMembre;
    DocumentReference documentReference;
    String genre, nom,prenom, dateNaissance, userImage, fuserImage, image;
    DatePickerDialog datePickerDialog;
    EditText nom_membre,prenom_membre, date_naissance;
    TextView membreCount;
    RadioButton masculin, feminin;
    RadioGroup radioGenre;
    ImageView img_membre;
    MembreAdapter membreAdapter;
    FirebaseHelper firebaseHelper;

    int idMembre = 0;
    final String URL_USER = "https://firebasestorage.googleapis.com/v0/b/gestionabsence-zszh.appspot.com/o/images%2Fuser.png?alt=media&token=b9ba06af-8214-46ae-8e05-95da844fc8c8",
            URL_FUSER = "https://firebasestorage.googleapis.com/v0/b/gestionabsence-zszh.appspot.com/o/images%2Fuserf.png?alt=media&token=d2352853-729f-4db0-aa0e-3ebb3ecb7f8d";


    public MembresFragment() {
    }

    public MembresFragment(Group group) {
        this.group = group;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_membres, container, false);
        CardView ajouterMembre = view.findViewById(R.id.ajouter_membre);
        SearchView cherecherMembre = view.findViewById(R.id.chercher_membre);
        listMembre = view.findViewById(R.id.membre_list);
        membreCount = view.findViewById(R.id.membres_count);
        //Firebase
        firebaseHelper = FirebaseHelper.getInstance();
        DocumentReference documentReference = firebaseHelper.getCollectionReference("groupes")
                .document(group.getIdGroup());

        //Get Images
        userImage = URL_USER;
        fuserImage = URL_FUSER;
        //get Data
        getMembreData();

        //creer datedialog pour dateNaissance
        datePickerDialog = new DatePickerDialog(getContext(), this, 2000, 0, 1);
        datePickerDialog.setCancelable(true);

        //Ajouter Membre
        ajouterMembre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dialog
                dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.alert_create_membre);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(true);

                img_membre = dialog.findViewById(R.id.alert_img_membre);
                nom_membre = dialog.findViewById(R.id.alert_nom_membre);
                prenom_membre = dialog.findViewById(R.id.alert_prenom_membre);
                date_naissance = dialog.findViewById(R.id.alert_naissance_membre);
                radioGenre = dialog.findViewById(R.id.radio_membre);
                masculin = dialog.findViewById(R.id.alert_m);
                feminin = dialog.findViewById(R.id.alert_f);
                Button ajouter_membre = dialog.findViewById(R.id.ajouter_membre);
                ImageButton annuler_membre = dialog.findViewById(R.id.cancel_membre);

                //affiche le datedialog ou on choisie date de naissance
                date_naissance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        datePickerDialog.show();
                    }
                });

                //check genre
                radioGenre.setOnCheckedChangeListener((group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.alert_m:
                            img_membre.setImageResource(R.drawable.user);
                            break;
                        case R.id.alert_f:
                            img_membre.setImageResource(R.drawable.userf);
                            break;
                    }
                });

                //action d'ajout
                ajouter_membre.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!nom_membre.getText().toString().isEmpty()) {
                            if(!prenom_membre.getText().toString().isEmpty()) {
                                if (!date_naissance.getText().toString().isEmpty()) {
                                    Membre membre = new Membre();
                                    if (masculin.isChecked()) {
                                        membre.setImgMembre(URL_USER);
                                        membre.setGenre("M");
                                    } else {
                                        membre.setImgMembre(URL_FUSER);
                                        membre.setGenre("F");
                                    }

                                    idMembre = group.getLastID() + 1;

                                    membre.setIdMembre(idMembre);
                                    membre.setNomMembre(nom_membre.getText().toString().trim());
                                    membre.setPrenomMembre(prenom_membre.getText().toString().trim());
                                    membre.setDateNaissance(date_naissance.getText().toString());
                                    membre.setIdGroup(group.getIdGroup());

                                    //create membreGroup field
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("nomMembre", membre.getNomMembre());
                                    map.put("prenomMembre", membre.getPrenomMembre());
                                    map.put("genreMembre", membre.getGenre());
                                    map.put("dateNaissance", membre.getDateNaissance());

                                    //Creation Membre
                                    documentReference.update("membresGroup." + membre.getIdMembre(), map)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    group.setLastID(membre.getIdMembre());
                                                    membreCount.setText(String.valueOf(group.getListMembres().size()));
                                                }
                                            });
                                    group.ajouterMembre(membre);
                                    membreAdapter.notifyDataSetChanged();
                                    dialog.cancel();
                                } else
                                    date_naissance.setError("entrer date de naissace");
                            }else
                                prenom_membre.setError("svp entrer le prenom de membre");
                        } else
                            nom_membre.setError("svp entrer le nom de membre");
                    }
                });

                annuler_membre.setOnClickListener(v1 -> dialog.cancel());

                dialog.show();
            }
        });

        //Chercher Membre
        cherecherMembre.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                cherecherMembre.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String requete) {

                //on creer un nouveau list ou on le donne membre rechercher pour le passer a l'adapter
                ArrayList<Membre> membreArrayList = new ArrayList<>();
                //je boucle sure les Membre en comparant le nom avec la requete chercher
                for (int i = 0; i < group.getListMembres().size(); i++) {
                    //j'ai utiliser toLowerCase pour n'etre pas sensible avec UpperCase de nom
                    if (group.getListMembres().get(i).nomComplet().trim().toLowerCase().contains(requete.toLowerCase()))
                        membreArrayList.add(group.getListMembres().get(i));
                    setAdapter(membreArrayList);
                }
                return true;
            }
        });

        return view;
    }

    private void getMembreData() {
        //get Membre data
        documentReference = firebaseHelper.getFireStoreReference(group.getIdGroup());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData();
                    //get membre from group il est en forme JSON donc en l'enregitre dans Map(cle,valeur)
                    Map<String, Map<String, Object>> membres = (Map<String, Map<String, Object>>)
                            data.get("membresGroup");
                    if (membres.size() > 0) {
                        group.getListMembres().clear();
                        //get Keys(ID) of the Membre
                        Set<String> IDs = membres.keySet();
                        String[] id_membre = IDs.toArray(new String[IDs.size()]); //Convert Set to String Array
                        group.setFirstID(Integer.valueOf(id_membre[0]));//get First ID
                        group.setLastID(Integer.valueOf(id_membre[id_membre.length - 1]));//get Last ID

                        //boucler sur les membres et l'ajouter dans leur group
                        for (int i = group.getFirstID(); i <= group.getLastID(); i++) {
                            if (membres.containsKey(String.valueOf(i))) {
                                genre = membres.get(String.valueOf(i)).get("genreMembre").toString();
                                nom = membres.get(String.valueOf(i)).get("nomMembre").toString();
                                prenom = membres.get(String.valueOf(i)).get("prenomMembre").toString();
                                dateNaissance = membres.get(String.valueOf(i)).get("dateNaissance").toString();
                                if (genre.equals("M")) image = userImage;
                                else image = fuserImage;
                                Membre membre = new Membre(i, nom,prenom, genre, image, dateNaissance, group.getIdGroup());
                                group.ajouterMembre(membre);
                            }
                        }
                    }
                    setAdapter(group.getListMembres());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //get Data
        getMembreData();
    }

    public void setAdapter(ArrayList<Membre> membreArrayList) {
        membreAdapter = new MembreAdapter(getContext(), membreArrayList);
        listMembre.setAdapter(membreAdapter);
        listMembre.setLayoutManager(new LinearLayoutManager(getContext()));
        //afficher counteur des membres
        membreCount.setText(String.valueOf(membreArrayList.size()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month += 1;//car le mois commence par 0
        String date = dayOfMonth + "/" + month + "/" + year;
        date_naissance.setText(date);//pique date et l'afficher dans EditText
    }
}