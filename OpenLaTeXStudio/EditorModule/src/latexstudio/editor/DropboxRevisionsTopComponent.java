/* 
 * Copyright (c) 2015 Sebastian Brudzinski
 * 
 * See the file LICENSE for copying permission.
 */
package latexstudio.editor;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.sun.glass.events.KeyEvent;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import latexstudio.editor.remote.DbxEntryRevision;
import latexstudio.editor.remote.DbxState;
import latexstudio.editor.remote.DbxUtil;
import latexstudio.editor.util.ApplicationUtils;
import lib.ButtonColumn;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.IOUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component that displays Dropbox file revisions.
 */
@ConvertAsProperties(
        dtd = "-//latexstudio.editor//DropboxRevisions//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "DropboxRevisionsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "latexstudio.editor.DropboxRevisionsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DropboxRevisionsAction",
        preferredID = "DropboxRevisionsTopComponent"
)
@Messages({
    "CTL_DropboxRevisionsAction=Dropbox Revisions",
    "CTL_DropboxRevisionsTopComponent=Dropbox Revisions",
    "HINT_DropboxRevisionsTopComponent=This is a Dropbox Revisions window"
})
public final class DropboxRevisionsTopComponent extends TopComponent {

    private DefaultListModel<DbxEntryRevision> dlm = new DefaultListModel<DbxEntryRevision>();
    private static final ApplicationLogger LOGGER = new ApplicationLogger("Dropbox");

    private static final RevisionDisplayTopComponent REVTC = new TopComponentFactory<RevisionDisplayTopComponent>()
            .getTopComponent(RevisionDisplayTopComponent.class.getSimpleName());

    private static final String REVISION_COLUMN_NAME = "Revision";
    private static final String MODIFIED_COLUMN_NAME = "Modified";
    private static final String FILE_SIZE_COLUMN_NAME = "File size";
    private static final String REVIEW_COLUMN_NAME = "Review";
    private static final String REVIEW_BUTTON_LABEL = "View Revision";
    private static final int REVISION_COLUMN = 0;
    private static final int REVIEW_COLUMN = 3;

    public DropboxRevisionsTopComponent() {
        initComponents();
        setName(Bundle.CTL_DropboxRevisionsTopComponent());
        setToolTipText(Bundle.HINT_DropboxRevisionsTopComponent());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Revision", "Modified", "File size", "Review"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setRowHeight(25);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTable1MousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(DropboxRevisionsTopComponent.class, "DropboxRevisionsTopComponent.jTable1.columnModel.title0")); // NOI18N
            jTable1.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(DropboxRevisionsTopComponent.class, "DropboxRevisionsTopComponent.jTable1.columnModel.title1")); // NOI18N
            jTable1.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(DropboxRevisionsTopComponent.class, "DropboxRevisionsTopComponent.jTable1.columnModel.title2")); // NOI18N
            jTable1.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(DropboxRevisionsTopComponent.class, "DropboxRevisionsTopComponent.jTable1.columnModel.title3_1")); // NOI18N
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void loadRevision(String revisionNumber) {
        DbxEntryRevision entry = null;
        DbxClient client = DbxUtil.getDbxClient();
        for (int i = 0; i < dlm.size(); i++) {
            if (revisionNumber.equals(dlm.get(i).getRevision())) {
                entry = dlm.get(i);
                break;
            }
        }

        FileOutputStream outputStream = null;

        if (entry != null) {
            File outputFile = new File(ApplicationUtils.getAppDirectory() + File.separator + entry.getName() + entry.getRevision());

            try {
                outputStream = new FileOutputStream(outputFile);
                client.getFile(entry.getPath(), entry.getRevision(), outputStream);
                LOGGER.log("Loaded revision " + entry.getRevision() + " from Dropbox");
            } catch (Throwable e) {
                Exceptions.printStackTrace(e);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }

            REVTC.open();
            REVTC.requestActive();
            REVTC.setName(entry.getName() + " (rev: " + entry.getRevision() + ")");
            REVTC.setDisplayedRevision(new DbxState(entry.getPath(), entry.getRevision()));
            try {
                REVTC.setText(FileUtils.readFileToString(outputFile));
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            updateRevisionsList(entry.getPath());
        }
    }

    private void jTable1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MousePressed
        if (evt.getClickCount() == 2) {
            // Resolving which row has been double-clicked
            Point point = evt.getPoint();
            JTable table = (JTable) evt.getSource();
            int row = table.rowAtPoint(point);
            // Finding revision using information from the clicked row
            String revisionNumber = table.getValueAt(row, REVISION_COLUMN).toString();
            loadRevision(revisionNumber);
        }
    }//GEN-LAST:event_jTable1MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    public void updateRevisionsList(String path) {
        DbxClient client = DbxUtil.getDbxClient();
        List<DbxEntry.File> entries = null;

        try {
            entries = client.getRevisions(path);
        } catch (DbxException ex) {
            Exceptions.printStackTrace(ex);
        }

        dlm.clear();
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == REVIEW_COLUMN;
            }
        };
        model.addColumn(REVISION_COLUMN_NAME);
        model.addColumn(MODIFIED_COLUMN_NAME);
        model.addColumn(FILE_SIZE_COLUMN_NAME);
        model.addColumn(REVIEW_COLUMN_NAME);

        for (DbxEntry.File dbxEntry : entries) {
            dlm.addElement(new DbxEntryRevision(dbxEntry));
            model.addRow(new Object[]{dbxEntry.rev, dbxEntry.lastModified, dbxEntry.humanSize, REVIEW_BUTTON_LABEL});
        }

        Action showVersion = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Resolving which row has been double-clicked
                JTable table = (JTable) e.getSource();
                int row = Integer.valueOf(e.getActionCommand());
                // Finding revision using information from the clicked row
                String revisionNumber = table.getValueAt(row, REVISION_COLUMN).toString();
                loadRevision(revisionNumber);
            }
        };

        jTable1.setModel(model);
        ButtonColumn buttonColumn = new ButtonColumn(jTable1, showVersion, REVIEW_COLUMN);
        buttonColumn.setMnemonic(KeyEvent.VK_D);
    }

    public DefaultListModel<DbxEntryRevision> getDlm() {
        return dlm;
    }

    public void setDlm(DefaultListModel<DbxEntryRevision> dlm) {
        this.dlm = dlm;
    }

}
