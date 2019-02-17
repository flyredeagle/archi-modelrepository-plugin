/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.archicontribs.modelrepository.actions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.archicontribs.modelrepository.IModelRepositoryImages;
import org.archicontribs.modelrepository.ModelRepositoryPlugin;
import org.archicontribs.modelrepository.authentication.ProxyAuthenticater;
import org.archicontribs.modelrepository.authentication.SimpleCredentialsStorage;
import org.archicontribs.modelrepository.dialogs.ImportLocalModelInputDialog;
import org.archicontribs.modelrepository.grafico.ArchiRepository;
import org.archicontribs.modelrepository.grafico.GraficoModelLoader;
import org.archicontribs.modelrepository.grafico.GraficoUtils;
import org.archicontribs.modelrepository.grafico.IGraficoConstants;
import org.archicontribs.modelrepository.grafico.IRepositoryListener;
import org.archicontribs.modelrepository.preferences.IPreferenceConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.archimatetool.editor.model.IEditorModelManager;
import com.archimatetool.editor.utils.StringUtils;
import com.archimatetool.model.IArchimateModel;

/**
 * Clone a model
 * 
 * 1. Get Location
 * 2. Check Valid Gitlab repo
 * 3. If Grafico files exist load the model from the Grafico files and save it as temp file
 * 4. If Grafico files do not exist create a new temp model and save it
 *
 */
public class ImportLocalModelAction extends AbstractModelAction {
	
    public ImportLocalModelAction(IWorkbenchWindow window) {
        super(window);
        setImageDescriptor(IModelRepositoryImages.ImageFactory.getImageDescriptor(IModelRepositoryImages.ICON_CLONE));
        setText(Messages.CloneModelAction_0);
        setToolTipText(Messages.CloneModelAction_0);
    }

    @Override
    public void run() {
    	
        ImportLocalModelInputDialog dialog = new ImportLocalModelInputDialog(fWindow.getShell());
        if(dialog.open() != Window.OK) {
            return;
        }
    	
        final String repoFilePath = dialog.getPath();
        if(!StringUtils.isSet(repoFilePath)){
        		return;
        }
        File localRepoFolder = new File(repoFilePath);
        if(!GraficoUtils.isGitRepository(localRepoFolder)) {
			MessageDialog.openError(fWindow.getShell(),
	                  Messages.ImportLocalModelAction_0,
	                  Messages.ImportLocalModelAction_1 + " " + localRepoFolder.getAbsolutePath() + 
						Messages.ImportLocalModelAction_2); 

            return;
        }else {
        	// Check if it is one of the already open models
        	List<IArchimateModel> models= IEditorModelManager.INSTANCE.getModels();
        	for (IArchimateModel model : models) {
        		if(model.getFile().getParentFile().getParentFile().getAbsolutePath().equals(localRepoFolder.getAbsolutePath())) {
        			MessageDialog.openError(fWindow.getShell(),
        	                  Messages.ImportLocalModelAction_0,
        	                  Messages.ImportLocalModelAction_1 + " " + localRepoFolder.getAbsolutePath() + 
        						" "+ Messages.ImportLocalModelAction_3 + " " + model.getName()); 
        			return;
        		} 
        	}
        }
        setRepository(new ArchiRepository(localRepoFolder));

        
        try {
            // Load it from the Grafico files if we can
            IArchimateModel graficoModel = new GraficoModelLoader(getRepository()).loadModel();
            
            // We couldn't load it from Grafico so create a new blank model
            if(graficoModel == null) {
                // New one. This will open in the tree
                IArchimateModel model = IEditorModelManager.INSTANCE.createNewModel();
                model.setFile(getRepository().getTempModelFile());
                
                // And Save it
                IEditorModelManager.INSTANCE.saveModel(model);
                
                // Export to Grafico
                getRepository().exportModelToGraficoFiles();
                
                // And do a first commit
                getRepository().commitChanges(Messages.CloneModelAction_3, false);
                
                // Save the checksum
                getRepository().saveChecksum();
          	}
            
          	// Notify listeners
          	notifyChangeListeners(IRepositoryListener.REPOSITORY_ADDED);
        }
        catch(Exception ex) {
            displayErrorDialog(Messages.CloneModelAction_0, ex);
        }
    }
    
    @Override
    protected boolean shouldBeEnabled() {
        return true;
    }
}
