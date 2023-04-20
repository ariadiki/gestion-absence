package com.sadiki.gestionabsences.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {
    private String image;
    private String nomGroup;
    private String description;
    private String idGroup;
    private int firstID, lastID;
    private ArrayList<Membre> listMembres = new ArrayList<>();

    public Group() {
    }

    public Group(String idGroup, String image, String nomGroup,String description) {
        this.idGroup = idGroup;
        this.image = image;
        this.nomGroup = nomGroup;
        this.description = description;
        firstID = -1;
        lastID = -1;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public void ajouterMembre(Membre membre) {
        listMembres.add(membre);
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNomGroup() {
        return nomGroup;
    }

    public void setNomGroup(String nomGroup) {
        this.nomGroup = nomGroup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Membre> getListMembres() {
        return listMembres;
    }

    public int getFirstID() {
        return firstID;
    }

    public void setFirstID(int firstID) {
        this.firstID = firstID;
    }

    public int getLastID() {
        return lastID;
    }

    public void setLastID(int lastID) {
        this.lastID = lastID;
    }

    @Override
    public String toString() {
        return "Group{" +
                "image='" + image + '\'' +
                ", nomGroup='" + nomGroup + '\'' +
                ", listMembres=" + listMembres +
                '}';
    }
}
