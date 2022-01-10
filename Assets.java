/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expenses.expenses;

import java.io.Serializable;
import java.util.*;

/**
 * Класс активов
 * @author Ruslan Orlov
 **/
public class Assets implements Serializable {
    private int     number;             // порядковый номер
    private Date    date;               // дата
    private float   sum;                // сумма
    private ArrayList<Details> details; // подробности о покупках
    private String  kindOfAssets;       // вид актива                      - потом сделать класс
    private String  orgsOrPerson;       // где или у кого приобретен актив - потом сделать класс
    private String  description;        // описание

    public Assets() {
    }
    public Assets(int number, Date date, float sum, 
            ArrayList<Details> details, String kindOfAssets, 
            String orgsOrPerson, String description) {
        this.number     = number;
        this.date       = date;
        this.sum        = sum;
        this.details    = details;
        this.kindOfAssets = kindOfAssets;
        this.orgsOrPerson = orgsOrPerson;
        this.description  = description;
    }
    
    public int getNumber() {
        return number;
    }
    public Date getDate() {
        return date;
    }
    public float getSum() {
        return sum;
    }
    public ArrayList<Details> getDetails() {
        return details;
    }
    public String getKindOfAssets() {
        return kindOfAssets;
    }
    public String getOrgsOrPerson() {
        return orgsOrPerson;
    }
    public String getDescription() {
        return description;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public void setSum(float sum) {
        this.sum = sum;
    }
    public void setDetails(ArrayList<Details> details) {
        this.details = details;
    }
    public void setKindOfAssets(String kindOfAssets) {
        this.kindOfAssets = kindOfAssets;
    }
    public void setOrgsOrPerson(String orgsOrPerson) {
        this.orgsOrPerson = orgsOrPerson;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}