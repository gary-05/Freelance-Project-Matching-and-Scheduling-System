package mySystem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.*;

public class FreelanceMatchGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextArea resultsArea;
    private JButton matchButton;
    private JLabel statusLabel;
    private final List<Project> allProjects = new ArrayList<>();
    private JComboBox<String>[] skillDropdowns;
    private static final String[] predefinedSkills = {
        "Java", "Python", "Web Development", "C++", "Machine Learning", "Android", "React", "Data Science",
        "Frontend","Backend","Full Stack Development","HTML","UI/UX Design","JavaScript","Spring Boot","SQL"
    };

    public FreelanceMatchGUI() {
        setupUI();
        initializeComponents();
        populateSampleProjects();
    }

    private void setupUI() {
        setTitle("Freelance Project Matching and Scheduling");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(createInputPanel(), BorderLayout.NORTH);
        mainPanel.add(createResultsPanel(), BorderLayout.CENTER);
        mainPanel.add(createStatusPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }
    
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        TitledBorder inputBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select Your Skills");
        inputBorder.setTitleFont(new Font("Arial", Font.BOLD, 18));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(inputBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel dropdownPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        skillDropdowns = new JComboBox[3];
        
        for (int i = 0; i < skillDropdowns.length; i++) {
            JLabel label = new JLabel("User Skill " + (i + 1) + ":");
            label.setFont(new Font("Arial", Font.PLAIN, 17));
            dropdownPanel.add(label);
            
            String[] skillsWithNone = new String[predefinedSkills.length + 1];
            skillsWithNone[0] = "None";
            System.arraycopy(predefinedSkills, 0, skillsWithNone, 1, predefinedSkills.length);

            skillDropdowns[i] = new JComboBox<>(skillsWithNone);
            skillDropdowns[i].setSelectedIndex(0);
            dropdownPanel.add(skillDropdowns[i]);
        }
        
        

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        matchButton = new JButton("Match Projects");
        matchButton.setFont(new Font("Arial", Font.BOLD, 15));
        buttonsPanel.add(matchButton);

        inputPanel.add(dropdownPanel, BorderLayout.CENTER);
        inputPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return inputPanel;
    }


    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout(10, 10));
        TitledBorder resultsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Matched Projects");
        resultsBorder.setTitleFont(new Font("Arial", Font.BOLD, 18));
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(resultsBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        resultsArea.setText("Project matches will appear here...");
        JScrollPane resultsScrollPane = new JScrollPane(resultsArea);
        resultsPanel.add(resultsScrollPane, BorderLayout.CENTER);

        return resultsPanel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        return statusPanel;
    }

    private void initializeComponents() {
        matchButton.addActionListener(e -> {
            Set<String> skillSet = new HashSet<>();
            for (JComboBox<String> comboBox : skillDropdowns) {
                String selected = (String) comboBox.getSelectedItem();
                if (selected != null && !selected.equals("None") && !selected.trim().isEmpty()) {
                    skillSet.add(selected.trim().toLowerCase());
                }
            }


            List<Project> filtered = new ArrayList<>();
            Set<String> unmatchedSkillsInDescription = new HashSet<>();

            for (Project p : allProjects) {
                boolean matched = false;

                for (String required : p.requiredSkills) {
                    for (String skill : skillSet) {
                        String skillLower = skill.toLowerCase();
                        String requiredLower = required.toLowerCase();
                        if (requiredLower.equals(skillLower) ||
                            FreelanceProjectAllocator.KMPMatcher.kmpMatch(requiredLower, skillLower) ||
                            FreelanceProjectAllocator.KMPMatcher.kmpMatch(skillLower, requiredLower)) {
                            matched = true;
                            break;
                        }
                    }
                    if (matched) break;
                }

                if (!matched) {
                    for (String userSkill : skillSet) {
                        if (FreelanceProjectAllocator.KMPMatcher.kmpMatch(p.description, userSkill)) {
                            matched = true;
                            break;
                        }
                    }
                }

                if (matched) {
                    filtered.add(p);
                }

                for (String userSkill : skillSet) {
                    if (FreelanceProjectAllocator.KMPMatcher.kmpMatch(p.description, userSkill)) {
                        boolean inRequired = false;
                        for (String req : p.requiredSkills) {
                            if (req.equalsIgnoreCase(userSkill)) {
                                inRequired = true;
                                break;
                            }
                        }
                        if (!inRequired) {
                            unmatchedSkillsInDescription.add(userSkill);
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                resultsArea.setText("No matching projects found for the selected skills.");
            } else {
                List<Project> selected = new ArrayList<>();
                int maxEarnings = FreelanceProjectAllocator.maximizeEarnings(filtered, selected);
                StringBuilder sb = new StringBuilder();
                sb.append("Maximum Earnings: Rs.").append(maxEarnings).append("\n\n");
                sb.append("Selected Projects:\n");
                for (Project p : selected) {
                    sb.append("- ").append(p.toString()).append("\n");
                }
                if (!unmatchedSkillsInDescription.isEmpty()) {
                    sb.append("\nNote: These skills were found in project descriptions but not marked as required:\n");
                    for (String skill : unmatchedSkillsInDescription) {
                        sb.append("- ").append(skill).append("\n");
                    }
                }
                resultsArea.setText(sb.toString());
                statusLabel.setText("Matched " + selected.size() + " project(s).");
            }
        });
    }

    private void populateSampleProjects() {
        allProjects.clear();
        String csvFile = "project_dataset.csv";
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = parseCSVLine(line);
                if (parts.length < 7) continue;
                String id = parts[0].trim();
                String name = parts[1].trim();
                String description = parts[2].trim();
                String[] skills = parts[3].replace("\"", "").split(";");
                int start = parseDateToInt(parts[4].trim());
                int end = parseDateToInt(parts[5].trim());
                int earnings = Integer.parseInt(parts[6].trim());
                allProjects.add(new Project(id, name, description, skills, start, end, earnings));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    private int parseDateToInt(String dateStr) {
        try {
            return Integer.parseInt(dateStr.replace("-", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new FreelanceMatchGUI().setVisible(true);
        });
    }
}
