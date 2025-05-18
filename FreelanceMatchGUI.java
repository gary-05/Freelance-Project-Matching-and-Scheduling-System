package mySystem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FreelanceMatchGUI extends JFrame {
	private static final long serialVersionUID = 1L;  
    private JTextArea skillsInput;
    private JTextArea resultsArea;
    private JButton matchButton;
    private JLabel statusLabel;
    private final List<Project> allProjects = new ArrayList<>();

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
        TitledBorder inputBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Your Skills");
        inputBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(inputBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        skillsInput = new JTextArea(4, 40);
        skillsInput.setFont(new Font("Arial", Font.PLAIN, 14));
        skillsInput.setLineWrap(true);
        skillsInput.setWrapStyleWord(true);
        skillsInput.setText("Enter your skills (comma-separated), e.g.: Java, Python, Web Development");
        JScrollPane skillsScrollPane = new JScrollPane(skillsInput);

        JLabel helpLabel = new JLabel("Enter your skills separated by commas");
        helpLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        matchButton = new JButton("Match Projects");
        matchButton.setFont(new Font("Arial", Font.BOLD, 12));
        buttonsPanel.add(matchButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(helpLabel, BorderLayout.NORTH);
        topPanel.add(skillsScrollPane, BorderLayout.CENTER);

        inputPanel.add(topPanel, BorderLayout.CENTER);
        inputPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return inputPanel;
    }

    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout(10, 10));
        TitledBorder resultsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Matched Projects");
        resultsBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(resultsBorder, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
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
        skillsInput.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (skillsInput.getText().contains("Enter your skills")) {
                    skillsInput.setText("");
                }
            }
        });

        matchButton.addActionListener(e -> {
            String[] userSkills = skillsInput.getText().toLowerCase().split(",");
            Set<String> skillSet = new HashSet<>();
            for (String skill : userSkills) {
                skillSet.add(skill.trim());
            }

            List<Project> filtered = new ArrayList<>();
            Set<String> unmatchedSkillsInDescription = new HashSet<>();

            for (Project p : allProjects) {
                boolean matched = false;

                // Check required skills with KMP & equalsIgnoreCase
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

                // If not matched yet, check description via KMP for user skills
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

                // Check for unmatched skills mentioned in description but not required
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
        	    resultsArea.setText("No matching projects found for the entered skill.");
        	    
        	}
                       
            else {
            	List<Project> selected = new ArrayList<>();
            	int maxEarnings = FreelanceProjectAllocator.maximizeEarnings(filtered, selected);
            	StringBuilder sb = new StringBuilder();
            	sb.append("Maximum Earnings: Rs.").append(maxEarnings).append("\n\n");
            	sb.append("Selected Projects:\n");
            	for (Project p : selected) {
            		sb.append("- ").append(p.toString()).append("\n");
            	}
            	if (!unmatchedSkillsInDescription.isEmpty()) {
            	    sb.append("\nNote: The following skills were found in descriptions but not listed as required skills:\n");
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
        allProjects.add(new Project("Project A", "Simple Java", new String[]{"Java"}, 1, 3, 50));
        allProjects.add(new Project("Project B", "Frontend Fix", new String[]{"HTML", "CSS"}, 2, 5, 60));
        allProjects.add(new Project("Project C", "API Dev", new String[]{"Java", "Spring"}, 4, 6, 70));
        allProjects.add(new Project("Project D", "Bug Fixing", new String[]{"Java"}, 6, 7, 30));
        
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
