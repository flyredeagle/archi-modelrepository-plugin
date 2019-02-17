/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.archicontribs.modelrepository.actions;

import org.archicontribs.modelrepository.grafico.GraficoUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.archimatetool.editor.actions.AbstractModelSelectionHandler;


/**
 * Show In Tags View Handler
 * 
 * @author Diego Bragato
 */
public class ShowInTagsViewHandler extends AbstractModelSelectionHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ShowInTagsViewAction action = new ShowInTagsViewAction(HandlerUtil.getActiveWorkbenchWindowChecked(event));
        action.run();
        return null;
    }

    @Override
    public void updateState() {
        // Do nothing
    }

    @Override
    public boolean isEnabled() {
        return GraficoUtils.isModelInLocalRepository(getActiveArchimateModel());
    }
}
