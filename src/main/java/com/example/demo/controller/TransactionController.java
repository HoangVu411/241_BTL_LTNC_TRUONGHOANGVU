package com.example.demo.controller;

import com.example.demo.model.Transaction;
import com.example.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(value = "search", required = false) String keyword,
                       @RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
    
        // Kiểm tra nếu page nhỏ hơn 1, set lại thành 1
        if (page < 1) {
            page = 1;
        }
    
        // Kiểm tra nếu page vượt quá tổng số trang
        int totalTransactions = transactionService.getTotalTransactionCount(keyword);
        int totalPages = (int) Math.ceil((double) totalTransactions / pageSize);
        if (page > totalPages) {
            page = totalPages;  // Điều chỉnh page nếu vượt quá tổng số trang
        }
    
        List<Transaction> transactions;
        
        // Nếu có từ khóa tìm kiếm và từ khóa không rỗng
        if (keyword != null && !keyword.trim().isEmpty()) {
            transactions = transactionService.searchTransactions(keyword.trim(), page, pageSize);
        } else {
            transactions = transactionService.loadLatestTransactions(page, pageSize);
        }
    
        // Thêm các thuộc tính vào model
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", keyword);
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("noResults", transactions.isEmpty());
        model.addAttribute("pageSize", pageSize);
    
        return "index";  // Trả về view "index.html"
    }

}
    
