package main;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JButton;

import com.google.gson.JsonSyntaxException;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Color;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.JSeparator;

import java.awt.SystemColor;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.awt.Toolkit;
import javax.swing.UIManager;


public class SoundCloneGUI {
	
	private final int TOGGLE_INDEX = 2;
	private final String[] columnNames = { "Name", "Duration", "Download" };

	private JFrame frmSoundclone;
	private JTextField downloadPath;
	private JComboBox comboBox;
	private JTextPane status;
	private JButton start;
	private static boolean locked;
	
	private static DownloadLikes downloader;
	private JTable table;
	private JSeparator separator;
	private JButton btnBrowse;
	private JFileChooser fileChoose;
	
	@SuppressWarnings("unused")
	public enum StatusType { STANDARD, WARNING, COMPLETE, PROCESS };
	private Map<StatusType, SimpleAttributeSet> statusTypes;
	private JButton btnNewButton;
	private JButton button;
	private JLabel lblNewLabel_1;
	private JButton btnNewButton_1;

	/**
	 * Launch the application.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		downloader = new DownloadLikes();
		locked = false;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SoundCloneGUI window = new SoundCloneGUI();
					window.frmSoundclone.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SoundCloneGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		// Setup text attributes for status lines
		SimpleAttributeSet standard = new SimpleAttributeSet();
		StyleConstants.setForeground(standard, Color.black);
		SimpleAttributeSet warning = new SimpleAttributeSet();
		StyleConstants.setForeground(warning, Color.red);
		SimpleAttributeSet complete = new SimpleAttributeSet();
		StyleConstants.setForeground(complete, new Color(0, 104, 20));
		SimpleAttributeSet process = new SimpleAttributeSet();
		StyleConstants.setForeground(process, Color.black);
		StyleConstants.setItalic(process, true);
		
		statusTypes = new HashMap<StatusType, SimpleAttributeSet>();
		statusTypes.put(StatusType.STANDARD, standard);
		statusTypes.put(StatusType.COMPLETE, complete);
		statusTypes.put(StatusType.WARNING, warning);
		statusTypes.put(StatusType.PROCESS, process);
		
		frmSoundclone = new JFrame();
		frmSoundclone.setIconImage(Toolkit.getDefaultToolkit().getImage(SoundCloneGUI.class.getResource("/com/sun/java/swing/plaf/windows/icons/FloppyDrive.gif")));
		frmSoundclone.setTitle("SoundClone");
		frmSoundclone.setResizable(false);
		frmSoundclone.setBounds(100, 100, 591, 421);
		frmSoundclone.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSoundclone.getContentPane().setLayout(null);
		
		status = new JTextPane();
		status.setBounds(22, 85, 347, 67);
		status.setBackground(new Color(211, 211, 211));
		status.setFont(new Font("Tahoma", Font.PLAIN, 12));
		status.setEditable(false);
		frmSoundclone.getContentPane().add(status);
		
		downloadPath = new JTextField(downloader.getDownloadPath());
		downloadPath.setBounds(212, 44, 259, 20);
		downloadPath.setHorizontalAlignment(SwingConstants.CENTER);
		frmSoundclone.getContentPane().add(downloadPath);
		downloadPath.setColumns(10);
		
		//JScrollPane scrollPane = new JScrollPane(table);
		
		start = new JButton("Download");
		start.setBounds(381, 85, 183, 67);
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (locked) {
						if (start.getText().equals("Download"))
							updateStatus("Cannot start download", StatusType.WARNING);
						downloader.stopThread();
					}else{
						startDownloading();
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		frmSoundclone.getContentPane().add(start);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(22, 171, 543, 164);
		frmSoundclone.getContentPane().add(scrollPane);
		
		table = new JTable(null, columnNames);
		scrollPane.setViewportView(table);
		table.setRowSelectionAllowed(false);
		table.setModel(new DefaultTableModel(null, columnNames) {
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
		        return columnIndex == TOGGLE_INDEX;
		    }
			
			@Override
		    public Class<?> getColumnClass(int columnIndex) {
		        if (columnIndex == TOGGLE_INDEX)
		        	return Boolean.class;
		        return String.class;
		    }
		});
		table.getTableHeader().setResizingAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);
		
		comboBox = new JComboBox(downloader.getConfigNames());
		comboBox.setBounds(22, 44, 169, 20);
		comboBox.setEditable(true);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					comboBoxChanged();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		frmSoundclone.getContentPane().add(comboBox);
		
		// Try to initially update downloader with initial config
		try {
			comboBoxChanged();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		JLabel lblNewLabel = new JLabel("Username");
		lblNewLabel.setBounds(70, 19, 74, 14);
		frmSoundclone.getContentPane().add(lblNewLabel);
		
		JLabel lblDownload = new JLabel("Destination");
		lblDownload.setBounds(212, 19, 259, 14);
		lblDownload.setHorizontalAlignment(SwingConstants.CENTER);
		frmSoundclone.getContentPane().add(lblDownload);
		
		separator = new JSeparator();
		separator.setBounds(201, 11, 46, 60);
		separator.setOrientation(SwingConstants.VERTICAL);
		frmSoundclone.getContentPane().add(separator);
		
		fileChoose = new JFileChooser(downloadPath.getText());
		fileChoose.setAcceptAllFileFilterUsed(false);
		fileChoose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		btnBrowse = new JButton("Browse");
		btnBrowse.setBounds(481, 43, 83, 23);
		btnBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				browseDownloadPath(e);
			}
			
		});
		frmSoundclone.getContentPane().add(btnBrowse);
		
		btnNewButton = new JButton("Select All");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				for (int i = 0; i < model.getRowCount(); i++)
					model.setValueAt(true, i, TOGGLE_INDEX);
			}
		});
		btnNewButton.setBounds(345, 357, 96, 23);
		frmSoundclone.getContentPane().add(btnNewButton);
		
		button = new JButton("Deselect All");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				for (int i = 0; i < model.getRowCount(); i++)
					model.setValueAt(false, i, TOGGLE_INDEX);
			}
		});
		button.setBounds(451, 357, 113, 23);
		frmSoundclone.getContentPane().add(button);
		
		lblNewLabel_1 = new JLabel("Narbulus - 2014");
		lblNewLabel_1.setForeground(SystemColor.textInactiveText);
		lblNewLabel_1.setBounds(32, 361, 130, 14);
		frmSoundclone.getContentPane().add(lblNewLabel_1);
		
		btnNewButton_1 = new JButton("Open Download Location");
		btnNewButton_1.setBounds(137, 357, 198, 23);
		btnNewButton_1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					File pathRoot = new File(downloadPath.getText());
					File pathMain = new File(downloadPath.getText() + "/" + ((String)comboBox.getSelectedItem()));
					
					if (pathMain.exists()) {
						Desktop.getDesktop().open(pathMain);
					}else if (pathRoot.exists()) {
						Desktop.getDesktop().open(pathRoot);
					}else{
						updateStatus("Download path doesn't exists", StatusType.WARNING);
					}
				} catch (IOException | BadLocationException e1) {
					try {
						updateStatus("Download path doesn't exist");
					} catch (BadLocationException e2) {
						e2.printStackTrace();
					}
				}
			}
			
		});
		frmSoundclone.getContentPane().add(btnNewButton_1);
		table.getModel().addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				onTableChanged(e);
				table.getColumn("Duration").setMaxWidth((int) (table.getWidth() * 0.2));
				table.getColumn("Download").setMaxWidth((int) (table.getWidth() * 0.2));
			}
			
		});
		
	}
	
	private void browseDownloadPath(ActionEvent e) {
		//Handle open button action.
		int returnVal = fileChoose.showOpenDialog(frmSoundclone.getContentPane());
 
		if (returnVal == JFileChooser.APPROVE_OPTION) {
        	File file = fileChoose.getSelectedFile();
        	downloadPath.setText(file.getAbsolutePath());
        }
	}
	
	private void onTableChanged(TableModelEvent e) {
		if (e.getColumn() == TOGGLE_INDEX) {
			downloader.toggleDownload(e.getLastRow());
		}
	}
	
	private void comboBoxChanged() throws JsonSyntaxException, Exception {
		if (downloader != null && !downloader.isThreadRunning() && comboBox.getSelectedItem() != null) {
			String select = (String) comboBox.getSelectedItem();
			String curUser = downloader.getCurrentUser();
			if ( curUser == null || (curUser != null && !downloader.getCurrentUser().equals(select)) ) {
				lockControls();
				downloader.updateUser(select.replace(".", "-"), this);
				downloadPath.setText(downloader.getDownloadPath());
			}
		}
	}
	
	private void startDownloading() throws JsonSyntaxException, Exception {
		if (downloader != null && !downloader.isThreadRunning() && !locked) {
			if (comboBox.getSelectedItem() != null && downloadPath.getText() != null) {
				if (downloader.isNewPath(downloadPath.getText()) && start.getText().equals("Download")) {
					updateStatus("New path differs from last download path used with this username. Re-download files to new directory?"
							, StatusType.WARNING);
					start.setText("Continue");
				}else{
					lockControls();
					start.setText("Cancel");
					downloader.downloadTracks((String) comboBox.getSelectedItem(), downloadPath.getText(), this);
				}
			}
		}else{
			updateStatus("Cannot start download", StatusType.WARNING);
		}
	}
	
	public void updateStatus(String message) throws BadLocationException {
		updateStatus(message, StatusType.STANDARD); 
	}
	
	public void updateStatus(String message, StatusType type) throws BadLocationException {
		status.getDocument().insertString(0, message + "\n", statusTypes.get(type)); 
	}
	
	@SuppressWarnings("unchecked")
	public void addConfig(String config) {
		comboBox.insertItemAt(config, comboBox.getItemCount());
	}
	
	public void addTableRow(Object[] row) {
		((DefaultTableModel) table.getModel()).addRow(row);
	}
	
	public void resetTable() {
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		model.setDataVector(null, columnNames);
	}
	
	public void lockControls() {
		comboBox.setEditable(false);
		comboBox.setEnabled(false);
		locked = true;
	}
	
	public void unlockControls() {
		comboBox.setEditable(true);
		comboBox.setEnabled(true);
		locked = false;
		start.setText("Download");
	}
}
