package org.archicontribs.modelrepository.views.tags;

import org.archicontribs.modelrepository.ModelRepositoryPlugin;
import org.archicontribs.modelrepository.actions.AddBranchAction;
import org.archicontribs.modelrepository.actions.DeleteBranchAction;
import org.archicontribs.modelrepository.actions.MergeBranchAction;
import org.archicontribs.modelrepository.actions.SwitchBranchAction;
import org.archicontribs.modelrepository.grafico.ArchiRepository;
import org.archicontribs.modelrepository.grafico.BranchInfo;
import org.archicontribs.modelrepository.grafico.GraficoUtils;
import org.archicontribs.modelrepository.grafico.IArchiRepository;
import org.archicontribs.modelrepository.grafico.IRepositoryListener;
import org.archicontribs.modelrepository.grafico.RepositoryListenerManager;
import org.archicontribs.modelrepository.views.tags.TagsTableViewer;
import org.archicontribs.modelrepository.views.tags.Messages;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.archimatetool.editor.ui.components.UpdatingTableColumnLayout;
import com.archimatetool.model.IArchimateModel;

public class TagsView 
extends ViewPart
implements IContextProvider, ISelectionListener, IRepositoryListener {
	public static String ID = ModelRepositoryPlugin.PLUGIN_ID + ".tagsView"; //$NON-NLS-1$
    public static String HELP_ID = ModelRepositoryPlugin.PLUGIN_ID + ".tagsViewHelp"; //$NON-NLS-1$
    
    private IArchiRepository fSelectedRepository;
    
    private Label fRepoLabel;
    private TagsTableViewer fTagsTableViewer;
    
    private AddBranchAction fActionAddBranch;
    private SwitchBranchAction fActionSwitchBranch;
    private MergeBranchAction fActionMergeBranch;
    private DeleteBranchAction fActionDeleteBranch;
    
    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout());
        
        // Create Info Section
        createInfoSection(parent);

        // Create Table Section
        createTableSection(parent);
        
        makeActions();
        hookContextMenu();
        makeLocalToolBarActions();
        
        // Register us as a selection provider so that Actions can pick us up
        getSite().setSelectionProvider(getTagsViewer());
        
        // Listen to workbench selections
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);

        // Register Help Context
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getTagsViewer().getControl(), HELP_ID);
        
        // Initialise with whatever is selected in the workbench
        IWorkbenchPart part = getSite().getWorkbenchWindow().getPartService().getActivePart();
        if(part != null) {
            selectionChanged(part, getSite().getWorkbenchWindow().getSelectionService().getSelection());
        }
        
        // Add listener
        RepositoryListenerManager.INSTANCE.addListener(this);
    }
    
    private void createInfoSection(Composite parent) {
        Composite mainComp = new Composite(parent, SWT.NONE);
        mainComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        mainComp.setLayout(layout);

        // Repository name
        fRepoLabel = new Label(mainComp, SWT.NONE);
        fRepoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fRepoLabel.setText(Messages.TagsView_0);
    }

    private void createTableSection(Composite parent) {
        Composite tableComp = new Composite(parent, SWT.NONE);
        tableComp.setLayout(new UpdatingTableColumnLayout(tableComp));
        
        // This ensures a minumum and equal size and no horizontal size creep for the table
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 100;
        gd.heightHint = 50;
        tableComp.setLayoutData(gd);
        
        // Branches Table
        fTagsTableViewer = new TagsTableViewer(tableComp);
        
        /*
         * Listen to Table Selections to update local Actions
         */
        fTagsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateActions();
            }
        });
        
        fTagsTableViewer.addDoubleClickListener((event) -> {
//            if(fActionSwitchBranch.isEnabled()) {
//                fActionSwitchBranch.run();
//            }
        });
    }
    
    /**
     * Make local actions
     */
    private void makeActions() {
        fActionAddBranch = new AddBranchAction(getViewSite().getWorkbenchWindow());
        fActionAddBranch.setEnabled(false);
        
        fActionSwitchBranch = new SwitchBranchAction(getViewSite().getWorkbenchWindow());
        fActionSwitchBranch.setEnabled(false);
        
        fActionMergeBranch = new MergeBranchAction(getViewSite().getWorkbenchWindow());
        fActionMergeBranch.setEnabled(false);
        
        fActionDeleteBranch = new DeleteBranchAction(getViewSite().getWorkbenchWindow());
        fActionDeleteBranch.setEnabled(false);
    }

    /**
     * Hook into a right-click menu
     */
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#BranchesPopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        
        Menu menu = menuMgr.createContextMenu(getTagsViewer().getControl());
        getTagsViewer().getControl().setMenu(menu);
        
        getSite().registerContextMenu(menuMgr, getTagsViewer());
    }
    
    /**
     * Make Local Toolbar items
     */
    protected void makeLocalToolBarActions() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();

        manager.add(fActionAddBranch);
        manager.add(fActionSwitchBranch);
        manager.add(fActionMergeBranch);
        manager.add(new Separator());
        manager.add(fActionDeleteBranch);
    }
    
    /**
     * Update the Local Actions depending on the local selection 
     * @param selection
     */
    private void updateActions() {
        BranchInfo branchInfo = (BranchInfo)getTagsViewer().getStructuredSelection().getFirstElement();
        fActionSwitchBranch.setBranch(branchInfo);
        fActionAddBranch.setBranch(branchInfo);
        fActionMergeBranch.setBranch(branchInfo);
        fActionDeleteBranch.setBranch(branchInfo);
    }
    
    private void fillContextMenu(IMenuManager manager) {
        // boolean isEmpty = getViewer().getSelection().isEmpty();

        manager.add(fActionAddBranch);
        manager.add(fActionSwitchBranch);
        manager.add(fActionMergeBranch);
        manager.add(new Separator());
        manager.add(fActionDeleteBranch);
    }

    @Override
    public void setFocus() {
        if(getTagsViewer() != null) {
            getTagsViewer().getControl().setFocus();
        }
    }
    
    TagsTableViewer getTagsViewer() {
        return fTagsTableViewer;
    }
    
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if(part == this || selection == null) {
            return;
        }
        
        Object selected = ((IStructuredSelection)selection).getFirstElement();
        
        IArchiRepository selectedRepository = null;
        
        // Repository selected
        if(selected instanceof IArchiRepository) {
            selectedRepository = (IArchiRepository)selected;
        }
        // Model selected, but is it in a git repo?
        else {
            IArchimateModel model = part.getAdapter(IArchimateModel.class);
            if(GraficoUtils.isModelInLocalRepository(model)) {
                selectedRepository = new ArchiRepository(GraficoUtils.getLocalRepositoryFolderForModel(model));
            }
        }
        
        // Update if selectedRepository is different 
        if(selectedRepository != null && !selectedRepository.equals(fSelectedRepository)) {
            // Store last selected
            fSelectedRepository = selectedRepository;

            // Set label text
            fRepoLabel.setText(Messages.TagsView_0 + " " + selectedRepository.getName()); //$NON-NLS-1$
            
            // Set Branches
            getTagsViewer().doSetInput(selectedRepository);
            
            // Update actions
            fActionAddBranch.setRepository(selectedRepository);
            fActionSwitchBranch.setRepository(selectedRepository);
            fActionMergeBranch.setRepository(selectedRepository);
            fActionDeleteBranch.setRepository(selectedRepository);
        }
    }
    
    @Override
    public void repositoryChanged(String eventName, IArchiRepository repository) {
        if(repository.equals(fSelectedRepository)) {
            switch(eventName) {
                case IRepositoryListener.HISTORY_CHANGED:
                    getTagsViewer().doSetInput(repository);
                    break;
                    
                case IRepositoryListener.REPOSITORY_DELETED:
                    fRepoLabel.setText(Messages.TagsView_0);
                    getTagsViewer().setInput(""); //$NON-NLS-1$
                    fSelectedRepository = null; // Reset this
                    break;
                    
                case IRepositoryListener.REPOSITORY_CHANGED:
                    fRepoLabel.setText(Messages.TagsView_0 + " " + repository.getName()); //$NON-NLS-1$
                    break;

                case IRepositoryListener.BRANCHES_CHANGED:
                    getTagsViewer().doSetInput(repository);
                    break;
                    
                default:
                    break;
            }
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        RepositoryListenerManager.INSTANCE.removeListener(this);
    }
    

    // =================================================================================
    //                       Contextual Help support
    // =================================================================================
    
    @Override
    public int getContextChangeMask() {
        return NONE;
    }

    @Override
    public IContext getContext(Object target) {
        return HelpSystem.getContext(HELP_ID);
    }

    @Override
    public String getSearchExpression(Object target) {
        return Messages.TagsView_1;
    }

}
