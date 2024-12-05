package com.example.demo.service;

import com.example.demo.model.Transaction;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.List;

@Service
public class TransactionService {

    private static final String CSV_FILE_PATH = "src/main/resources/static/chuyen_khoan.csv";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    // Phương thức tìm kiếm giao dịch với phân trang và lọc theo từ khóa
    public List<Transaction> searchTransactions(String keyword, int page, int pageSize) {
        List<Transaction> filteredTransactions = new ArrayList<>();
    
        // Kiểm tra giá trị page và pageSize
        if (page < 1) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 20;
        }
    
        // Nếu từ khóa tìm kiếm là null hoặc rỗng, chỉ lấy giao dịch mới nhất
        if (keyword == null || keyword.isEmpty()) {
            return loadLatestTransactions(page, pageSize);
        }
    
        try (CSVReader csvReader = new CSVReader(new FileReader(CSV_FILE_PATH))) {
            String[] row;
            int skip = (page - 1) * pageSize;
            int currentIndex = 0;
    
            // Bỏ qua dòng tiêu đề
            csvReader.readNext();
    
            // Đọc từng dòng và xử lý giao dịch
            while ((row = csvReader.readNext()) != null) {
                if (row.length == 5) {
                    String dateTime = row[0];
                    int transNo = 0;
                    try {
                        transNo = Integer.parseInt(row[1]);  // Kiểm tra số giao dịch
                    } catch (NumberFormatException e) {
                        continue;  // Bỏ qua nếu transNo không hợp lệ
                    }
    
                    double credit = 0;
                    double debit = 0;
    
                    try {
                        credit = Double.parseDouble(row[2]);  // Kiểm tra số tiền credit
                    } catch (NumberFormatException e) {
                        // Không xử lý gì nếu lỗi, giá trị mặc định là 0
                    }
    
                    try {
                        debit = Double.parseDouble(row[3]);  // Kiểm tra số tiền debit
                    } catch (NumberFormatException e) {
                        // Không xử lý gì nếu lỗi, giá trị mặc định là 0
                    }
    
                    String detail = row[4];  // Chi tiết giao dịch
    
                    // Kiểm tra xem từ khóa có phải là ngày không
                    if (isDateSearch(keyword) && !dateTime.contains(keyword)) {
                        continue;  // Bỏ qua nếu ngày không chứa từ khóa
                    }
    
                    // Kiểm tra xem từ khóa có phải là số không
                    if (isNumberSearch(keyword)) {
                        double searchValue = Double.parseDouble(keyword);
                        if (!(credit == searchValue || debit == searchValue)) {
                            continue;  // Bỏ qua nếu giá trị credit hoặc debit không khớp
                        }
                    }
    
                  // Lọc theo chi tiết giao dịch (detail)
                    if (detail != null && keyword != null && !keyword.isEmpty() && !isDateSearch(keyword) && !isNumberSearch(keyword)) {
                        // Kiểm tra keyword có null hay không trước khi gọi toLowerCase
                        if (keyword != null && !detail.toLowerCase().contains(keyword.toLowerCase())) {
                            continue;  // Bỏ qua nếu detail không chứa từ khóa
                        }
                    }
    
                    // Thêm vào danh sách kết quả nếu thỏa mãn điều kiện và nằm trong phạm vi trang
                    if (currentIndex >= skip && currentIndex < skip + pageSize) {
                        filteredTransactions.add(new Transaction(dateTime, transNo, credit, debit, detail));
                    }
    
                    currentIndex++;
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    
        return filteredTransactions;
    }
    
    
    // Phương thức lấy các giao dịch mới nhất mà không có lọc
  public List<Transaction> loadLatestTransactions(int page, int pageSize) {
    List<Transaction> transactions = new ArrayList<>();

    try (CSVReader csvReader = new CSVReader(new FileReader(CSV_FILE_PATH))) {
        String[] row;
        List<Transaction> allTransactions = new ArrayList<>();

        // Đọc tất cả các giao dịch vào list
        while ((row = csvReader.readNext()) != null) {
            if (row.length == 5) {
                String dateTime = row[0];
                int transNo = 0;
                try {
                    // Kiểm tra giá trị transNo trước khi chuyển đổi
                    if (row[1] != null && !row[1].isEmpty()) {
                        transNo = Integer.parseInt(row[1]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Lỗi định dạng số cho cột 'trans_no': " + row[1]);
                    continue;  // Bỏ qua dòng này nếu trans_no không hợp lệ
                }

                double credit = 0;
                double debit = 0;

                try {
                    if (row[2] != null && !row[2].isEmpty()) {
                        credit = Double.parseDouble(row[2]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Lỗi định dạng số cho cột 'credit': " + row[2]);
                }

                try {
                    if (row[3] != null && !row[3].isEmpty()) {
                        debit = Double.parseDouble(row[3]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Lỗi định dạng số cho cột 'debit': " + row[3]);
                }

                String detail = row[4];
                allTransactions.add(new Transaction(dateTime, transNo, credit, debit, detail));
            }
        }

        // Đảm bảo sắp xếp theo thứ tự mới nhất (dựa vào transNo hoặc dateTime)
        allTransactions.sort((t1, t2) -> Integer.compare(t2.getTransNo(), t1.getTransNo()));

        // Phân trang: Lấy giao dịch mới nhất từ danh sách đã sắp xếp
        int start = Math.max(0, allTransactions.size() - page * pageSize);
        int end = Math.min(allTransactions.size(), start + pageSize);
        transactions = allTransactions.subList(start, end);

    } catch (IOException | CsvValidationException e) {
        e.printStackTrace();
    }

    return transactions;
}




    // Kiểm tra xem từ khóa có phải là ngày không
    private boolean isDateSearch(String keyword) {
        if (keyword == null) {
            return false;  // Tránh lỗi khi từ khóa là null
        }
        try {
            // Kiểm tra xem từ khóa có thể chuyển thành ngày không
            DATE_FORMAT.parse(keyword);
            return true;  // Nếu có thể chuyển thành ngày thì đây là một từ khóa tìm kiếm ngày
        } catch (ParseException e) {
            return false;  // Nếu không thể chuyển thành ngày, trả về false
        }
    }
    
    
    

    // Kiểm tra xem từ khóa có phải là số không
    private boolean isNumberSearch(String keyword) {
        if (keyword == null) {
            return false;  // Tránh lỗi khi từ khóa là null
        }
        try {
            Double.parseDouble(keyword);  // Kiểm tra xem từ khóa có phải là số không
            return true;
        } catch (NumberFormatException e) {
            return false;  // Nếu không phải số, trả về false
        }
    }
    

    // Phương thức tính tổng số giao dịch (bao gồm tìm kiếm từ khóa nếu có)
    public int getTotalTransactionCount(String keyword) {
        int count = 0;
        try (CSVReader csvReader = new CSVReader(new FileReader(CSV_FILE_PATH))) {
            String[] row;
            // Bỏ qua dòng tiêu đề
            csvReader.readNext();
    
            while ((row = csvReader.readNext()) != null) {
                if (row.length == 5) {
                    String dateTime = row[0];
                    String detail = row[4];
    
                    // Lọc theo ngày
                    if (isDateSearch(keyword) && !dateTime.contains(keyword)) {
                        continue;
                    }
    
                    // Lọc theo số (credit, debit)
                    if (isNumberSearch(keyword)) {
                        double credit = Double.parseDouble(row[2]);
                        double debit = Double.parseDouble(row[3]);
                        double searchValue = Double.parseDouble(keyword);
                        if (!(credit == searchValue || debit == searchValue)) {
                            continue;
                        }
                    }
    
                    // Lọc theo tên tổ chức trong detail
                    if (!isDateSearch(keyword) && !isNumberSearch(keyword) && keyword != null && !detail.toLowerCase().contains(keyword.toLowerCase())) {
                        continue;
                    }
    
                    count++; // Tăng số lượng giao dịch
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return count;
    }
    

    public List<Transaction> loadTransactionsFromCSV(int page, int pageSize) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadTransactionsFromCSV'");
    }
}
