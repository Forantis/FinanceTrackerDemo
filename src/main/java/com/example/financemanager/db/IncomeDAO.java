package com.example.financemanager.db;

import com.example.financemanager.model.Income;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class IncomeDAO {

    private static final Logger log = LoggerFactory.getLogger(IncomeDAO.class);

    private static final String tableName = "income";
    private static final String dateColumn = "date";
    private static final String salaryColumn = "salary";
    private static final String aidColumn = "aid";
    private static final String freelanceIncomeColumn = "freelanceIncome";
    private static final String passiveIncomeColumn = "passiveIncome";
    private static final String otherIncomeColumn = "otherIncome";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final ObservableList<Income> incomes;

    static {
        incomes = FXCollections.observableArrayList();
        fetchAllDataFromDB();
    }

    public static ObservableList<Income> getIncomes() {
        return FXCollections.unmodifiableObservableList(incomes.sorted(Income::compareTo));
    }

    private static void fetchAllDataFromDB() {
        String query = "SELECT * FROM " + tableName;

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            incomes.clear();
            while (rs.next()) {
                incomes.add(new Income(
                        rs.getFloat(salaryColumn),
                        rs.getFloat(aidColumn),
                        rs.getFloat(freelanceIncomeColumn),
                        rs.getFloat(passiveIncomeColumn),
                        rs.getFloat(otherIncomeColumn),
                        LocalDate.parse(rs.getString(dateColumn), DATE_FORMAT)));
            }
        } catch (SQLException e) {
            log.error("Could not load Incomes from database", e);
            incomes.clear();
        }
    }

    public static void insertIncome(Income income) {
        //update database
        String query = "INSERT INTO " + tableName + "(" +
                dateColumn + ", " +
                salaryColumn + ", " +
                aidColumn + ", " +
                freelanceIncomeColumn + ", " +
                passiveIncomeColumn + ", " +
                otherIncomeColumn +
                ") VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, income.getDate().format(DATE_FORMAT));
            statement.setFloat(2, income.getSalary());
            statement.setFloat(3, income.getAid());
            statement.setFloat(4, income.getFreelanceIncome());
            statement.setFloat(5, income.getPassiveIncome());
            statement.setFloat(6, income.getOtherIncome());

            statement.executeUpdate();
            log.info("Successfully inserted income for date: {}", income.getDate());
        } catch (SQLException e) {
            log.error("Could not insert Income in database. Error: {}", e.getMessage(), e);
        }

        //update cache
        incomes.add(income);
    }

    public static List<Income> findLastIncomesEndingAtCurrentMonth(int numberOfLine, LocalDate currentMonth) {
        String query = "SELECT * FROM " + tableName
                + " WHERE " + dateColumn + " <= '" + currentMonth.format(DATE_FORMAT)
                + "' ORDER BY " + dateColumn + " DESC LIMIT " + numberOfLine;

        List<Income> lastIncomes = new ArrayList<>();

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                lastIncomes.add(new Income(
                        rs.getFloat(salaryColumn),
                        rs.getFloat(aidColumn),
                        rs.getFloat(freelanceIncomeColumn),
                        rs.getFloat(passiveIncomeColumn),
                        rs.getFloat(otherIncomeColumn),
                        LocalDate.parse(rs.getString(dateColumn), DATE_FORMAT)));
            }
        } catch (SQLException e) {
            log.error("Could not load Incomes from database", e);
        }
        return lastIncomes;
    }

    /**
     * Trouve les revenus entre deux dates spécifiques
     * @param startDate Date de début (incluse)
     * @param endDate Date de fin (incluse)
     * @return Liste des revenus entre les dates spécifiées
     */
    public static List<Income> findIncomesBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<Income> result = new ArrayList<>();
        String query = "SELECT * FROM " + tableName + 
                       " WHERE " + dateColumn + " >= '" + startDate.format(DATE_FORMAT) + "'" +
                       " AND " + dateColumn + " <= '" + endDate.format(DATE_FORMAT) + "'" +
                       " ORDER BY " + dateColumn + " DESC";

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                result.add(new Income(
                        rs.getFloat(salaryColumn),
                        rs.getFloat(aidColumn),
                        rs.getFloat(freelanceIncomeColumn),
                        rs.getFloat(passiveIncomeColumn),
                        rs.getFloat(otherIncomeColumn),
                        LocalDate.parse(rs.getString(dateColumn), DATE_FORMAT)));
            }
        } catch (SQLException e) {
            log.error("Could not load incomes between dates from database", e);
        }
        
        return result;
    }
}