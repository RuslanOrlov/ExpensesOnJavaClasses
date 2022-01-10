/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expenses.expenses;

import java.io.Serializable;

/**
 * Класс подробностей о покупках и активах
 * @author User
 */
public class Details  implements Serializable {
    private String  name;           // наименование
    private int     quantity;       // количество
    private String  measure;        // единица измерения

    public Details() {
    }
    public Details(String name, int quantity, String measure) {
        this.name = name;
        this.quantity = quantity;
        this.measure = measure;
    }

    public String getName() {
        return name;
    }
    public int getQuantity() {
        return quantity;
    }
    public String getMeasure() {
        return measure;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void setMeasure(String measure) {
        this.measure = measure;
    }
}