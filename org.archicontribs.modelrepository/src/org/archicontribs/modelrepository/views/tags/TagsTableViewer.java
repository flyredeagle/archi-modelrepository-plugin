/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.archicontribs.modelrepository.views.tags;

import java.io.IOException;

import org.archicontribs.modelrepository.IModelRepositoryImages;
import org.archicontribs.modelrepository.grafico.TagInfo;
import org.archicontribs.modelrepository.grafico.TagStatus;
import org.archicontribs.modelrepository.grafico.IArchiRepository;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.archimatetool.editor.ui.FontFactory;
import com.archimatetool.editor.ui.components.UpdatingTableColumnLayout;


/**
 * Tags Table Viewer
 */
public class TagsTableViewer extends TableViewer {
    
    /**
     * Constructor
     */
    public TagsTableViewer(Composite parent) {
        super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        
        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(false);
        
        TableColumnLayout tableLayout = (TableColumnLayout)parent.getLayout();
        
        TableViewerColumn column = new TableViewerColumn(this, SWT.NONE, 0);
        column.getColumn().setText(Messages.TagsTableViewer_0);
        tableLayout.setColumnData(column.getColumn(), new ColumnWeightData(50, false));
        
        column = new TableViewerColumn(this, SWT.NONE, 1);
        column.getColumn().setText(Messages.TagsTableViewer_2);
        tableLayout.setColumnData(column.getColumn(), new ColumnWeightData(40, false));

        column = new TableViewerColumn(this, SWT.NONE, 2);
        column.getColumn().setText(Messages.TagsTableViewer_1);
        tableLayout.setColumnData(column.getColumn(), new ColumnWeightData(40, false));

        column = new TableViewerColumn(this, SWT.NONE, 3);
        column.getColumn().setText(Messages.TagsTableViewer_6);
        tableLayout.setColumnData(column.getColumn(), new ColumnWeightData(40, false));

        setContentProvider(new TagsContentProvider());
        setLabelProvider(new TagsLabelProvider());
        
        setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                TagInfo b1 = (TagInfo)e1;
                TagInfo b2 = (TagInfo)e2;
                return b1.getShortName().compareToIgnoreCase(b2.getShortName());
            }
        });
    }

    public void doSetInput(IArchiRepository archiRepo) {
        setInput(archiRepo);
        
        // Do the Layout kludge
        ((UpdatingTableColumnLayout)getTable().getParent().getLayout()).doRelayout();

        // Select first row
        //Object element = getElementAt(0);
        //if(element != null) {
        //    setSelection(new StructuredSelection(element));
        //}
    }
    
    // ===============================================================================================
	// ===================================== Table Model ==============================================
	// ===============================================================================================
    
    /**
     * The Model for the Table.
     */
    class TagsContentProvider implements IStructuredContentProvider {
        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }
        
        @Override
        public Object[] getElements(Object parent) {
            IArchiRepository repo = (IArchiRepository)parent;
            
            // Local Repo was deleted
            if(!repo.getLocalRepositoryFolder().exists()) {
                return new Object[0];
            }
            
            try {
                TagStatus status = repo.getTagStatus();
                if(status != null) {
                    return status.getLocalAndUntrackedRemoteTags().toArray();
                }
            }
            catch(IOException | GitAPIException ex) {
                ex.printStackTrace();
            }
            
            return new Object[0];
        }
    }
    
    // ===============================================================================================
	// ===================================== Label Model ==============================================
	// ===============================================================================================

    class TagsLabelProvider extends CellLabelProvider {
        
        public String getColumnText(TagInfo tagInfo, int columnIndex) {
        	String name = "";
            switch(columnIndex) {
            	case 0:
            		name += tagInfo.getShortName();
                    if(tagInfo.isCurrentBranch()) {
                        name += " " + Messages.TagsTableViewer_2; //$NON-NLS-1$
                    }
                    return name;
                case 1:
                	name += "This should be a description:" + columnIndex;
                	if(tagInfo.isRemoteDeleted()) {
                        name += Messages.TagsTableViewer_3;
                    }
                    if(tagInfo.hasRemoteRef()) {
                    	name += Messages.TagsTableViewer_4;
                    }
                    else {
                    	name += Messages.TagsTableViewer_5;
                    }
                    return name;
                case 2:
                	if(tagInfo.isLocal()) {
                		name =  Messages.TagsTableViewer_3;
                	}else if(tagInfo.isRemote()) {
                		name = Messages.TagsTableViewer_4;
                	}else {
                		name = Messages.TagsTableViewer_7;
                	}
                	return name;
                case 3:
                	// Date column
                	name += "This should be a date:" + columnIndex;
                	return name;
                default:
                    return null;
            }
        }

        @Override
        public void update(ViewerCell cell) {
            if(cell.getElement() instanceof TagInfo) {
                TagInfo tagInfo = (TagInfo)cell.getElement();
                
                cell.setText(getColumnText(tagInfo, cell.getColumnIndex()));
                
                if(tagInfo.isCurrentBranch() && cell.getColumnIndex() == 0) {
                    cell.setFont(FontFactory.SystemFontBold);
                }
                else {
                    cell.setFont(null);
                }
                
                switch(cell.getColumnIndex()) {
                    case 0:
                        cell.setImage(IModelRepositoryImages.ImageFactory.getImage(IModelRepositoryImages.ICON_BRANCH));
                        break;

                    default:
                        break;
                }
            }
        }
        
    }
}
