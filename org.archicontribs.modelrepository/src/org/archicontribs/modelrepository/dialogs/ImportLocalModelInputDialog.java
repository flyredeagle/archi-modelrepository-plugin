/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.archicontribs.modelrepository.dialogs;

import org.archicontribs.modelrepository.ModelRepositoryPlugin;
import org.archicontribs.modelrepository.preferences.IPreferenceConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.archimatetool.editor.ui.IArchiImages;

/**
 * Import Local Model Input Dialog
 * 
 * @author Diego Bragato
 */
public class ImportLocalModelInputDialog extends TitleAreaDialog {

	private Text txtPath;
    
    private Button browseButton;

    /**
     * File system path
     */
    private String path;
    
    public ImportLocalModelInputDialog(Shell parentShell) {
        super(parentShell);
        setTitle(Messages.ImportLocalModelInputDialog_0);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.ImportLocalModelInputDialog_0);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage(Messages.ImportLocalModelInputDialog_1, IMessageProvider.INFORMATION);
        setTitleImage(IArchiImages.ImageFactory.getImage(IArchiImages.ECLIPSE_IMAGE_NEW_WIZARD));

        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        txtPath = createTextField(container, Messages.ImportLocalModelInputDialog_2, SWT.NONE);
        txtPath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
                saveInput();				
			}
		});
        createBrowseButton(container);
        
        return area;
    }
    
    private Text createTextField(Composite container, String message, int style) {
        Label label = new Label(container, SWT.NONE);
        label.setText(message);
        
        Text txt = new Text(container, SWT.BORDER | style);
        //txt.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, true));
        txt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        return txt;
    }

    private void createBrowseButton(Composite container) {
        browseButton = new Button(container, SWT.PUSH);
        browseButton.setText(Messages.ImportLocalModelInputDialog_3);
        //browseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,1,0));
        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(container.getShell(), SWT.NULL);
                String path = dialog.open();
                if (path != null) {
                	txtPath.setText(path);
                	saveInput();
                }
            }
        });
        //browseButton.setLayoutData(new GridData(SWT.TOP, SWT.RIGHT, true, true));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        browseButton.setLayoutData(gd);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    // save content of the Text fields because they get disposed
    // as soon as the Dialog closes
    private void saveInput() {
        path = txtPath.getText().trim();

        // maybe remember initial directory ?
        //ModelRepositoryPlugin.INSTANCE.getPreferenceStore().setValue(IPreferenceConstants.PREFS_STORE_REPO_CREDENTIALS, browseButton.getSelection());
    }

    @Override
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }

    public String getPath() {
        return path;
    }
}