package app.ui;

import app.core.Session;
import app.dao.ProblemDAO;
import app.dao.SubmissionDAO;
import app.dao.UserDAO;
import app.model.Problem;
import app.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UserFrame extends JFrame {

    private static final Map<String, String[]> TEST_CASES = Map.of(
            "Sum Two Numbers", new String[]{"5 7", "12"},
            "Sort Array", new String[]{"3\n3\n1\n2", "1 2 3"},
            "Reverse Array", new String[]{"3\n1 2 3", "3 2 1"}
    );

    private final JLabel welcomeLabel = new JLabel();
    private final DefaultListModel<User> usersModel = new DefaultListModel<>();
    private final JList<User> usersList = new JList<>(usersModel);
    private final List<Problem> problems = ProblemDAO.getAllProblems();
    private final List<JRadioButton> problemRadios = new ArrayList<>();

    public UserFrame() {
        super("User Dashboard");

        User currentUser = Session.getCurrentUser();
        String username = currentUser != null ? currentUser.username() : "User";

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 540);
        setResizable(false);
        setLocationRelativeTo(null);

        onCloseReturnToLogin();

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildTopPanel(username), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        centerPanel.add(buildUsersPanel());
        centerPanel.add(buildProblemsPanel());

        root.add(centerPanel, BorderLayout.CENTER);
        root.add(buildBottomPanel(), BorderLayout.SOUTH);

        updateUserList();
    }

    private void onCloseReturnToLogin() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LoginFrame.instance.setVisible(true);
            }
        });
    }

    private JPanel buildTopPanel(String username) {
        welcomeLabel.setText("Welcome, " + username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        actionPanel.add(button("Refresh Score", this::refreshScore));
        actionPanel.add(button("Add Submission", this::onAddSubmission));

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.add(welcomeLabel, BorderLayout.NORTH);
        panel.add(actionPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildUsersPanel() {
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersList.setFixedCellHeight(24);
        usersList.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Users Scores"));
        panel.add(new JScrollPane(usersList), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildProblemsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Choose Problem"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Font radioFont = new Font("Segoe UI", Font.PLAIN, 12);

        if (problems.isEmpty()) {

            JLabel noProblems = new JLabel("No problems available");
            noProblems.setFont(radioFont);
            noProblems.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(noProblems);

        } else {

            ButtonGroup group = new ButtonGroup();

            for (Problem problem : problems) {

                JRadioButton radio = new JRadioButton(problem.title());
                radio.setFont(radioFont);
                radio.setAlignmentX(Component.LEFT_ALIGNMENT);

                group.add(radio);
                problemRadios.add(radio);

                panel.add(radio);
                panel.add(Box.createVerticalStrut(10));
            }
        }

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(button("Logout", this::onLogout), BorderLayout.WEST);
        panel.add(button("Exit", () -> System.exit(0)), BorderLayout.EAST);
        return panel;
    }

    private JButton button(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(110, 32));
        btn.addActionListener(_ -> action.run());
        return btn;
    }

    private void refreshScore() {

        User sessionUser = Session.getCurrentUser();

        if (sessionUser != null) {

            User fresh = UserDAO.getUserById(sessionUser.id());

            if (fresh != null) {
                Session.setCurrentUser(fresh);
                welcomeLabel.setText("Welcome, " + fresh.username());
            }
        }

        updateUserList();
    }

    private void updateUserList() {

        usersModel.clear();

        for (User user : UserDAO.getAllUsers()) {
            if (!"ADMIN".equals(user.role())) {
                usersModel.addElement(user);
            }
        }
    }

    private void onLogout() {
        Session.logout();
        LoginFrame.instance.setVisible(true);
        dispose();
    }

    private void onAddSubmission() {

        Problem selectedProblem = getSelectedProblem();

        if (selectedProblem == null) {
            showMessage("Submission", "Please select a problem!", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a Java Solution");
        chooser.setFileFilter(new FileNameExtensionFilter("Java Files (*.java)", "java"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File javaFile = chooser.getSelectedFile();

        String code = readFile(javaFile);

        if (code == null) {
            return;
        }

        String compileMessage = compileJavaFile(javaFile);

        if (compileMessage == null) {
            return;
        }

        if (!compileMessage.trim().isEmpty()) {
            showMessage("Compilation Error", "Compilation Error:\n" + compileMessage, JOptionPane.ERROR_MESSAGE);
            saveSubmission(selectedProblem, code, "COMPILATION_ERROR");
            return;
        }

        testJavaCode(
                selectedProblem,
                code,
                javaFile.getParent(),
                javaFile.getName().replaceFirst("\\.java$", "")
        );
    }

    private Problem getSelectedProblem() {

        for (int i = 0; i < problemRadios.size(); i++) {
            if (problemRadios.get(i).isSelected()) {
                return problems.get(i);
            }
        }

        return null;
    }

    private String readFile(File javaFile) {

        try {
            return Files.readString(javaFile.toPath());
        } catch (IOException ex) {
            showMessage("Error", "Could not read file: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private String compileJavaFile(File javaFile) {

        ProcessResult result = runProcess(List.of("javac", javaFile.getAbsolutePath()), null);

        if (result.exitCode() < 0) {
            showMessage(
                    "Compilation Error",
                    "Could not compile.\nMake sure javac is installed and added to PATH.\n\n" + result.stderr(),
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        return result.exitCode() == 0 ? result.stdout() : result.stderr();
    }

    private void testJavaCode(Problem problem, String code, String directory, String className) {

        String[] testCase = TEST_CASES.get(problem.title());

        if (testCase == null) {
            showMessage("Submission", "No test case defined for this problem.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ProcessResult result = runProcess(
                List.of("java", "-cp", directory, className),
                testCase[0] + "\n"
        );

        if (result.exitCode() < 0) {
            showMessage(
                    "Error",
                    "Could not run solution.\nMake sure java is installed and added to PATH.\n\n" + result.stderr(),
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String output = result.stdout().trim();
        String runtimeError = result.stderr().trim();

        if (!runtimeError.isEmpty()) {

            showMessage("Runtime Error", "Runtime Error:\n" + runtimeError, JOptionPane.ERROR_MESSAGE);
            saveSubmission(problem, code, "RUNTIME_ERROR");

        } else if (output.equals(testCase[1])) {

            User sessionUser = Session.getCurrentUser();

            boolean alreadySolved = SubmissionDAO.hasAccepted(sessionUser.id(), problem.id());

            saveSubmission(problem, code, "ACCEPTED");

            if (alreadySolved) {

                showMessage("Accepted", "Correct! (No points - you already solved this)", JOptionPane.INFORMATION_MESSAGE);

            } else {

                UserDAO.incrementScore(sessionUser.id(), problem.points());
                Session.setCurrentUser(UserDAO.getUserById(sessionUser.id()));

                showMessage(
                        "Accepted",
                        "Correct! You earned " + problem.points() + " point(s)!",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            updateUserList();

        } else {

            showMessage(
                    "Wrong Answer",
                    "Wrong Answer\nExpected: " + testCase[1] + "\nYour Output: " + output,
                    JOptionPane.WARNING_MESSAGE
            );
            saveSubmission(problem, code, "WRONG_ANSWER");
        }
    }

    private void saveSubmission(Problem problem, String code, String verdict) {

        User sessionUser = Session.getCurrentUser();

        if (sessionUser != null) {
            SubmissionDAO.addSubmission(sessionUser.id(), problem.id(), code, "JAVA", verdict);
        }
    }

    private ProcessResult runProcess(List<String> command, String input) {

        try {

            Process process = new ProcessBuilder(command).start();

            if (input != null) {
                process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
            }

            process.getOutputStream().close();

            ExecutorService pool = Executors.newFixedThreadPool(2);

            Future<String> stdoutFuture = pool.submit(() -> readAll(process.getInputStream()));
            Future<String> stderrFuture = pool.submit(() -> readAll(process.getErrorStream()));

            int exitCode = process.waitFor();

            pool.shutdown();

            return new ProcessResult(exitCode, stdoutFuture.get(), stderrFuture.get());

        } catch (Exception ex) {
            return new ProcessResult(-1, "", ex.getMessage());
        }
    }

    private void showMessage(String title, String message, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }

    private static String readAll(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    private record ProcessResult(int exitCode, String stdout, String stderr) {
    }
}
