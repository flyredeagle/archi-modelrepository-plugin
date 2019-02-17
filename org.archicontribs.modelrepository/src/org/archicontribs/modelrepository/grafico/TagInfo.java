/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.archicontribs.modelrepository.grafico;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

/**
 * BranchInfo
 * 
 * @author Diego Bragato
 */
public class TagInfo {
    
    private Ref ref;
    private String shortName;
    private boolean isRemoteDeleted;
    private boolean isCurrentBranch;
    private boolean hasTrackedRef;
    private boolean hasLocalRef;
    private boolean hasRemoteRef;
    
    private File repoDir; 
    
    private final static String REMOTE = Constants.R_REMOTES + IGraficoConstants.ORIGIN + "/"; //$NON-NLS-1$

    TagInfo(Repository repository, Ref ref) throws IOException {
        this.ref = ref;
        
        repoDir = repository.getDirectory();
        
        hasLocalRef = getHasLocalRef(repository);
        hasRemoteRef = getHasRemoteRef(repository);
        hasTrackedRef = getHasTrackedRef(repository);

        isRemoteDeleted = getIsRemoteDeleted(repository);
        isCurrentBranch = getIsCurrentBranch(repository);
    }
    
    public Ref getRef() {
        return ref;
    }
    
    public String getFullName() {
        return ref.getName();
    }
    
    public String getShortName() {
        if(shortName == null) {
            shortName = getShortName(getFullName());
        }
        return shortName;
    }
    public String getMessage() {
    	return "message";
    }
    
    public boolean isLocal() {
        return getFullName().startsWith(TagStatus.localPrefix);
    }

    public boolean isRemote() {
        return getFullName().startsWith(TagStatus.remotePrefix);
    }

    public boolean hasLocalRef() {
        return hasLocalRef;
    }

    public boolean hasRemoteRef() {
        return hasRemoteRef;
    }

    public boolean isRemoteDeleted() {
        return isRemoteDeleted;
    }

    public boolean isCurrentBranch() {
        return isCurrentBranch;
    }
    
    public boolean hasTrackedRef() {
        return hasTrackedRef;
    }
    
    public String getRemoteTagNameFor() {
        return TagStatus.remotePrefix + getShortName();
    }
    
    public String getLocalTagNameFor() {
        return TagStatus.localPrefix + getShortName();
    }

    private boolean getHasLocalRef(Repository repository) throws IOException {
        return repository.findRef(getLocalTagNameFor()) != null;
    }

    private boolean getHasRemoteRef(Repository repository) throws IOException {
        return repository.findRef(getRemoteTagNameFor()) != null;
    }

    private boolean getHasTrackedRef(Repository repository) throws IOException {
        if(isRemote()) {
            return getHasLocalRef(repository);
        }
        
        return getHasRemoteRef(repository);
    }
    
    /*
     * Figure out whether the remote branch has been deleted
     * 1. We have a local branch ref
     * 2. We are tracking it
     * 3. But it does not have a remote branch ref
     */
    private boolean getIsRemoteDeleted(Repository repository) throws IOException {
        if(isRemote()) {
            return false;
        }
        
        // Is it being tracked?
        BranchConfig branchConfig = new BranchConfig(repository.getConfig(), getShortName());
        boolean isBeingTracked = branchConfig.getRemoteTrackingBranch() != null;
        
        // Does it have a remote ref?
        boolean hasNoRemoteTagFor = repository.findRef(getRemoteTagNameFor()) == null;
        
        // Is being tracked but no remote ref
        return isBeingTracked && hasNoRemoteTagFor;
    }

    private boolean getIsCurrentBranch(Repository repository) throws IOException {
        return getFullName().equals(repository.getFullBranch());
    }
    
    private String getShortName(String tagName) {
        if(tagName.startsWith(Constants.R_TAGS)) {
            return tagName.substring(Constants.R_TAGS.length());
        }
        
        if(tagName.startsWith(REMOTE)) {
            return tagName.substring(REMOTE.length());
        }
        
        return tagName;
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj != null) &&
                (obj instanceof TagInfo) &&
                repoDir.equals(((TagInfo)obj).repoDir) &&
                getFullName().equals(((TagInfo)obj).getFullName());
    }
}