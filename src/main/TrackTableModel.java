package main;

import javax.swing.table.AbstractTableModel;

public class TrackTableModel extends AbstractTableModel {

	private String[] columnNames = { "Name", "Duration", "Download" };
	private Object[][] data = { { "Track Name", 0, false } };
	
	public void setTableData(Object[][] data) {
		this.data = data;
	}
	
	public int getColumnCount() {
	    return columnNames.length;
	}
	
	public int getRowCount() {
	    return data.length;
	}
	
	public String getColumnName(int col) {
	    return columnNames[col];
	}
	
	public Object getValueAt(int row, int col) {
	    return data[row][col];
	}
	
	public Class getColumnClass(int c) {
	    return getValueAt(0, c).getClass();
	}
	
	/*
	 * Don't need to implement this method unless your table's
	 * editable.
	 */
	public boolean isCellEditable(int row, int col) {
	    //Note that the data/cell address is constant,
	    //no matter where the cell appears onscreen.
	    if (col < 2) {
	        return false;
	    } else {
	        return true;
	    }
	}

	public void addRow(Object[] row) {
		Object[][] newData = new Object[data.length + 1][];
		for (int i = 0; i < data.length; i++)
			newData[i] = data[i];
		newData[data.length] = row;
		data = newData;
		super.fireTableDataChanged();
	}

}
