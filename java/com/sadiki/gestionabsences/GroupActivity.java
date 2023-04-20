package com.sadiki.gestionabsences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sadiki.gestionabsences.Adapter.GridAdapter;
import com.sadiki.gestionabsences.Firebase.FirebaseHelper;
import com.sadiki.gestionabsences.Model.Group;
import com.sadiki.gestionabsences.Model.Membre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupActivity extends AppCompatActivity {
    GridView gridView;
    FloatingActionButton fab;
    String groupImage;
    ArrayList<Group> groups = new ArrayList<>();
    FirebaseHelper firebaseHelper;
    FirebaseStorage firebaseStorage;
    DocumentReference documentReference, createDocument, createAbsence;
    DatabaseReference firebaseDatabase;
    StorageReference groupRef;
    String genre, nom, dateNaissance, userImage, fuserImage, image;
    GridAdapter gridAdapter;
    Dialog dialog;
    int dernierId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        gridView = findViewById(R.id.grid_group);
        fab = findViewById(R.id.add_group);
        //toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Groupes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase Storage and Database
        firebaseHelper = FirebaseHelper.getInstance();
        firebaseStorage = firebaseHelper.getStorage();
        firebaseDatabase = firebaseHelper.getDatabaseReference("groupes");

        //get Image
        groupRef = firebaseHelper.getStorageReference("images/group.png");

        //get Group Image URL
        groupRef.getDownloadUrl().addOnSuccessListener(uri -> groupImage = uri.toString());

        //get group ID to get Data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("groupes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    getGroupData(document.getId());
                }
            } else {
                Toast.makeText(GroupActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });

        //Ajouter Group
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dialog
                dialog = new Dialog(GroupActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.alert_create_group);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(true);

                TextInputEditText nom_group = dialog.findViewById(R.id.alert_nom_group);
                TextInputEditText desc_group = dialog.findViewById(R.id.alert_desc_group);
                Button ajouter_group = dialog.findViewById(R.id.ajouter_group);
                ImageButton annuler_ajout = dialog.findViewById(R.id.cancel_group);

                //get LastId group
                String[] separer = groups.get(groups.size() - 1).getIdGroup().split("group");
                dernierId = Integer.valueOf(separer[1]) + 1;

                //action d'ajout de group
                ajouter_group.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Group group = new Group();
                        if (!nom_group.getText().toString().isEmpty()) {
                            //creation de group
                            group.setIdGroup("group" + (dernierId));
                            createDocument = firebaseHelper
                                    .getCollectionReference("groupes")
                                    .document(group.getIdGroup());
                            // Set some data in the document
                            Map<String, Object> data = new HashMap<>();
                            data.put("nomGroup", nom_group.getText().toString());
                            data.put("membresGroup", new HashMap<>());
                            data.put("description",desc_group.getText().toString());

                            // creation Group
                            createDocument.set(data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            group.setNomGroup(nom_group.getText().toString());
                                            group.setDescription(desc_group.getText().toString());
                                            group.setImage(groupImage);
                                            group.setFirstID(dernierId * 50);
                                            group.setLastID(dernierId * 50);
                                            groups.add(group);
                                            gridAdapter.notifyDataSetChanged();
                                            Toast.makeText(GroupActivity.this, group.getIdGroup() + " est creer", Toast.LENGTH_SHORT).show();
                                            //absence collection
                                            CollectionReference subcollectionRef = createDocument.collection("absence");
                                            createAbsence = subcollectionRef
                                                    .document("142023");
                                            Map<String, Object> subdata = new HashMap<>();
                                            createAbsence.set(subdata)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(GroupActivity.this, "Failed to create Group", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            dialog.cancel();
                        } else
                            nom_group.setError("svp entrer le nom de group");
                    }
                });

                annuler_ajout.setOnClickListener(v1 -> dialog.cancel());

                dialog.show();
            }
        });
    }

    public void getGroupData(String id) {
        //get Document Group
        //String IdGroup = "group" + index;
        String IdGroup = id;
        documentReference = firebaseHelper.getFireStoreReference(IdGroup);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //get Group data
                    Map<String, Object> data = documentSnapshot.getData();
                    Group group = new Group(IdGroup, groupImage, data.get("nomGroup").toString(),data.get("description").toString());
                    //affectation les groups avec le GridView
                    groups.add(group);
                    setGridAdapter();
                }
            }
        });
    }

    public void setGridAdapter() {
        gridAdapter = new GridAdapter(GroupActivity.this, groups);
        gridView.setAdapter(gridAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            firebaseHelper.logout();
            // Get the fragment manager for this activity
            FragmentManager fragmentManager = getSupportFragmentManager();

            // Get a list of all fragments currently attached to the activity
            List<Fragment> fragments = fragmentManager.getFragments();

            // Iterate over the list of fragments and call onDestroyView() on each  one
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onDestroyView();
                }
            }
            Intent intent = new Intent(this, Authentification.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.absentism);
        builder.setTitle("Quitter");
        builder.setMessage(R.string.msg);

        builder.setPositiveButton("Oui", (dialog, which) -> {
            firebaseHelper.logout();
            finish();
        });
        builder.setNegativeButton("Non", (dialog, which) -> {
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return super.onSupportNavigateUp();
    }
}