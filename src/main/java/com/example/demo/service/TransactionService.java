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
        page = (page < 1) ? 1 : page; // Đảm bảo page không nhỏ hơn 1
        pageSize = (pageSize <= 0) ? 100 : pageSize; // Đảm bảo pageSize lớn hơn 0

        // Nếu từ khóa tìm kiếm là null hoặc rỗng, chỉ lấy giao dịch mới nhất
        if (keyword == null || keyword.isEmpty()) {
            return loadLatestTransactions(page, pageSize);
        }

        try (CSVReader csvReader = new CSVReader(new FileReader(CSV_FILE_PATH))) {
            String[] row;
            int skip = (page - 1) * pageSize;  // Số dòng cần bỏ qua
            int currentIndex = 0;

            // Bỏ qua dòng tiêu đề
            csvReader.readNext();

            // Đọc từng dòng và xử lý giao dịch
            while ((row = csvReader.readNext()) != null) {
                if (row.length == 5) {
                    // Lọc theo từng điều kiện tìm kiếm
                    Transaction transaction = parseTransaction(row);
                    if (transaction == null) continue;

                    // Kiểm tra ngày nếu là tìm kiếm theo ngày
                    if (isDateSearch(keyword) && !transaction.getDateTime().contains(keyword)) {
                        continue;
                    }

                    // Kiểm tra nếu là tìm kiếm số
                    if (isNumberSearch(keyword)) {
                        double searchValue = Double.parseDouble(keyword);
                        if (!(transaction.getCredit() == searchValue || transaction.getDebit() == searchValue)) {
                            continue;
                        }
                    }

                    // Lọc theo chi tiết giao dịch
                    if (!isDateSearch(keyword) && !isNumberSearch(keyword) && !transaction.getDetail().toLowerCase().contains(keyword.toLowerCase())) {
                        continue;
                    }

                    // Thêm vào danh sách kết quả nếu thỏa mãn điều kiện và nằm trong phạm vi trang
                    if (currentIndex >= skip && currentIndex < skip + pageSize) {
                        filteredTransactions.add(transaction);
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
                    Transaction transaction = parseTransaction(row);
                    if (transaction != null) {
                        allTransactions.add(transaction);
                    }
                }
            }

            // Sắp xếp theo số giao dịch (hoặc thời gian nếu muốn)
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

    // Phương thức giúp chuyển đổi mảng row thành đối tượng Transaction
    private Transaction parseTransaction(String[] row) {
        try {
            String dateTime = row[0].length() >= 10 ? row[0].substring(0, 10) : row[0];  // Lấy 10 ký tự đầu tiên (Ngày tháng năm)
            int transNo = Integer.parseInt(row[1]);
            int credit = Integer.parseInt(row[2]);
            int debit = Integer.parseInt(row[3]);
            String detail = row[4];
            return new Transaction(dateTime, transNo, credit, debit, detail);
        } catch (NumberFormatException e) {
            return null; // Trả về null nếu có lỗi khi chuyển đổi dữ liệu
        }
    }

    // Kiểm tra xem từ khóa có phải là ngày không
    private boolean isDateSearch(String keyword) {
        if (keyword == null) return false;
        try {
            DATE_FORMAT.parse(keyword);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // Kiểm tra xem từ khóa có phải là số không
    private boolean isNumberSearch(String keyword) {
        if (keyword == null) return false;
        try {
            Double.parseDouble(keyword);
            return true;
        } catch (NumberFormatException e) {
            return false;
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
                    Transaction transaction = parseTransaction(row);
                    if (transaction == null) continue;

                    // Lọc theo ngày
                    if (isDateSearch(keyword) && !transaction.getDateTime().contains(keyword)) {
                        continue;
                    }

                    // Lọc theo số (credit, debit)
                    if (isNumberSearch(keyword)) {
                        int searchValue = Integer.parseInt(keyword);
                        if (!(transaction.getCredit() == searchValue || transaction.getDebit() == searchValue)) {
                            continue;
                        }
                    }

                    // Lọc theo tên tổ chức trong detail
                    if (!isDateSearch(keyword) && !isNumberSearch(keyword) && keyword != null && !transaction.getDetail().toLowerCase().contains(keyword.toLowerCase())) {
                        continue;
                    }

                    count++;
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return count;
    }
}