package com.expense.service;

import com.expense.model.Expense;
import com.expense.model.User;
import com.expense.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public void saveExpense(Expense expense) {
        expenseRepository.save(expense);
    }

    @Override
    public List<Expense> getAllExpenses(User user) {
        return expenseRepository.findByUser(user);
    }

    @Override
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    @Override
    public double getTotalExpense(User user) {
        return expenseRepository.findByUser(user).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    /**
     * Calculate the monthly income for the user.
     * Assuming it's stored in the User object.
     */
    @Override
    public double getMonthlyIncome(User user) {
        return user.getMonthlyIncome(); // This should be part of the User model.
    }

    /**
     * Calculate the total expense for the current month for the user.
     */
    @Override
    public double getMonthlyExpense(User user) {
        // Get current month's date range
        LocalDate now = LocalDate.now();
        return expenseRepository.findByUser(user).stream()
                .filter(expense -> expense.getDate().getMonth() == now.getMonth() &&
                        expense.getDate().getYear() == now.getYear())
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    /**
     * Get total expenses categorized by the category field.
     */
    @Override
    public Map<String, Double> getExpensesByCategory(User user) {
        List<Expense> expenses = expenseRepository.findByUser(user);
        Map<String, Double> categoryData = new HashMap<>();
        
        // Categorize the expenses by category
        for (Expense expense : expenses) {
            categoryData.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }
        return categoryData;
    }
}
