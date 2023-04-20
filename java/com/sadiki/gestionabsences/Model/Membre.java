package com.sadiki.gestionabsences.Model;

import java.io.Serializable;

public class Membre implements Serializable {
    private int idMembre;
    private String nomMembre;
    private String prenomMembre;
    private String imgMembre;
    private String genre;
    private String dateNaissance;
    private String presence;
    private String datePresence;
    private String idGroup;

    public Membre(){
        this.datePresence="";
        this.presence="null";
    }

    public Membre(int idMembre, String nomMembre,String prenomMembre,String genre, String imgMembre,String dateNaissance,String idGroup) {
        this.idMembre = idMembre;
        this.nomMembre = nomMembre;
        this.prenomMembre = prenomMembre;
        this.genre = genre;
        this.imgMembre = imgMembre;
        this.dateNaissance = dateNaissance;
        this.idGroup = idGroup;
        this.datePresence="";
        this.presence="null";
    }

    public int getIdMembre() {
        return idMembre;
    }

    public void setIdMembre(int idMembre) {
        this.idMembre = idMembre;
    }

    public String getNomMembre() {
        return nomMembre;
    }

    public void setNomMembre(String nomMembre) {
        this.nomMembre = nomMembre;
    }

    public String getPrenomMembre() {
        return prenomMembre;
    }

    public void setPrenomMembre(String prenomMembre) {
        this.prenomMembre = prenomMembre;
    }

    public String nomComplet()
    {
        return this.nomMembre+" "+this.prenomMembre;
    }
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }

    public String getDatePresence() {
        return datePresence;
    }

    public void setDatePresence(String datePresence) {
        this.datePresence = datePresence;
    }

    public String getImgMembre() {
        return imgMembre;
    }

    public void setImgMembre(String imgMembre) {
        this.imgMembre = imgMembre;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    @Override
    public String toString() {
        return "Membre{" +
                "idMembre=" + idMembre +
                ", nomMembre='" + nomMembre + '\'' +
                ", imgMembre='" + imgMembre + '\'' +
                ", genre=" + genre +
                ", present=" + presence +
                ", datePresence='" + datePresence + '\'' +
                '}';
    }
}
