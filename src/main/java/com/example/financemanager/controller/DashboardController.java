package com.example.financemanager.controller;

import com.example.financemanager.db.ExpenseDAO;
import com.example.financemanager.db.IncomeDAO;
import com.example.financemanager.model.Expense;
import com.example.financemanager.model.Income;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML
    private PieChart pieChart;

    @FXML
    private LineChart<String, Float> lineChart;

    @FXML
    private BarChart<String, Number> compareChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private CategoryAxis compareXAxis;

    @FXML
    private NumberAxis compareYAxis;

    @FXML
    private ChoiceBox<String> periodChoiceBox;

    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM yy");
    private final static DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

    public void initialize() {
        LocalDate date = LocalDate.now();

        loadExpensesAndIncomes(date);

        for (int i = 0; i < 12; i++) {
            periodChoiceBox.getItems().add(date.format(FULL_DATE_FORMAT));
            date = date.minusMonths(1);
        }
        periodChoiceBox.getSelectionModel().selectFirst();
    }

    private void loadExpensesAndIncomes(LocalDate currentMonth) {
        // Récupérer les données avec une plage suffisamment grande pour couvrir le mois sélectionné et les 4 mois précédents
        YearMonth selectedYearMonth = YearMonth.from(currentMonth);
        YearMonth startYearMonth = selectedYearMonth.minusMonths(4);
        LocalDate startDate = startYearMonth.atDay(1);
        LocalDate endDate = selectedYearMonth.atEndOfMonth();
        
        // Utiliser une période personnalisée qui englobe exactement les 5 mois nécessaires
        List<Expense> periodExpenses = ExpenseDAO.findExpensesBetweenDates(startDate, endDate);
        List<Income> periodIncomes = IncomeDAO.findIncomesBetweenDates(startDate, endDate);
        
        if (periodExpenses.isEmpty() && periodIncomes.isEmpty()) {
            return;
        }

        // Filtrer les dépenses du mois sélectionné pour le PieChart
        LocalDate startOfSelectedMonth = selectedYearMonth.atDay(1);
        LocalDate endOfSelectedMonth = selectedYearMonth.atEndOfMonth();
        
        List<Expense> currentMonthExpenses = periodExpenses.stream()
                .filter(e -> !e.getDate().isBefore(startOfSelectedMonth) && !e.getDate().isAfter(endOfSelectedMonth))
                .collect(Collectors.toList());
                
        updatePieChart(currentMonthExpenses.isEmpty() ? periodExpenses : currentMonthExpenses);
        updateLineChart(periodExpenses);
        updateCompareChart(periodExpenses, periodIncomes);
    }

    private void updatePieChart(List<Expense> expenses) {
        if (expenses.isEmpty()) return;

        pieChart.getData().clear();
        pieChart.getData().addAll(
                new PieChart.Data("Logement", expenses.getFirst().getHousing()),
                new PieChart.Data("Nourriture", expenses.getFirst().getFood()),
                new PieChart.Data("Sortie", expenses.getFirst().getGoingOut()),
                new PieChart.Data("Transport", expenses.getFirst().getTransportation()),
                new PieChart.Data("Voyage", expenses.getFirst().getTravel()),
                new PieChart.Data("Impôts", expenses.getFirst().getTax()),
                new PieChart.Data("Autres", expenses.getFirst().getOther())
        );
    }

    private void updateLineChart(List<Expense> expenses) {
        if (expenses.isEmpty()) return;

        lineChart.getData().clear();

        XYChart.Series<String, Float> seriesHousing = new XYChart.Series<>();
        seriesHousing.setName("Logement");
        XYChart.Series<String, Float> seriesFood = new XYChart.Series<>();
        seriesFood.setName("Nourriture");
        XYChart.Series<String, Float> seriesGoingOut = new XYChart.Series<>();
        seriesGoingOut.setName("Sortie");
        XYChart.Series<String, Float> seriesTransportation = new XYChart.Series<>();
        seriesTransportation.setName("Transport");
        XYChart.Series<String, Float> seriesTravel = new XYChart.Series<>();
        seriesTravel.setName("Voyage");
        XYChart.Series<String, Float> seriesTax = new XYChart.Series<>();
        seriesTax.setName("Impôts");
        XYChart.Series<String, Float> seriesOther = new XYChart.Series<>();
        seriesOther.setName("Autres");

        expenses.stream().sorted(Comparator.comparing(Expense::getDate)).forEach(expense -> {
            String dateStr = expense.getDate().format(DATE_FORMAT);
            seriesHousing.getData().add(new XYChart.Data<>(dateStr, expense.getHousing()));
            seriesFood.getData().add(new XYChart.Data<>(dateStr, expense.getFood()));
            seriesGoingOut.getData().add(new XYChart.Data<>(dateStr, expense.getGoingOut()));
            seriesTransportation.getData().add(new XYChart.Data<>(dateStr, expense.getTransportation()));
            seriesTravel.getData().add(new XYChart.Data<>(dateStr, expense.getTravel()));
            seriesTax.getData().add(new XYChart.Data<>(dateStr, expense.getTax()));
            seriesOther.getData().add(new XYChart.Data<>(dateStr, expense.getOther()));
        });

        lineChart.getData().addAll(
                seriesHousing,
                seriesFood,
                seriesGoingOut,
                seriesTransportation,
                seriesTravel,
                seriesTax,
                seriesOther
        );
    }

    private void updateCompareChart(List<Expense> expenses, List<Income> incomes) {
        compareChart.getData().clear();

        // Créer les séries de données
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Dépenses");

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Revenus");

        // Récupérer le mois sélectionné
        YearMonth currentMonth = YearMonth.now();
        String selectedPeriod = periodChoiceBox.getSelectionModel().getSelectedItem();
        if (selectedPeriod != null && !selectedPeriod.isEmpty()) {
            LocalDate selectedDate = LocalDate.parse("01 " + selectedPeriod, DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            currentMonth = YearMonth.from(selectedDate);
        }
        
        // Créer une liste des mois à afficher (mois sélectionné + 4 mois précédents)
        List<YearMonth> monthsToDisplay = new ArrayList<>();
        
        // Inclure le mois sélectionné et les 4 mois précédents
        for (int i = 0; i <= 4; i++) {
            monthsToDisplay.add(currentMonth.minusMonths(i));
        }
        
        // Trier les mois par ordre chronologique
        Collections.sort(monthsToDisplay);
        
        // Agréger les données par mois
        Map<YearMonth, Double> expensesByMonth = new HashMap<>();
        Map<YearMonth, Double> incomesByMonth = new HashMap<>();
        
        for (Expense expense : expenses) {
            YearMonth month = YearMonth.from(expense.getDate());
            if (monthsToDisplay.contains(month)) {
                expensesByMonth.merge(month, (double) expense.getTotal(), Double::sum);
            }
        }
        
        for (Income income : incomes) {
            YearMonth month = YearMonth.from(income.getDate());
            if (monthsToDisplay.contains(month)) {
                incomesByMonth.merge(month, (double) income.getTotalIncome(), Double::sum);
            }
        }
        
        // Ajouter les données au graphique dans l'ordre chronologique
        for (YearMonth month : monthsToDisplay) {
            String monthLabel = month.format(DateTimeFormatter.ofPattern("MMM yy"));
            
            double expenseAmount = expensesByMonth.getOrDefault(month, 0.0);
            XYChart.Data<String, Number> expenseData = new XYChart.Data<>(monthLabel, expenseAmount);
            expenseSeries.getData().add(expenseData);
            
            double incomeAmount = incomesByMonth.getOrDefault(month, 0.0);
            XYChart.Data<String, Number> incomeData = new XYChart.Data<>(monthLabel, incomeAmount);
            incomeSeries.getData().add(incomeData);
        }
        
        compareChart.getData().addAll(expenseSeries, incomeSeries);
        
        // Force JavaFX à mettre à jour le graphique immédiatement
        compareChart.layout();
        
        // Appliquer les tooltips après un court délai pour s'assurer que les nœuds sont créés
        javafx.application.Platform.runLater(() -> {
            // Attendre que JavaFX finisse le rendu
            try {
                // Petit délai pour laisser JavaFX terminer
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Appliquer manuellement les tooltips à chaque nœud
            for (int i = 0; i < compareChart.getData().size(); i++) {
                XYChart.Series<String, Number> series = compareChart.getData().get(i);
                String seriesName = series.getName();
                
                for (int j = 0; j < series.getData().size(); j++) {
                    XYChart.Data<String, Number> data = series.getData().get(j);
                    
                    if (data.getNode() != null) {
                        final double value = data.getYValue().doubleValue();
                        final Tooltip tooltip = new Tooltip(String.format("%s: %.2f €", seriesName, value));
                        tooltip.setStyle("-fx-font-size: 14px;");
                        
                        javafx.scene.Node node = data.getNode();
                        Tooltip.install(node, tooltip);
                        
                        // Ajouter un effet visuel explicite au survol
                        final javafx.scene.Node finalNode = node;
                        node.setOnMouseEntered(event -> {
                            finalNode.setStyle("-fx-background-color: orange;");
                            tooltip.show(finalNode, event.getScreenX(), event.getScreenY() + 15);
                        });
                        node.setOnMouseExited(event -> {
                            finalNode.setStyle("");
                            tooltip.hide();
                        });
                    }
                }
            }
        });
    }

    public void changePeriod(ActionEvent actionEvent) {
        var periodSelected = periodChoiceBox.getSelectionModel().getSelectedItem();
        LocalDate dateSelected = LocalDate.parse("01 " + periodSelected, DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        loadExpensesAndIncomes(dateSelected);
    }
}