package com.sadiki.gestionabsences.Firebase;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseHelper {

    private static FirebaseHelper instance;
    private final FirebaseAuth mAuth;
    private final FirebaseDatabase mDatabase;
    private final FirebaseFirestore mFirestore;
    private final FirebaseStorage mStorage;

    private FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public static FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public CollectionReference getCollectionReference(String ref)
    {
        return mFirestore.collection(ref);
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public DocumentReference getFireStoreReference(String reference) {
        return mFirestore.collection("groupes").document(reference);
    }

    public DatabaseReference getDatabaseReference(String reference) {
        return mDatabase.getReference(reference);
    }

    public FirebaseStorage getStorage() {
        return mStorage;
    }

    public StorageReference getStorageReference(String reference) {
        return mStorage.getReference(reference);
    }

    public void logout()
    {
        mAuth.signOut();
    }
}

