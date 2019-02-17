/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.archicontribs.modelrepository.grafico;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

/**
 * Status of Branches
 * 
 * @author Diego Bragato
 */
public class TagStatus {
    
    public final static String localPrefix = "refs/tags/"; //$NON-NLS-1$
    // Check this is an error !!!
    public final static String remotePrefix = "remotes/origin/refs/tags/"; //$NON-NLS-1$
    
    private Map<String, TagInfo> infos = new HashMap<String, TagInfo>();
    
    TagStatus(IArchiRepository archiRepo) throws IOException, GitAPIException {
        try(Git git = Git.open(archiRepo.getLocalRepositoryFolder())) {
            Repository repository = git.getRepository();

            // Get all known tags
            for(Ref ref : git.tagList().call()) {
                TagInfo info = new TagInfo(repository, ref);
                infos.put(info.getFullName(), info);
            }

//           repository.getTags()

//			  Get current local branch
//            String head = repository.getFullBranch();
//            if(head != null) {
//                currentLocalBranch = infos.get(head);
//            }
//            
//            // Get current remote branch
//            if(currentLocalBranch != null) {
//                String remoteName = currentLocalBranch.getRemoteBranchNameFor();
//                if(remoteName != null) {
//                    currentRemoteBranch = infos.get(remoteName);
//                }
//            }
        }
    }
    
    /**
     * @return A union of local branches and remote branches that we are not tracking
     */
    public List<TagInfo> getLocalAndUntrackedRemoteTags() {
        List<TagInfo> list = new ArrayList<TagInfo>();
        for(TagInfo tag : infos.values()) {
            // All local branches
            if(tag.isLocal()) {
                list.add(tag);
            }
            // All remote branches that don't have a local ref
            else if(tag.isRemote() && !tag.hasTrackedRef()) {
                list.add(tag);
            }
        }
        
        return list;
    }
    
    /**
     * @return All local branches
     */
    public List<TagInfo> getLocalTags() {
        List<TagInfo> list = new ArrayList<TagInfo>();
        
        for(TagInfo tag : infos.values()) {
            if(tag.isLocal()) {
                list.add(tag);
            }
        }
        
        return list;
    }
    
    /**
     * @return All remote branches
     */
    public List<TagInfo> getRemoteTags() {
        List<TagInfo> list = new ArrayList<TagInfo>();
        
        for(TagInfo tag : infos.values()) {
            if(tag.isRemote()) {
                list.add(tag);
            }
        }
        
        return list;
    }
    
}
