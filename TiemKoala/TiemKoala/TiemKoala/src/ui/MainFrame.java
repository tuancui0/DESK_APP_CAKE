package ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import utils.DBConnection;
import ui.InvoicePanel;

public class MainFrame extends JFrame {
    private JPanel contentPanel;
    private Connection cnn;

    public MainFrame() {
        this(null); // Gọi constructor với Connection mặc định
    }

    public MainFrame(Connection conn) {
        this.cnn = (conn != null) ? conn : initializeConnection();
        initUI();
    }

    private Connection initializeConnection() {
        try {
            DBConnection dbc = new DBConnection();
            return dbc.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage() + "\nVui lòng kiểm tra cấu hình!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Thoát nếu không kết nối được
            return null; // Không bao giờ đến đây, nhưng cần để biên dịch
        }
    }

    private void initUI() {
        setTitle("Quản lý Cửa Hàng Bánh Ngọt Anh Hòa");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Banner tại top
        JPanel bannerPanel = new JPanel();
        bannerPanel.setBackground(new Color(255, 182, 193));
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel bannerLabel = new JLabel("Chào mừng bạn đến với tiệm bánh Anh Hòa của chúng tôi", SwingConstants.CENTER);
        bannerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        bannerLabel.setForeground(Color.WHITE);
        bannerPanel.add(bannerLabel);

        // Panel điều hướng
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(11, 1, 5, 5));
        navPanel.setBackground(new Color(255, 228, 225));
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Thêm logo
        URL imageUrl = getClass().getResource("/Image/logo.png");
        if (imageUrl != null) {
            ImageIcon logoIcon = new ImageIcon(imageUrl);
            Image logoImg = logoIcon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(logoImg));
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            navPanel.add(logoLabel);
        } else {
            JLabel logoLabel = new JLabel("CỬA HÀNG BÁNH NGỌT ANH HÒA", SwingConstants.CENTER);
            logoLabel.setFont(new Font("Arial", Font.BOLD, 16));
            logoLabel.setForeground(new Color(255, 105, 180));
            navPanel.add(logoLabel);
            System.err.println("Không tìm thấy ảnh /Image/logo.png. Sử dụng text thay thế.");
        }

        // Tạo các nút điều hướng với ảnh
        String[] buttons = {
            "Tổng quan", "Bán hàng", "Quản lý Sản phẩm", "Quản lý Đơn hàng", "Quản lý Hóa đơn",
            "Quản lý Khách hàng", "Quản lý Nhân viên", "Quản lý Người dùng", "Quản lý Thống kê", "Đăng xuất"
        };
        String[] iconNames = {
            "sanpham.png", "sale.png", "quanlysanpham.png", "banhang.png", "invoice.png",
            "nhanvien.png", "quanlynhanvien.png", "quanlynguoidung.jpg", "thongke.png", "logout.png"
        };

        for (int i = 0; i < buttons.length; i++) {
            JButton btn = new JButton(buttons[i]);
            btn.setBackground(new Color(255, 182, 193));
            btn.setFont(new Font("Arial", Font.BOLD, 13));
            btn.setFocusPainted(false);

            // Thêm icon cho button với kích thước lớn hơn
            URL iconUrl = getClass().getResource("/Image/" + iconNames[i]);
            if (iconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);
                Image scaledIcon = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaledIcon));
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setIconTextGap(15); // Tăng khoảng cách để phù hợp với icon lớn hơn
            } else {
                System.err.println("Không tìm thấy ảnh /Image/" + iconNames[i] + ". Nút chỉ hiển thị text.");
            }

            // Sử dụng biến cục bộ cho ActionListener
            final String buttonText = buttons[i];
            btn.addActionListener(e -> switchPanel(buttonText));
            navPanel.add(btn);
        }

        // Panel nội dung với ảnh nền
        contentPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                URL bgImageUrl = getClass().getResource("/Image/background.png");
                if (bgImageUrl != null) {
                    ImageIcon bgIcon = new ImageIcon(bgImageUrl);
                    Image bgImage = bgIcon.getImage();
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    System.err.println("Không tìm thấy ảnh /Image/background.png.");
                }
            }
        };
        contentPanel.setBackground(Color.WHITE);

        // Layout chính
        setLayout(new BorderLayout());
        add(bannerPanel, BorderLayout.NORTH);
        add(navPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void switchPanel(String name) {
        contentPanel.removeAll();
        switch (name) {
            case "Tổng quan":
                contentPanel.add(new DashboardPanel(cnn));
                break;
            case "Bán hàng":
                contentPanel.add(new SalesPanel(cnn));
                break;
            case "Quản lý Sản phẩm":
                if (cnn != null) {
                    contentPanel.add(new ProductPanel(cnn));
                } else {
                    JOptionPane.showMessageDialog(this, "Kết nối cơ sở dữ liệu không khả dụng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case "Quản lý Đơn hàng":
                contentPanel.add(new OrderPanel(cnn));
                break;
            case "Quản lý Hóa đơn":
                if (cnn != null) {
                    contentPanel.add(new InvoicePanel(cnn));
                } else {
                    JOptionPane.showMessageDialog(this, "Kết nối cơ sở dữ liệu không khả dụng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case "Quản lý Khách hàng":
                contentPanel.add(new CustomerPanel(cnn));
                break;
            case "Quản lý Nhân viên":
                contentPanel.add(new EmployeePanel(cnn));
                break;
            case "Quản lý Người dùng":
                contentPanel.add(new UserPanel(cnn));
                break;
            case "Quản lý Thống kê":
                contentPanel.add(new StatisticPanel(cnn));
                break;
            case "Đăng xuất":
                JOptionPane.showMessageDialog(this, "Bạn đã đăng xuất!");
                this.setVisible(false);
                SwingUtilities.invokeLater(() -> {
                    LoginFrame loginFrame = new LoginFrame(cnn); 
                    loginFrame.setVisible(true);
                });
                break;
            default:
                JOptionPane.showMessageDialog(this, "Chức năng '" + name + "' chưa được triển khai!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}