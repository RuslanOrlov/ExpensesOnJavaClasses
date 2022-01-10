/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expenses.expenses;

import java.io.Serializable;
import java.util.*;

/**
 * Класс доходов
 * @author Ruslan Orlov
 **/
public class Income implements Serializable {
    private int     number;         // порядковый номер
    private Date    date;           // дата
    private float   sum;            // сумма
    private String  kindOfIncome;   // вид доходов      - потом сделать класс
    private String  incomeSource;   // источник дохода  - потом сделать класс
    private String  description;    // описание

    public Income() {
    }
    public Income(int number, Date date, float sum, 
            String kindOfIncome, String incomeSource, 
            String description) {
        this.number = number;
        this.date   = date;
        this.sum    = sum;
        this.kindOfIncome = kindOfIncome;
        this.incomeSource = incomeSource;
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
    public String getKindOfIncome() {
        return kindOfIncome;
    }
    public String getIncomeSource() {
        return incomeSource;
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
    public void setKindOfIncome(String kindOfIncome) {
        this.kindOfIncome = kindOfIncome;
    }
    public void setIncomeSource(String incomeSource) {
        this.incomeSource = incomeSource;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
