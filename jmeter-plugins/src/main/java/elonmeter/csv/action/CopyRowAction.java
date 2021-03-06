package elonmeter.csv.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.apache.jmeter.gui.util.PowerTableModel;

public class CopyRowAction
        implements ActionListener {

    private JTable grid;
    private PowerTableModel tableModel;
    private JButton copyRowButton;
    private JComponent sender;

    public CopyRowAction(JComponent aSender, JTable grid, PowerTableModel tableModel, JButton copyRowButton) {
        this.grid = grid;
        this.tableModel = tableModel;
        this.copyRowButton = copyRowButton;
        this.sender = aSender;
    }

    public void actionPerformed(ActionEvent e) {
        if (grid.isEditing()) {
            TableCellEditor cellEditor = grid.getCellEditor(grid.getEditingRow(), grid.getEditingColumn());
            cellEditor.stopCellEditing();
        }
        final int selectedRow = grid.getSelectedRow();

        if (tableModel.getRowCount() == 0 || selectedRow < 0) {
        	copyRowButton.setEnabled(false);
            return;
        }
        tableModel.addRow(tableModel.getRowData(selectedRow));
        tableModel.fireTableDataChanged();

        // Enable DELETE (which may already be enabled, but it won't hurt)
        copyRowButton.setEnabled(true);

        // Highlight (select) the appropriate row.
        int rowToSelect = selectedRow + 1;
        grid.setRowSelectionInterval(rowToSelect, rowToSelect);
        sender.updateUI();
    }
}
