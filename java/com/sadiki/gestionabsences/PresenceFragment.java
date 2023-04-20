package com.sadiki.gestionabsences;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sadiki.gestionabsences.Adapter.AbsentAdapter;
import com.sadiki.gestionabsences.Export.PDFile;
import com.sadiki.gestionabsences.Firebase.FirebaseHelper;
import com.sadiki.gestionabsences.Model.Group;
import com.sadiki.gestionabsences.Model.Membre;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class PresenceFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    Group group;
    EditText dateText;
    AppCompatButton enregistrer, exporter;
    RecyclerView presence_list;
    DatePickerDialog datePickerDialog;
    AbsentAdapter absentAdapter;
    String genre, nom, prenom, dateNaissance, userImage, fuserImage, image;
    FirebaseHelper firebaseHelper;
    FirebaseFirestore db;
    DocumentReference documentReference;
    final String URL_USER = "https://firebasestorage.googleapis.com/v0/b/gestionabsence-zszh.appspot.com/o/images%2Fuser.png?alt=media&token=b9ba06af-8214-46ae-8e05-95da844fc8c8",
            URL_FUSER = "https://firebasestorage.googleapis.com/v0/b/gestionabsence-zszh.appspot.com/o/images%2Fuserf.png?alt=media&token=d2352853-729f-4db0-aa0e-3ebb3ecb7f8d";


    public PresenceFragment() {
    }

    public PresenceFragment(Group group) {
        this.group = group;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_presence, container, false);
        dateText = view.findViewById(R.id.date_picker);
        presence_list = view.findViewById(R.id.presence_list);
        enregistrer = view.findViewById(R.id.save_presence);
        exporter = view.findViewById(R.id.download_pdf);

        //Firebase
        firebaseHelper = FirebaseHelper.getInstance();
        db = FirebaseFirestore.getInstance();

        //Get Images
        userImage = URL_USER;
        fuserImage = URL_FUSER;
        //get Data
        getMembreData();

        //declare Calendar
        Calendar calendar = Calendar.getInstance();
        int anne = calendar.get(Calendar.YEAR);
        int mois = calendar.get(Calendar.MONTH);
        int jour = calendar.get(Calendar.DAY_OF_MONTH);
        //creer datedialog
        datePickerDialog = new DatePickerDialog(getContext(), this, anne, mois, jour);
        datePickerDialog.setCancelable(true);

        //affiche le datedialog en cliquant sur EditText
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        //en changement de la date
        dateText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                for (int i = 0; i < group.getListMembres().size(); i++) {
                    group.getListMembres().get(i).setDatePresence(s.toString());
                    group.getListMembres().get(i).setPresence("null");
                }
                setPresenceAdapter(group.getListMembres());
            }
        });


        //Enregistrer les donnees
        enregistrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //si on a tous les absences saisie dans l'absence on l'enregistre dans BD
                if (check()) {
                    String selectedDate = dateText.getText().toString().replaceAll("/", "");
                    //enregistrer presence dans la base donne par date selectionner passer en parametre
                    DocumentReference dateDocRef = db.collection("groupes")
                            .document(group.getIdGroup())
                            .collection("absence")
                            .document(selectedDate);
                    //boucler sur chaque membre et donner leur statut
                    for (int i = 0; i < group.getListMembres().size(); i++) {
                        String membre = String.valueOf(group.getListMembres().get(i).getIdMembre());
                        String statut = group.getListMembres().get(i).getPresence();
                        dateDocRef.update(membre, statut);
                    }
                    Toast.makeText(getContext(), "Enregistré avec succès", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getContext(), "merci de compléter l'absence", Toast.LENGTH_LONG).show();
            }
        });

        //Exporter les donnees sous forme PDF
        exporter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //verifier s'il ya les membres premierement
                if (group.getListMembres().size() > 0) {
                    if (!dateText.getText().toString().isEmpty()) {
                        if (check()) {
                            try {
                                Toast.makeText(getContext(), "Téléchargement...", Toast.LENGTH_SHORT).show();
                                PDFile pdFile = new PDFile(getContext(), group);
                                pdFile.creerFichier();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }else
                            Toast.makeText(getContext(), "merci de compléter l'absence", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(getContext(), "vous devez entrer une date", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getContext(), "Vous n'avez aucun données", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private boolean check() {
        //on verifier s'il ya membre a null statut
        boolean check = true;
        for (int i = 0; i < group.getListMembres().size(); i++) {
            if (group.getListMembres().get(i).getPresence().equals("null"))
                check = false;
        }
        return check;
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
                                Membre membre = new Membre(i, nom, prenom, genre, image, dateNaissance, group.getIdGroup());
                                group.ajouterMembre(membre);
                            }
                        }
                    }
                    //Liste de presence
                    setPresenceAdapter(group.getListMembres());
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

    @Override
    public void onPause() {
        super.onPause();
        dateText.getText().clear();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        //quand l'utilisateur pique date
        month += 1;//car le mois commence par 0
        String date = dayOfMonth + "/" + month + "/" + year;
        dateText.setText(date);
        getAbsenceData(date);
    }

    public void setPresenceAdapter(ArrayList<Membre> membresList) {
        absentAdapter = new AbsentAdapter(getContext(), membresList);
        presence_list.setAdapter(absentAdapter);
        presence_list.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void getAbsenceData(String dateJour) {
        //enleve / pour nommer le document par date
        dateJour = dateJour.replaceAll("/", "");
        DocumentReference dateDocRef = db.collection("groupes")
                .document(group.getIdGroup())
                .collection("absence")
                .document(dateJour);
        dateDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //Si document date deja existe on recuperer les donnes
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        for (Membre membre : group.getListMembres()) {
                            String membreID = String.valueOf(membre.getIdMembre());
                            if (data.containsKey(membreID)) {
                                Object absenceStatus = data.get(membreID);
                                // affecter l'absence et l'afficher
                                for (int i = 0; i < group.getListMembres().size(); i++) {
                                    if (group.getListMembres().get(i).getIdMembre() == membre.getIdMembre()) {
                                        group.getListMembres().get(i).setDatePresence(dateText.getText().toString());
                                        group.getListMembres().get(i).setPresence(absenceStatus.toString());
                                    }
                                }
                            }
                        }
                        absentAdapter.notifyDataSetChanged();
                    }
                    //si le document date n'existe pas on l creer avec des null valeur pour la presence
                    else {
                        Map<String, Object> data = new HashMap<>();
                        for (Membre member : group.getListMembres()) {
                            data.put(String.valueOf(member.getIdMembre()), "null");
                        }
                        dateDocRef.set(data);
                    }
                } else {
                    Toast.makeText(getContext(), "Erreur de recuperer les donnees d'absence", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}