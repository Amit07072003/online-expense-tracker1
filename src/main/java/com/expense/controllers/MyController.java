package com.expense.controllers;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.expense.model.Expense;
import com.expense.model.PasswordResetToken;
import com.expense.model.User;
import com.expense.repository.PasswordResetTokenRepository;
import com.expense.repository.UserRepository;
import com.expense.service.EmailService;
import com.expense.service.ExpenseService;
import com.expense.service.UserService;
import java.time.LocalDate;


@Controller
public class MyController {

    @Autowired
    private PasswordResetTokenRepository tokenRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private EmailService emailService;

    // Home
    @GetMapping("/")
    public String homePage() {
        return "home";
    }

    //about


    @GetMapping("/about")
    public String aboutPage() {
        // Returns the Thymeleaf template named "about.html"
        return "about";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/terms")
    public String showTermsPage() {
        return "terms";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user, Model model) {
        if (userService.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "Email already registered");
            return "register";
        }
        userService.register(user);
        return "redirect:/login";
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findByEmail(userDetails.getUsername());

        double monthlyIncome = expenseService.getMonthlyIncome(user);
        double totalExpense = expenseService.getTotalExpense(user);
        double remainingBalance = monthlyIncome - totalExpense;

        Map<String, Double> categoryDataMap = expenseService.getExpensesByCategory(user);

        model.addAttribute("loggedInUser", user);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("remainingBalance", remainingBalance);
        model.addAttribute("categoryLabels", new ArrayList<>(categoryDataMap.keySet()));
        model.addAttribute("categoryValues", new ArrayList<>(categoryDataMap.values()));

        return "dashboard";
    }

    @GetMapping("/add-expense")
    public String showAddExpenseForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        Expense expense = new Expense();
        expense.setAmount(null); // Ensure no default value appears in the input field

        model.addAttribute("expense", expense);
        return "add-expense";
    }


    @PostMapping("/save-expense")
    public String saveExpense(@ModelAttribute Expense expense, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findByEmail(userDetails.getUsername());
        expense.setUser(user);
        expenseService.saveExpense(expense);
        return "redirect:/view-expenses";
    }

    @GetMapping("/view-expenses")
    public String viewExpenses(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findByEmail(userDetails.getUsername());
        List<Expense> expenses = expenseService.getAllExpenses(user);
        model.addAttribute("expenses", expenses);

        return "view-expense";
    }

    @GetMapping("/edit-expense")
    public String editExpense(@RequestParam Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findByEmail(userDetails.getUsername());
        Expense expense = expenseService.getExpenseById(id);

        if (expense != null && expense.getUser().getId().equals(user.getId())) {
            model.addAttribute("expense", expense);
            return "add-expense";
        } else {
            return "redirect:/view-expenses";
        }
    }

    @GetMapping("/delete-expense")
    public String deleteExpense(@RequestParam Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findByEmail(userDetails.getUsername());
        Expense expense = expenseService.getExpenseById(id);

        if (expense != null && expense.getUser().getId().equals(user.getId())) {
            expenseService.deleteExpense(id);
        }
        return "redirect:/view-expenses";
    }

    // ========================
    // Password Reset Section
    // ========================

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "No user found with that email.");
            return "forgot-password";
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        Optional<PasswordResetToken> existingTokenOpt = tokenRepo.findByUser(user);

        PasswordResetToken resetToken = existingTokenOpt.orElse(new PasswordResetToken());
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(expiry);
        tokenRepo.save(resetToken);

        String resetLink = "http://localhost:8080/resetpassword?token=" + token;
        System.out.println("Password reset link: " + resetLink); // TODO: replace with email logic
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        model.addAttribute("message", "Password reset link sent to your email.");
        return "forgot-password";
    }

    @GetMapping("/resetpassword")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "resetpassword";
    }

    @PostMapping("/resetpassword")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String newPassword,
                                       Model model) {

        Optional<PasswordResetToken> resetTokenOpt = tokenRepo.findByToken(token);
        if (resetTokenOpt.isEmpty()) {
            model.addAttribute("error", "Invalid token");
            return "resetpassword";
        }

        PasswordResetToken resetToken = resetTokenOpt.get();
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Token expired");
            return "resetpassword";
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepo.delete(resetToken); // optional: delete token after use

        model.addAttribute("message", "Password reset successful!");
        return "login";
    }


    //quick add button
    @PostMapping("/quick-add-expense")
    public String quickAddExpense(
            @RequestParam Double amount,
            @RequestParam String category,
            @RequestParam(required = false) String title,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) return "redirect:/login";

        Expense expense = new Expense();
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setTitle(title != null ? title.trim() : "Untitled");
        expense.setDate(LocalDate.now());
        expense.setUser(userService.findByEmail(userDetails.getUsername())); // Or however you set user

        expenseService.saveExpense(expense);

        return "redirect:/dashboard"; // or wherever you want to go after quick add
    }


}
