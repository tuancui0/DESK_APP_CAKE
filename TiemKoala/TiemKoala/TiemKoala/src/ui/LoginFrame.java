package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private Connection cnn; // Thêm biến để lưu kết nối

    public LoginFrame() {
        this(null); // Gọi constructor mặc định với Connection null
    }

    public LoginFrame(Connection conn) {
        this.cnn = conn; // Nhận kết nối từ MainFrame khi đăng xuất
        setTitle("Đăng nhập - Bakery Store Manager");
        setSize(600, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Tạo panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // Panel bên trái chứa ảnh
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(220, 300));
        leftPanel.setBackground(new Color(255, 240, 245));  // màu hồng nhẹ
        mainPanel.add(leftPanel, BorderLayout.WEST);

        try {
            // Load ảnh từ thư mục resource /images/login.png
            java.net.URL imgUrl = getClass().getResource("/Image/login1.png");
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image img = icon.getImage().getScaledInstance(220, 300, Image.SCALE_SMOOTH);
                JLabel lblImage = new JLabel(new ImageIcon(img));
                leftPanel.add(lblImage);
            } else {
                System.err.println("Không tìm thấy file ảnh /Image/login1.png");
                // Thay thế bằng JLabel thông báo hoặc placeholder ảnh
                leftPanel.add(new JLabel("Ảnh không tồn tại"));
            }
        } catch (Exception e) {
            e.printStackTrace();  // In lỗi chi tiết để dễ debug
            leftPanel.add(new JLabel("Lỗi khi load ảnh"));
        }

        // Panel bên phải chứa form login
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(255, 255, 255));
        rightPanel.setLayout(null);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Tiêu đề
        JLabel lblTitle = new JLabel("Bakery Store Manager");
        lblTitle.setBounds(70, 20, 250, 30);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 20));
        lblTitle.setForeground(new Color(139, 69, 19));  // màu nâu bánh mì
        rightPanel.add(lblTitle);

        // Label và textfield Username
        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        lblUsername.setBounds(40, 80, 100, 25);
        lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 14));
        rightPanel.add(lblUsername);

        txtUsername = new JTextField();
        txtUsername.setBounds(150, 80, 160, 25);
        rightPanel.add(txtUsername);

        // Label và textfield Password
        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setBounds(40, 120, 100, 25);
        lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 14));
        rightPanel.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(150, 120, 160, 25);
        rightPanel.add(txtPassword);

        // Nút đăng nhập
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setBounds(110, 180, 120, 30);
        btnLogin.setBackground(new Color(255, 182, 193)); // màu hồng nhẹ
        btnLogin.setForeground(Color.DARK_GRAY);
        btnLogin.setFocusPainted(false);
        rightPanel.add(btnLogin);

        // Thêm sự kiện đăng nhập
        btnLogin.addActionListener(e -> login());

        // Nhấn Enter ở mật khẩu cũng đăng nhập
        txtPassword.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (checkLogin(username, password)) {
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
            MainFrame mainFrame = new MainFrame(cnn); // Truyền kết nối vào MainFrame
            mainFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean checkLogin(String username, String password) {
        Connection conn = (cnn != null) ? cnn : null; // Sử dụng cnn nếu có, nếu không thì tạo mới
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            if (conn == null) {
                conn = utils.DBConnection.getConnection(); // Tạo kết nối mới nếu chưa có
            }
            String sql = "SELECT MatKhau FROM NguoiDung WHERE TenDangNhap = ?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            rs = pst.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("MatKhau");
                return password.equals(storedPassword);
            }
            return false;

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pst != null) pst.close(); } catch (Exception e) {}
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}