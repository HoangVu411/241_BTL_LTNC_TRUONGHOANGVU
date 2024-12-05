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
                       @RequestParam(value = "page", defaultValue = "1") int page) {
    
        // Giới hạn số lượng giao dịch trên mỗi trang
        int pageSize = 20;
    
        // Kiểm tra nếu page nhỏ hơn 1, set lại thành 1
        if (page < 1) {
            page = 1;
        }
    
        // Tạo biến chứa danh sách giao dịch
        List<Transaction> transactions;
    
        // Nếu có từ khóa tìm kiếm và từ khóa không rỗng
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Tìm kiếm giao dịch theo từ khóa và phân trang
            transactions = transactionService.searchTransactions(keyword, page, pageSize);
        } else {
            // Nếu không có từ khóa tìm kiếm, lấy danh sách giao dịch mới nhất
            transactions = transactionService.loadLatestTransactions(page, pageSize);
        }
    
        // Tính toán tổng số giao dịch và số trang
        int totalTransactions = transactionService.getTotalTransactionCount(keyword);
        int totalPages = (int) Math.ceil((double) totalTransactions / pageSize);
    
        // Thêm các thuộc tính vào model
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", keyword);  // Gửi từ khóa tìm kiếm vào view
    
        return "index";  // Trả về view "index.html"
    }
    
}

