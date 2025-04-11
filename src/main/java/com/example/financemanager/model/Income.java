package com.example.financemanager.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Income {
    private final LocalDate date;
    private final float total;
    private final float salary;
    private final float aid;
    private final float freelanceIncome;
    private final float passiveIncome;
    private final float otherIncome;
    private boolean isDollar = false;
    private float changeRate = 1.0f;

    private final static String PRICE_FORMAT = "%.2f €";
    private final static String DOLLAR_PRICE_FORMAT = "$%.2f";

    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

    public Income(float salary, float aid, float freelanceIncome, float passiveIncome, float otherIncome, LocalDate date) {
        this.date = date;
        this.salary = salary;
        this.total = salary + aid + freelanceIncome + passiveIncome + otherIncome;
        this.aid = aid;
        this.freelanceIncome = freelanceIncome;
        this.passiveIncome = passiveIncome;
        this.otherIncome = otherIncome;
    }

    public StringProperty dateProperty() {
        return new SimpleStringProperty(date.format(DATE_FORMAT));
    }

    public StringProperty totalProperty() {
        return formatAmount(total);
    }

    private SimpleStringProperty formatAmount(float amount) {
        if (isDollar) {
            return new SimpleStringProperty(String.format(DOLLAR_PRICE_FORMAT, amount*changeRate));
        }
        return new SimpleStringProperty(String.format(PRICE_FORMAT, amount));
    }

    public StringProperty salaryProperty() { return formatAmount(salary); }
    public StringProperty aidProperty() { return formatAmount(aid); }
    public StringProperty freelanceIncomeProperty() { return formatAmount(freelanceIncome); }
    public StringProperty passiveIncomeProperty() { return formatAmount(passiveIncome); }
    public StringProperty otherIncomeProperty() { return formatAmount(otherIncome); }

    public float getSalary() { return  salary; }
    public float getAid() { return aid; }
    public float getFreelanceIncome() { return freelanceIncome; }
    public float getPassiveIncome() { return passiveIncome; }
    public float getOtherIncome() { return otherIncome; }
    public LocalDate getDate() { return date; }
    public float getTotalIncome() { return total; }

    public int compareTo(Income income) {
        return -this.date.compareTo(income.date);
    }

    public void switchCurrency(float changeRate) {
        isDollar = !isDollar;
        this.changeRate = changeRate;
    }

}