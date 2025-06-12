package com.expense.service;

import com.expense.model.Expense;
import com.expense.model.User;

import java.util.List;
import java.util.Map;

public interface ExpenseService {
    void saveExpense(Expense expense);
    List<Expense> getAllExpenses(User user);
    Expense getExpenseById(Long id);
    void deleteExpense(Long id);
    double getTotalExpense(User user);

    /**
     * Calculate the monthly income for the user.
     * Assuming it's stored in the User object or another data source.
     */
    double getMonthlyIncome(User user);

    /**
     * Calculate the total expense for the current month for the user.
     */
    double getMonthlyExpense(User user);

    /**
     * Get total expenses categorized by the category field.
     */
    Map<String, Double> getExpensesByCategory(User user);
}
